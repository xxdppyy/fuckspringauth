package extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 参数变异工具类
 */
public class ParameterMutator {

    /**
     * 参数信息
     */
    public static class Parameter {
        public String name;
        public String value;
        public boolean isNumeric;

        public Parameter(String name, String value) {
            this.name = name;
            this.value = value;
            this.isNumeric = isNumeric(value);
        }

        private boolean isNumeric(String str) {
            if (str == null || str.isEmpty()) {
                return false;
            }
            try {
                Integer.parseInt(str);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    /**
     * 变异结果
     */
    public static class Mutation {
        public String mutatedContent;
        public String description;

        public Mutation(String mutatedContent, String description) {
            this.mutatedContent = mutatedContent;
            this.description = description;
        }
    }

    /**
     * 从URL查询字符串提取参数
     */
    public static List<Parameter> extractFromQuery(String query) {
        List<Parameter> params = new ArrayList<>();
        if (query == null || query.isEmpty() || !query.contains("=")) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            if (pair.contains("=")) {
                String[] kv = pair.split("=", 2);
                params.add(new Parameter(kv[0], kv.length > 1 ? kv[1] : ""));
            }
        }
        return params;
    }

    /**
     * 从JSON提取参数
     */
    public static List<Parameter> extractFromJson(String json) {
        List<Parameter> params = new ArrayList<>();
        if (json == null || !json.contains("{")) {
            return params;
        }

        // 简单的JSON解析（只处理简单的key:value格式）
        String[] parts = json.split("\"");
        for (int i = 0; i < parts.length; i++) {
            // 找到key（前面是{ 或 ,）
            if (i > 0 && (parts[i - 1].trim().endsWith("{") || parts[i - 1].trim().endsWith(","))) {
                String key = parts[i];
                // 下一个应该是 ":"，再下一个是值
                if (i + 2 < parts.length && parts[i + 1].trim().startsWith(":")) {
                    String value = parts[i + 2];
                    if (key.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                        params.add(new Parameter(key, value));
                    }
                }
            }
        }
        return params;
    }

    /**
     * 生成单个参数的所有变异
     */
    public static List<Mutation> generateMutations(List<Parameter> allParams, int targetIndex, String format) {
        List<Mutation> mutations = new ArrayList<>();
        Parameter target = allParams.get(targetIndex);

        // 1. 数组变异
        mutations.add(createArrayMutation(allParams, targetIndex, format));

        // 2. 参数污染（命名变异）
        String[] namingStyles = {"snake_case", "camelCase", "UPPERCASE", "PascalCase"};
        for (String style : namingStyles) {
            String variantName = ParameterNamingConverter.convertNaming(target.name, style);
            if (!variantName.equals(target.name)) {
                mutations.add(createPollutionMutation(allParams, targetIndex, variantName, target.value, format, style));
                mutations.add(createOverrideMutation(allParams, targetIndex, variantName, target.value, format, style));
            }
        }

        // 3. 数值变异（仅数字）
        if (target.isNumeric) {
            int originalValue = Integer.parseInt(target.value);
            mutations.add(createValueMutation(allParams, targetIndex, String.valueOf(originalValue + 1), format, "+1"));
            mutations.add(createValueMutation(allParams, targetIndex, "0", format, "=0"));
            mutations.add(createValueMutation(allParams, targetIndex, "-1", format, "=-1"));
        }

        return mutations;
    }

    // 创建数组变异
    private static Mutation createArrayMutation(List<Parameter> params, int targetIndex, String format) {
        if (format.equals("json")) {
            return new Mutation(buildJsonWithArray(params, targetIndex), "Array: " + params.get(targetIndex).name + "[]");
        } else {
            return new Mutation(buildQueryWithArray(params, targetIndex), "Array: " + params.get(targetIndex).name + "[]");
        }
    }

    // 创建参数污染变异
    private static Mutation createPollutionMutation(List<Parameter> params, int targetIndex, String variantName, String value, String format, String style) {
        if (format.equals("json")) {
            return new Mutation(buildJsonWithDuplicate(params, targetIndex, variantName, value), "Pollution: " + style);
        } else {
            return new Mutation(buildQueryWithDuplicate(params, targetIndex, variantName, value), "Pollution: " + style);
        }
    }

    // 创建参数覆盖变异
    private static Mutation createOverrideMutation(List<Parameter> params, int targetIndex, String variantName, String value, String format, String style) {
        if (format.equals("json")) {
            return new Mutation(buildJsonWithOverride(params, targetIndex, variantName, value), "Override: " + style);
        } else {
            return new Mutation(buildQueryWithOverride(params, targetIndex, variantName, value), "Override: " + style);
        }
    }

    // 创建数值变异
    private static Mutation createValueMutation(List<Parameter> params, int targetIndex, String newValue, String format, String desc) {
        if (format.equals("json")) {
            return new Mutation(buildJsonWithValue(params, targetIndex, newValue), "Value: " + params.get(targetIndex).name + desc);
        } else {
            return new Mutation(buildQueryWithValue(params, targetIndex, newValue), "Value: " + params.get(targetIndex).name + desc);
        }
    }

    // 构建带数组的JSON
    private static String buildJsonWithArray(List<Parameter> params, int targetIndex) {
        StringBuilder json = new StringBuilder("{");
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(params.get(i).name).append("\":");
            if (i == targetIndex) {
                json.append("[\"").append(params.get(i).value).append("\"]");
            } else {
                json.append("\"").append(params.get(i).value).append("\"");
            }
        }
        json.append("}");
        return json.toString();
    }

