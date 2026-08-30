package com.myfinal.objectengine.engine;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.myfinal.objectengine.common.BusinessException;
import com.myfinal.objectengine.domain.CustomField;
import com.myfinal.objectengine.enums.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 字段类型注册表：每种 FieldType 对应一个值校验器
 */
public final class FieldTypeRegistry {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$");

    /** 手机号（11 位）或座机（区号-号码） */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$|^0\\d{2,3}-?\\d{7,8}$");

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern URL_PATTERN = Pattern.compile("^https?://\\S+$");

    private static final Map<FieldType, FieldValidator> REGISTRY = new EnumMap<>(FieldType.class);

    static {
        REGISTRY.put(FieldType.TEXT, FieldTypeRegistry::requireText);
        REGISTRY.put(FieldType.TEXTAREA, FieldTypeRegistry::requireText);
        REGISTRY.put(FieldType.NUMBER, FieldTypeRegistry::requireNumberWithDigits);
        REGISTRY.put(FieldType.MONEY, FieldTypeRegistry::requireNumberWithDigits);
        REGISTRY.put(FieldType.PERCENT, FieldTypeRegistry::requireNumberWithDigits);
        REGISTRY.put(FieldType.DATE, FieldTypeRegistry::requireDate);
        REGISTRY.put(FieldType.SELECT, FieldTypeRegistry::requireOption);
        REGISTRY.put(FieldType.MULTI_SELECT, FieldTypeRegistry::requireOptionList);
        REGISTRY.put(FieldType.REFERENCE, FieldTypeRegistry::requireReference);
        REGISTRY.put(FieldType.BOOLEAN, FieldTypeRegistry::requireBoolean);
        REGISTRY.put(FieldType.TIME, FieldTypeRegistry::requireTime);
        REGISTRY.put(FieldType.PHONE, FieldTypeRegistry::requirePhone);
        REGISTRY.put(FieldType.EMAIL, FieldTypeRegistry::requireEmail);
        REGISTRY.put(FieldType.URL, FieldTypeRegistry::requireUrl);
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
     * 创建/修改字段时校验类型配置：
     * LOCAL 的 SELECT / MULTI_SELECT 必须配置非空 options（GLOBAL 由选项集提供，跳过）；
     * 长度、位数配置需在合法范围内
     */
    public static void validateConfig(String fieldType, String fieldName, Object configJson, boolean enforceOptions) {
        boolean optionType = FieldType.SELECT.name().equals(fieldType)
            || FieldType.MULTI_SELECT.name().equals(fieldType);
        if (optionType && enforceOptions && extractOptionValues(configJson).isEmpty()) {
            throw BusinessException.badRequest("字段【" + fieldName + "】必须配置 options 选项");
        }
        JSONObject config = parseConfig(configJson);
        Integer maxLength = config.getInteger("maxLength");
        Integer minLength = config.getInteger("minLength");
        if (maxLength != null && maxLength < 1) {
            throw BusinessException.badRequest("字段【" + fieldName + "】最大长度必须大于 0");
        }
        if (minLength != null && minLength < 0) {
            throw BusinessException.badRequest("字段【" + fieldName + "】最小长度不能为负数");
        }
        if (maxLength != null && minLength != null && minLength > maxLength) {
            throw BusinessException.badRequest("字段【" + fieldName + "】最小长度不能大于最大长度");
        }
        Integer integerLength = config.getInteger("integerLength");
        Integer scale = config.getInteger("scale");
        if (integerLength != null && (integerLength < 1 || integerLength > 15)) {
            throw BusinessException.badRequest("字段【" + fieldName + "】整数长度需在 1 ~ 15 位之间");
        }
        if (scale != null && (scale < 0 || scale > 6)) {
            throw BusinessException.badRequest("字段【" + fieldName + "】小数位数需在 0 ~ 6 位之间");
        }
        if (integerLength != null && scale != null && scale >= integerLength) {
            throw BusinessException.badRequest("字段【" + fieldName + "】小数位数必须小于整数长度");
        }
    }

    private static void requireText(CustomField field, Object value) {
        if (!(value instanceof String s)) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】必须为字符串");
        }
        JSONObject config = parseConfig(field.getConfigJson());
        Integer maxLength = config.getInteger("maxLength");
        Integer minLength = config.getInteger("minLength");
        if (maxLength != null && s.length() > maxLength) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】长度不能超过 " + maxLength + " 个字符");
        }
        if (minLength != null && s.length() < minLength) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】长度不能少于 " + minLength + " 个字符");
        }
    }

    private static void requireNumberWithDigits(CustomField field, Object value) {
        if (!(value instanceof Number)) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】必须为数字");
        }
        JSONObject config = parseConfig(field.getConfigJson());
        Integer scale = config.getInteger("scale");
        Integer integerLength = config.getInteger("integerLength");
        if (scale == null && integerLength == null) {
            return;
        }
        BigDecimal number = new BigDecimal(String.valueOf(value));
        if (scale != null && number.scale() > scale) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】最多保留 " + scale + " 位小数");
        }
        if (integerLength != null) {
            int intDigits = Math.max(0, number.precision() - number.scale());
            if (intDigits > integerLength) {
                throw BusinessException.badRequest("字段【" + field.getFieldName() + "】整数部分不能超过 " + integerLength + " 位");
            }
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
     * 提取关联字段值中的记录 ID：接受数字或 { "recordId": 数字 } 结构
     */
    public static Long referenceRecordId(CustomField field, Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof Map<?, ?> map && map.get("recordId") instanceof Number number) {
            return number.longValue();
        }
        throw BusinessException.badRequest("字段【" + field.getFieldName() + "】必须是关联的记录");
    }

    private static void requireReference(CustomField field, Object value) {
        referenceRecordId(field, value);
    }

    private static void requireOptionList(CustomField field, Object value) {
        List<String> optionValues = extractOptionValues(field.getConfigJson());
        if (optionValues.isEmpty()) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】未配置选项");
        }
        if (!(value instanceof List<?> values)) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】必须为选项数组");
        }
        for (Object item : values) {
            if (!optionValues.contains(String.valueOf(item))) {
                throw BusinessException.badRequest("字段【" + field.getFieldName() + "】的值不在选项范围内：" + item);
            }
        }
    }

    private static void requireBoolean(CustomField field, Object value) {
        if (value instanceof Boolean) {
            return;
        }
        if (value instanceof String s && ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s))) {
            return;
        }
        throw BusinessException.badRequest("字段【" + field.getFieldName() + "】必须为布尔值");
    }

    private static void requireTime(CustomField field, Object value) {
        if (!(value instanceof String s) || !TIME_PATTERN.matcher(s).matches()) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】必须为时间（HH:mm 或 HH:mm:ss）");
        }
    }

    private static void requirePhone(CustomField field, Object value) {
        if (!(value instanceof String s) || !PHONE_PATTERN.matcher(s).matches()) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】必须为合法的手机号或座机号");
        }
    }

    private static void requireEmail(CustomField field, Object value) {
        if (!(value instanceof String s) || !EMAIL_PATTERN.matcher(s).matches()) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】必须为合法的邮箱地址");
        }
    }

    private static void requireUrl(CustomField field, Object value) {
        if (!(value instanceof String s) || !URL_PATTERN.matcher(s).matches()) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】必须为合法的网址（以 http(s):// 开头）");
        }
    }

    /** configJson 统一解析为 JSONObject，空值返回空对象 */
    private static JSONObject parseConfig(Object configJson) {
        if (configJson == null) {
            return new JSONObject();
        }
        if (configJson instanceof String s) {
            return s.isBlank() ? new JSONObject() : JSON.parseObject(s);
        }
        return JSON.parseObject(JSON.toJSONString(configJson));
    }

    /**
     * 从 configJson 提取 SELECT 选项值列表，config 形如 { "options": [ { "label": xx, "value": yy } ] }
     */
    private static List<String> extractOptionValues(Object configJson) {
        JSONArray options = parseConfig(configJson).getJSONArray("options");
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
