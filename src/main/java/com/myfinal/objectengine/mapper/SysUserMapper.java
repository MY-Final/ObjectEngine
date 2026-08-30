package com.myfinal.objectengine.mapper;

import com.myfinal.objectengine.domain.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 34861
* @description 针对表【sys_user(系统用户)】的数据库操作Mapper
* @createDate 2026-08-30 18:46:35
* @Entity com.myfinal.objectengine.domain.SysUser
*/
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

}




