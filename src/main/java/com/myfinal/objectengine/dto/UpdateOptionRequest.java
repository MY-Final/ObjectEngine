package com.myfinal.objectengine.dto;

import lombok.Data;

/**
 * 修改通用选项请求（value 已被 Record 引用，创建后不可修改，只允许改展示与排序属性）
 */
@Data
public class UpdateOptionRequest {

    private String label;

    private Integer sort;

    private Integer status;

    private Integer isDefault;

    private String description;

    private String remark;
}
