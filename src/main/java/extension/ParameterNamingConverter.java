package extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 参数命名转换工具类
 */
public class ParameterNamingConverter {

    // 常见单词词典（按长度降序排列，优先匹配长单词）
    private static final String[] COMMON_WORDS = {
        "password", "username", "roletype", "admintoken", "phonenumber",
        "admin", "token", "email", "phone", "number", "status", "group",
        "user", "role", "name", "type", "data", "info", "code", "time",
        "date", "list", "item", "level", "flag", "mode", "count", "value",
        "key", "id"
    };

    /**
     * 智能分词：将参数名分割成单词
     */
    public static List<String> splitParamWords(String param) {
        List<String> words = new ArrayList<>();

        // 1. 如果已经有下划线分隔符，直接分割
        if (param.contains("_")) {
            return Arrays.asList(param.split("_"));
        }

        // 2. 如果已经有连字符，直接分割
        if (param.contains("-")) {
            return Arrays.asList(param.split("-"));
        }

        // 3. 如果是驼峰命名，按大写字母分割
        if (param.matches(".*[a-z][A-Z].*")) {
            String[] parts = param.split("(?=[A-Z])");
            return Arrays.asList(parts);
        }

        // 4. 使用词典匹配（贪婪匹配，优先匹配长单词）
        String remaining = param.toLowerCase();
        while (!remaining.isEmpty()) {
            boolean found = false;
            for (String word : COMMON_WORDS) {
                if (remaining.startsWith(word)) {
                    words.add(word);
                    remaining = remaining.substring(word.length());
                    found = true;
                    break;
                }
            }
            if (!found) {
                // 无法匹配，取剩余部分
                words.add(remaining);
                break;
            }
        }

        return words.isEmpty() ? Arrays.asList(param) : words;
    }

    /**
     * 转换参数命名格式
     */
    public static String convertNaming(String param, String style) {
        List<String> words = splitParamWords(param);

        switch (style) {
            case "snake_case":
                return String.join("_", words).toLowerCase();

            case "camelCase":
                if (words.isEmpty()) return param;
                StringBuilder camel = new StringBuilder(words.get(0).toLowerCase());
                for (int i = 1; i < words.size(); i++) {
                    String word = words.get(i);
                    camel.append(Character.toUpperCase(word.charAt(0)))
                         .append(word.substring(1).toLowerCase());
                }
                return camel.toString();

            case "UPPERCASE":
                return param.toUpperCase();

            case "PascalCase":
                StringBuilder pascal = new StringBuilder();
                for (String word : words) {
                    pascal.append(Character.toUpperCase(word.charAt(0)))
                          .append(word.substring(1).toLowerCase());
                }
                return pascal.toString();

            default:
                return param;
        }
    }
}
