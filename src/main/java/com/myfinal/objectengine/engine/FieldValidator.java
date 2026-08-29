package com.myfinal.objectengine.engine;

import com.myfinal.objectengine.domain.CustomField;

/**
 * 单字段值校验器，校验失败抛 BusinessException(400)
 */
@FunctionalInterface
public interface FieldValidator {

    void validate(CustomField field, Object value);
}
