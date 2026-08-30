package com.myfinal.objectengine.enums;

/**
 * 支持的字段类型；新增类型需同步扩展 FieldTypeRegistry 校验器与前端渲染映射
 */
public enum FieldType {

    TEXT,
    TEXTAREA,
    NUMBER,
    MONEY,
    DATE,
    SELECT,
    MULTI_SELECT,
    LOOKUP,
    REFERENCE,
    BOOLEAN,
    TIME,
    PHONE,
    EMAIL,
    URL,
    PERCENT;

    /**
     * 解析字段类型字符串，不合法返回 null
     */
    public static FieldType parse(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            return FieldType.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
