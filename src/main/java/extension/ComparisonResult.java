package extension;

public class ComparisonResult {
    public enum Confidence {
        HIGH, MEDIUM, LOW
    }

    public enum Status {
        SUCCESS, SUSPICIOUS, FAILED
    }

    private final Status status;
    private final Confidence confidence;
    private final String reason;
    private final int statusCodeDiff;
    private final double lengthDiffPercent;

    public ComparisonResult(Status status, Confidence confidence, String reason,
                           int statusCodeDiff, double lengthDiffPercent) {
        this.status = status;
        this.confidence = confidence;
        this.reason = reason;
        this.statusCodeDiff = statusCodeDiff;
        this.lengthDiffPercent = lengthDiffPercent;
    }

    public Status getStatus() {
        return status;
    }

    public Confidence getConfidence() {
        return confidence;
    }

    public String getReason() {
        return reason;
    }

    public int getStatusCodeDiff() {
        return statusCodeDiff;
    }

    public double getLengthDiffPercent() {
        return lengthDiffPercent;
    }

    public String getDisplaySymbol() {
        switch (status) {
            case SUCCESS: return "✓";
            case SUSPICIOUS: return "?";
            case FAILED: return "✗";
            default: return "-";
        }
    }
}
