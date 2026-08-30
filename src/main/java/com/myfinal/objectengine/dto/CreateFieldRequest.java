package com.myfinal.objectengine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建自定义字段请求
 */
@Data
public class CreateFieldRequest {

    /**
     * 字段 API 名称，例如 name，创建后不可修改
     */
    @NotBlank(message = "字段API名称不能为空")
    private String apiName;

    /**
     * 字段名称
     */
    @NotBlank(message = "字段名称不能为空")
    private String fieldName;

    /**
     * 字段类型：TEXT/TEXTAREA/NUMBER/MONEY/DATE/SELECT
     */
    @NotBlank(message = "字段类型不能为空")
    private String fieldType;

    private String description;

    private String remark;

    private Integer requiredFlag;

    private Integer uniqueFlag;

    private Integer searchableFlag;

    private Integer sortableFlag;

    private Integer sort;

    private String defaultValue;

    /**
     * 扩展配置，SELECT 类型存放 { "options": [ { "label": xx, "value": yy } ] }
     */
    private Object configJson;

    /**
     * 选项来源：LOCAL字段自有（默认），GLOBAL引用通用选项集，仅选择类字段有效
     */
    private String optionSource;

    /**
     * 通用选项集ID（optionSource=GLOBAL 时必填）
     */
    private Long optionSetId;
}
