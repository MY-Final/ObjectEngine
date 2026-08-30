package com.myfinal.objectengine.service;

import com.myfinal.objectengine.domain.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
