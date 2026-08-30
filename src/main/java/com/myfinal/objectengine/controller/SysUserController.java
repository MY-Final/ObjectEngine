package com.myfinal.objectengine.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.myfinal.objectengine.common.ApiResponse;
import com.myfinal.objectengine.common.PageResult;
import com.myfinal.objectengine.dto.CreateSysUserRequest;
import com.myfinal.objectengine.dto.ResetUserPasswordRequest;
import com.myfinal.objectengine.dto.SysUserQueryRequest;
import com.myfinal.objectengine.dto.UpdateSysUserRequest;
import com.myfinal.objectengine.dto.UpdateUserStatusRequest;
import com.myfinal.objectengine.service.SysUserService;
import com.myfinal.objectengine.vo.SysUserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统用户管理 API：账号 CRUD / 启停 / 重置密码（角色分配待 RBAC 第二步）
 */
@RestController
@RequestMapping("/api/v1/sys-users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @GetMapping
    public ApiResponse<PageResult<SysUserVO>> page(SysUserQueryRequest query) {
        return ApiResponse.ok(sysUserService.page(query));
    }

    @PostMapping
    public ApiResponse<SysUserVO> create(@Valid @RequestBody CreateSysUserRequest request) {
        return ApiResponse.ok(sysUserService.create(request), "用户创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<SysUserVO> update(@PathVariable Long id, @Valid @RequestBody UpdateSysUserRequest request) {
        return ApiResponse.ok(sysUserService.update(id, request), "用户修改成功");
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
        long operatorId = StpUtil.getLoginIdAsLong();
        sysUserService.updateStatus(id, request.getStatus(), operatorId);
        return ApiResponse.ok(null, request.getStatus() == 1 ? "已启用" : "已停用");
    }

    @PutMapping("/{id}/password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetUserPasswordRequest request) {
        sysUserService.resetPassword(id, request.getPassword());
        return ApiResponse.ok(null, "密码已重置");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        long operatorId = StpUtil.getLoginIdAsLong();
        sysUserService.delete(id, operatorId);
        return ApiResponse.ok(null, "用户删除成功");
    }
}
