package com.myfinal.objectengine.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myfinal.objectengine.common.BusinessException;
import com.myfinal.objectengine.domain.SysUser;
import com.myfinal.objectengine.service.SysUserService;
import com.myfinal.objectengine.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

/**
* @author 34861
* @description 针对表【sys_user(系统用户)】的数据库操作Service实现
* @createDate 2026-08-30 18:46:35
*/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
    implements SysUserService{

    @Override
    public SysUser login(String username, String password) {
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, username), false);
        // 账号不存在与密码错误统一提示，避免暴露账号是否存在
        if (user == null || !passwordMatches(password, user.getPassword())) {
            throw BusinessException.badRequest("账号或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw BusinessException.badRequest("账号已禁用，请联系管理员");
        }
        return user;
    }

    /** 密码为 BCrypt 摘要；摘要格式异常（历史脏数据）按密码错误处理 */
    private boolean passwordMatches(String rawPassword, String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
