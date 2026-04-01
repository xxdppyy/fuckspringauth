package extension;

import burp.api.montoya.http.message.responses.HttpResponse;

import java.util.Arrays;
import java.util.List;

public class ResponseComparator {
    private static final List<String> SUCCESS_KEYWORDS = Arrays.asList(
        "username", "email", "admin", "data", "user", "profile", "dashboard",
        "phone", "number", "password", "name"
    );

    private static final List<String> FAILURE_KEYWORDS = Arrays.asList(
        "unauthorized", "forbidden", "access denied", "login", "authentication required"
    );

    private final HttpResponse baselineResponse;
    private final double lengthThreshold;

    public ResponseComparator(HttpResponse baselineResponse, double lengthThreshold) {
        this.baselineResponse = baselineResponse;
        this.lengthThreshold = lengthThreshold;
    }

    public ComparisonResult compare(HttpResponse testResponse) {
        // 检查Content-Type，如果是text/html则直接判定为失败
        String contentType = testResponse.headerValue("Content-Type");
        if (contentType != null && contentType.toLowerCase().contains("text/html")) {
            return new ComparisonResult(
                ComparisonResult.Status.FAILED,
                ComparisonResult.Confidence.HIGH,
                "Content-Type is text/html",
                0, 0
            );
        }

        int baselineStatus = baselineResponse.statusCode();
        int testStatus = testResponse.statusCode();
        int statusDiff = testStatus - baselineStatus;

        int baselineLength = baselineResponse.body().length();
        int testLength = testResponse.body().length();
        int absoluteDiff = testLength - baselineLength;
        double lengthDiff;

        if (baselineLength == 0) {
            lengthDiff = testLength > 0 ? 999.9 : 0;
        } else {
            lengthDiff = ((double)absoluteDiff / (double)baselineLength) * 100;
        }

        String testBody = testResponse.bodyToString().toLowerCase();

        // 判断逻辑
        return analyzeResponse(baselineStatus, testStatus, statusDiff, lengthDiff, testBody, absoluteDiff);
    }

    private ComparisonResult analyzeResponse(int baselineStatus, int testStatus,
                                            int statusDiff, double lengthDiff, String testBody, int absoluteDiff) {
        // 明确失败：test响应为4xx/5xx错误
        if (testStatus >= 400) {
            return new ComparisonResult(
                ComparisonResult.Status.FAILED,
                ComparisonResult.Confidence.HIGH,
                "Response status is " + testStatus + " (error)",
                statusDiff, lengthDiff
            );
        }

        // 明确成功：状态码从4xx变为2xx/3xx
        if (baselineStatus >= 400 && testStatus >= 200 && testStatus < 400) {
            return new ComparisonResult(
                ComparisonResult.Status.SUCCESS,
                ComparisonResult.Confidence.HIGH,
                "Status code changed from " + baselineStatus + " to " + testStatus,
                statusDiff, lengthDiff
            );
        }

        // 明确成功：状态码从3xx重定向变为2xx成功
        if (baselineStatus >= 300 && baselineStatus < 400 && testStatus >= 200 && testStatus < 300) {
            return new ComparisonResult(
                ComparisonResult.Status.SUCCESS,
                ComparisonResult.Confidence.HIGH,
                "Status code changed from " + baselineStatus + " redirect to " + testStatus,
                statusDiff, lengthDiff
            );
        }

        // 明确成功：响应长度显著增加
        if (lengthDiff > lengthThreshold) {
            boolean hasSuccessKeyword = SUCCESS_KEYWORDS.stream().anyMatch(testBody::contains);
            if (hasSuccessKeyword) {
                return new ComparisonResult(
                    ComparisonResult.Status.SUCCESS,
                    ComparisonResult.Confidence.HIGH,
                    "Length increased " + String.format("%.1f", lengthDiff) + "% with success keywords",
                    statusDiff, lengthDiff
                );
            }
            return new ComparisonResult(
                ComparisonResult.Status.SUSPICIOUS,
                ComparisonResult.Confidence.MEDIUM,
                "Length increased " + String.format("%.1f", lengthDiff) + "%",
                statusDiff, lengthDiff
            );
        }

        // 疑似成功：响应长度中等变化
        if (lengthDiff > 20) {
            return new ComparisonResult(
                ComparisonResult.Status.SUSPICIOUS,
                ComparisonResult.Confidence.LOW,
                "Length increased " + String.format("%.1f", lengthDiff) + "%",
                statusDiff, lengthDiff
            );
        }

        // 明确失败
        return new ComparisonResult(
            ComparisonResult.Status.FAILED,
            ComparisonResult.Confidence.HIGH,
            "No significant difference",
            statusDiff, lengthDiff
        );
    }
}
