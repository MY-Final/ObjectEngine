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
     * 选项来源：LOCAL字段自有，GLOBAL引用通用选项集，仅选择类字段有效
     */
    private String optionSource;

    /**
     * 通用选项集ID（optionSource=GLOBAL 时必填）
     */
    private Long optionSetId;

    /**
     * 状态：1启用，0停用
     */
    private Integer status;
}
