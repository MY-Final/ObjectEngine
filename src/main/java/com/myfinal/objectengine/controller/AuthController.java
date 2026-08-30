package com.myfinal.objectengine.controller;

import com.myfinal.objectengine.common.ApiResponse;
import com.myfinal.objectengine.domain.SysUser;
import com.myfinal.objectengine.dto.LoginRequest;
import com.myfinal.objectengine.service.SysUserService;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 认证 API：登录 / 退出 / 当前用户。
 * 登录基于 Sa-Token，token 通过 Authorization 请求头回传
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        SysUser user = sysUserService.login(request.getUsername(), request.getPassword());
        StpUtil.login(user.getId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", StpUtil.getTokenValue());
        result.put("user", userInfo(user));
        return ApiResponse.ok(result, "登录成功");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        StpUtil.logout();
        return ApiResponse.ok(null, "已退出登录");
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return ApiResponse.error(401, "用户不存在");
        }
        return ApiResponse.ok(userInfo(user));
    }

    private Map<String, Object> userInfo(SysUser user) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("name", user.getName());
        return info;
    }
}
