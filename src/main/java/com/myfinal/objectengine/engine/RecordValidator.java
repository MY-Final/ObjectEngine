package com.myfinal.objectengine.engine;

import com.myfinal.objectengine.common.BusinessException;
import com.myfinal.objectengine.domain.CustomField;
import com.myfinal.objectengine.enums.FieldType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Record 请求校验：字段是否存在 → required → 字段类型 → SELECT 选项
 */
public final class RecordValidator {

    private RecordValidator() {
    }

    public static void validate(List<CustomField> fields, Map<String, Object> body) {
        Map<String, CustomField> fieldMap = new HashMap<>();
        for (CustomField field : fields) {
            fieldMap.put(field.getApiName(), field);
        }

        for (String key : body.keySet()) {
            if (!fieldMap.containsKey(key)) {
                throw BusinessException.badRequest("字段【" + key + "】不存在");
            }
        }

        for (CustomField field : fields) {
            Object value = body.get(field.getApiName());
            if (isBlank(value)) {
                if (Integer.valueOf(1).equals(field.getRequiredFlag())) {
                    throw BusinessException.badRequest("字段【" + field.getFieldName() + "】不能为空");
                }
                continue;
            }
            FieldType type = FieldType.parse(field.getFieldType());
            if (type == null) {
                throw BusinessException.badRequest("字段类型不合法：" + field.getFieldType());
            }
            FieldTypeRegistry.get(type).validate(field, value);
        }
    }

    private static boolean isBlank(Object value) {
        return value == null || (value instanceof String s && s.isEmpty());
    }
}
