package com.myfinal.objectengine.enums;

/**
 * V1 支持的字段类型，禁止自行扩展
 */
public enum FieldType {

    TEXT,
    TEXTAREA,
    NUMBER,
    MONEY,
    DATE,
    SELECT;

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
