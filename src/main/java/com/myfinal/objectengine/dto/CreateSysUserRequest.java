package com.myfinal.objectengine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建系统用户请求（密码 BCrypt 入库）
 */
@Data
public class CreateSysUserRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在 6 ~ 32 位之间")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String email;

    private String phone;

    private String remark;
}
