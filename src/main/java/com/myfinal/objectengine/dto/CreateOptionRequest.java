package com.myfinal.objectengine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增通用选项请求
 */
@Data
public class CreateOptionRequest {

    @NotBlank(message = "选项名称不能为空")
    private String label;

    @NotBlank(message = "选项值不能为空")
    private String value;

    private Integer sort;

    private Integer status;

    private Integer isDefault;

    private String description;

    private String remark;
}
