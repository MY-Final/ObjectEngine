package com.myfinal.objectengine.engine;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.myfinal.objectengine.common.BusinessException;
import com.myfinal.objectengine.domain.CustomField;
import com.myfinal.objectengine.enums.FieldType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 字段类型注册表：每种 FieldType 对应一个值校验器
 */
public final class FieldTypeRegistry {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Map<FieldType, FieldValidator> REGISTRY = new EnumMap<>(FieldType.class);

    static {
        FieldValidator text = FieldTypeRegistry::requireString;
        REGISTRY.put(FieldType.TEXT, text);
        REGISTRY.put(FieldType.TEXTAREA, text);
        REGISTRY.put(FieldType.NUMBER, FieldTypeRegistry::requireNumber);
        REGISTRY.put(FieldType.MONEY, FieldTypeRegistry::requireNumber);
        REGISTRY.put(FieldType.DATE, FieldTypeRegistry::requireDate);
        REGISTRY.put(FieldType.SELECT, FieldTypeRegistry::requireOption);
    }

    private FieldTypeRegistry() {
    }

    public static FieldValidator get(FieldType type) {
        FieldValidator validator = REGISTRY.get(type);
        if (validator == null) {
            throw BusinessException.badRequest("字段类型不合法：" + type);
        }
        return validator;
    }

    /**
     * 创建/修改字段时校验类型配置（SELECT 必须配置非空 options）
     */
    public static void validateConfig(String fieldType, String fieldName, Object configJson) {
        if (!FieldType.SELECT.name().equals(fieldType)) {
            return;
        }
        if (extractOptionValues(configJson).isEmpty()) {
            throw BusinessException.badRequest("字段【" + fieldName + "】必须配置 options 选项");
        }
    }

    private static void requireString(CustomField field, Object value) {
        if (!(value instanceof String)) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】必须为字符串");
        }
    }

    private static void requireNumber(CustomField field, Object value) {
        if (!(value instanceof Number)) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】必须为数字");
        }
    }

    private static void requireDate(CustomField field, Object value) {
        if (!(value instanceof String s)) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】必须为日期（yyyy-MM-dd）");
        }
        try {
            LocalDate.parse(s, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】必须为日期（yyyy-MM-dd）");
        }
    }

    private static void requireOption(CustomField field, Object value) {
        List<String> optionValues = extractOptionValues(field.getConfigJson());
        if (optionValues.isEmpty()) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】未配置选项");
        }
        if (!optionValues.contains(String.valueOf(value))) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】的值不在选项范围内");
        }
    }

    /**
     * 从 configJson 提取 SELECT 选项值列表，config 形如 { "options": [ { "label": xx, "value": yy } ] }
     */
    private static List<String> extractOptionValues(Object configJson) {
        if (configJson == null) {
            return List.of();
        }
        JSONObject config;
        if (configJson instanceof String s) {
            if (s.isBlank()) {
                return List.of();
            }
            config = JSON.parseObject(s);
        } else {
            config = JSON.parseObject(JSON.toJSONString(configJson));
        }
        JSONArray options = config.getJSONArray("options");
        if (options == null) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            String value = options.getJSONObject(i).getString("value");
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }
}
