package com.myfinal.objectengine.dto;

import lombok.Data;

/**
 * 修改通用选项集请求（apiName 创建后不可修改）
 */
@Data
public class UpdateOptionSetRequest {

    private String name;

    private String description;

    private String remark;

    private Integer sort;

    private Integer status;
}
