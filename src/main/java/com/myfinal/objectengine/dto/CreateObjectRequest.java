package com.myfinal.objectengine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建自定义对象请求
 */
@Data
public class CreateObjectRequest {

    /**
     * API 名称，例如 project__c，创建后不可修改
     */
    @NotBlank(message = "对象API名称不能为空")
    private String apiName;

    /**
     * 对象名称
     */
    @NotBlank(message = "对象名称不能为空")
    private String objectName;

    private String description;

    private String remark;

    private String icon;

    private Integer sort;
}
