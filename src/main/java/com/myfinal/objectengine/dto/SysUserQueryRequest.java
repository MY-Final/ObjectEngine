package com.myfinal.objectengine.dto;

import lombok.Data;

/**
 * 用户列表查询参数
 */
@Data
public class SysUserQueryRequest {

    /**
     * 页码，默认 1
     */
    private Integer page;

    /**
     * 每页条数，默认 20
     */
    private Integer pageSize;

    /**
     * 用户名 / 姓名 模糊匹配
     */
    private String keyword;

    /**
     * 状态：0禁用，1启用
     */
    private Integer status;
}
