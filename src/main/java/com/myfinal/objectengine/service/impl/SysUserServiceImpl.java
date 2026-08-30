package com.myfinal.objectengine.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myfinal.objectengine.common.BusinessException;
import com.myfinal.objectengine.common.PageResult;
import com.myfinal.objectengine.domain.SysUser;
import com.myfinal.objectengine.dto.CreateSysUserRequest;
import com.myfinal.objectengine.dto.SysUserQueryRequest;
import com.myfinal.objectengine.dto.UpdateSysUserRequest;
import com.myfinal.objectengine.mapper.SysUserMapper;
import com.myfinal.objectengine.service.SysUserService;
import com.myfinal.objectengine.vo.SysUserVO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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

    @Override
    public PageResult<SysUserVO> page(SysUserQueryRequest query) {
        int p = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int ps = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : query.getPageSize();
        String keyword = query.getKeyword();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
            .and(keyword != null && !keyword.isBlank(),
                q -> q.like(SysUser::getUsername, keyword).or().like(SysUser::getName, keyword))
            .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
            .orderByAsc(SysUser::getId);
        Page<SysUser> result = page(new Page<>(p, ps), wrapper);
        List<SysUserVO> records = result.getRecords().stream().map(SysUserVO::from).toList();
        return PageResult.of(records, result.getTotal(), p, ps);
    }

    @Override
    public SysUserVO create(CreateSysUserRequest request) {
        long count = count(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, request.getUsername()));
        if (count > 0) {
            throw BusinessException.badRequest("用户名已存在：" + request.getUsername());
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(BCrypt.hashpw(request.getPassword()));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRemark(request.getRemark());
        user.setStatus(1);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        save(user);
        return SysUserVO.from(user);
    }

    @Override
    public SysUserVO update(Long id, UpdateSysUserRequest request) {
        SysUser user = requireById(id);
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getRemark() != null) {
            user.setRemark(request.getRemark());
        }
        user.setUpdatedAt(new Date());
        updateById(user);
        return SysUserVO.from(user);
    }

    @Override
    public void updateStatus(Long id, Integer status, Long operatorId) {
        checkNotSelf(id, operatorId, "不能修改自己的状态");
        SysUser user = requireById(id);
        user.setStatus(status);
        user.setUpdatedAt(new Date());
        updateById(user);
    }

    @Override
    public void resetPassword(Long id, String password) {
        SysUser user = requireById(id);
        user.setPassword(BCrypt.hashpw(password));
        user.setUpdatedAt(new Date());
        updateById(user);
    }

    @Override
    public void delete(Long id, Long operatorId) {
        checkNotSelf(id, operatorId, "不能删除自己的账号");
        requireById(id);
        removeById(id);
    }

    private SysUser requireById(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在：" + id);
        }
        return user;
    }

    private void checkNotSelf(Long id, Long operatorId, String message) {
        if (id.equals(operatorId)) {
            throw BusinessException.badRequest(message);
        }
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
