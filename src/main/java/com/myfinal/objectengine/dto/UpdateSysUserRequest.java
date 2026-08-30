package com.myfinal.objectengine.dto;

import lombok.Data;

/**
 * 修改系统用户请求（用户名不可修改；状态走独立接口）
 */
@Data
public class UpdateSysUserRequest {

    private String name;

    private String email;

    private String phone;

    private String remark;
}
