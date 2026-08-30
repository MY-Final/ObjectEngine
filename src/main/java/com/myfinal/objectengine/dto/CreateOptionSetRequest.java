package com.myfinal.objectengine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建通用选项集请求
 */
@Data
public class CreateOptionSetRequest {

    @NotBlank(message = "选项集名称不能为空")
    private String name;

    @NotBlank(message = "选项集API名称不能为空")
    private String apiName;

    private String description;

    private String remark;

    private Integer sort;

    private Integer status;
}
