package com.myfinal.objectengine.config;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限数据源：返回当前账号拥有的角色与权限点。
 * 角色表（sys_role / sys_user_role）落地后在此查询返回；当前阶段返回空集合
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // TODO RBAC 第二步：按 loginId 从角色表读取
        return List.of();
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // TODO RBAC 第二步：按 loginId 从权限表读取
        return List.of();
    }
}
