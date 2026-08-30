package com.myfinal.objectengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myfinal.objectengine.common.PageResult;
import com.myfinal.objectengine.domain.SysUser;
import com.myfinal.objectengine.dto.CreateSysUserRequest;
import com.myfinal.objectengine.dto.SysUserQueryRequest;
import com.myfinal.objectengine.dto.UpdateSysUserRequest;
import com.myfinal.objectengine.vo.SysUserVO;

/**
* @author 34861
* @description 针对表【sys_user(系统用户)】的数据库操作Service
* @createDate 2026-08-30 18:46:35
*/
public interface SysUserService extends IService<SysUser> {

    /**
     * 登录校验：账号存在、密码匹配（BCrypt）、账号启用；任一不满足抛业务异常
     */
    SysUser login(String username, String password);

    PageResult<SysUserVO> page(SysUserQueryRequest query);

    SysUserVO create(CreateSysUserRequest request);

    SysUserVO update(Long id, UpdateSysUserRequest request);

    /**
     * 启用/停用用户；operatorId 为当前登录管理员，不能操作自己
     */
    void updateStatus(Long id, Integer status, Long operatorId);

    /**
     * 重置密码（BCrypt 入库）；允许管理员重置自己的密码
     */
    void resetPassword(Long id, String password);

    /**
     * 删除用户；operatorId 为当前登录管理员，不能删除自己
     */
    void delete(Long id, Long operatorId);
}
