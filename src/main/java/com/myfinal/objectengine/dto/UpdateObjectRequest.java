package com.myfinal.objectengine.dto;

import lombok.Data;

/**
 * 修改自定义对象请求，不包含 apiName（创建后不可修改）
 */
@Data
public class UpdateObjectRequest {

    private String objectName;

    private String description;

    private String remark;

    private String icon;

    private Integer sort;

    /**
     * 状态：1启用，0停用
     */
    private Integer status;
}