    // 构建带数组的Query
    private static String buildQueryWithArray(List<Parameter> params, int targetIndex) {
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) query.append("&");
            if (i == targetIndex) {
                query.append(params.get(i).name).append("[]=").append(params.get(i).value);
            } else {
                query.append(params.get(i).name).append("=").append(params.get(i).value);
            }
        }
        return query.toString();
    }

    // 构建带重复参数的JSON
    private static String buildJsonWithDuplicate(List<Parameter> params, int targetIndex, String variantName, String value) {
        StringBuilder json = new StringBuilder("{");
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(params.get(i).name).append("\":\"").append(params.get(i).value).append("\"");
        }
        json.append(",\"").append(variantName).append("\":\"").append(value).append("\"}");
        return json.toString();
    }

    // 构建带重复参数的Query
    private static String buildQueryWithDuplicate(List<Parameter> params, int targetIndex, String variantName, String value) {
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) query.append("&");
            query.append(params.get(i).name).append("=").append(params.get(i).value);
        }
        query.append("&").append(variantName).append("=").append(value);
        return query.toString();
    }

    // 构建修改值的JSON
    private static String buildJsonWithValue(List<Parameter> params, int targetIndex, String newValue) {
        StringBuilder json = new StringBuilder("{");
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(params.get(i).name).append("\":");
            if (i == targetIndex) {
                json.append("\"").append(newValue).append("\"");
            } else {
                json.append("\"").append(params.get(i).value).append("\"");
            }
        }
        json.append("}");
        return json.toString();
    }

    // 构建修改值的Query
    private static String buildQueryWithValue(List<Parameter> params, int targetIndex, String newValue) {
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) query.append("&");
            query.append(params.get(i).name).append("=");
            if (i == targetIndex) {
                query.append(newValue);
            } else {
                query.append(params.get(i).value);
            }
        }
        return query.toString();
    }

    // 构建参数覆盖的JSON（用变体名替换原参数名）
    private static String buildJsonWithOverride(List<Parameter> params, int targetIndex, String variantName, String value) {
        StringBuilder json = new StringBuilder("{");
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) json.append(",");
            if (i == targetIndex) {
                json.append("\"").append(variantName).append("\":\"").append(value).append("\"");
            } else {
                json.append("\"").append(params.get(i).name).append("\":\"").append(params.get(i).value).append("\"");
            }
        }
        json.append("}");
        return json.toString();
    }

    // 构建参数覆盖的Query（用变体名替换原参数名）
    private static String buildQueryWithOverride(List<Parameter> params, int targetIndex, String variantName, String value) {
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) query.append("&");
            if (i == targetIndex) {
                query.append(variantName).append("=").append(value);
            } else {
                query.append(params.get(i).name).append("=").append(params.get(i).value);
            }
        }
        return query.toString();
    }
}
