package com.myfinal.objectengine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置用户密码请求
 */
@Data
public class ResetUserPasswordRequest {

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在 6 ~ 32 位之间")
    private String password;
}
