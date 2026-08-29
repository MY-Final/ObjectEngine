package com.myfinal.objectengine.common;

import com.alibaba.fastjson2.JSON;

import java.util.Map;

/**
 * JSON 列（data_json / config_json）读写辅助
 */
public final class JsonUtils {

    private JsonUtils() {
    }

    /**
     * 序列化为 JSON 字符串，用于写入 JSON 列
     */
    public static String write(Object value) {
        return value == null ? null : JSON.toJSONString(value);
    }

    /**
     * 归一化为 JSON 对象：入参可能是 DB 读出的字符串，也可能是请求里的 Map
     */
    public static Object parse(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof String s) {
            return s.isBlank() ? null : JSON.parseObject(s);
        }
        return JSON.parseObject(JSON.toJSONString(raw));
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object raw) {
        Object parsed = parse(raw);
        return parsed instanceof Map ? (Map<String, Object>) parsed : null;
    }
}
