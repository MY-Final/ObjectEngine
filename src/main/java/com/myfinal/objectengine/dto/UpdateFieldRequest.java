package com.myfinal.objectengine.dto;

import lombok.Data;

/**
 * 修改自定义字段请求，不包含 apiName / fieldType（均不可修改）
 */
@Data
public class UpdateFieldRequest {

    private String fieldName;

    private String description;

    private String remark;

    private Integer requiredFlag;

    private Integer uniqueFlag;

    private Integer searchableFlag;

    private Integer sortableFlag;

    private Integer sort;

    private String defaultValue;

    private Object configJson;

    /**
     * 状态：1启用，0停用
     */
    private Integer status;
}
