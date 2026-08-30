package com.myfinal.objectengine.vo;

import com.myfinal.objectengine.common.DateUtils;
import com.myfinal.objectengine.domain.SysUser;
import lombok.Data;

/**
 * 系统用户信息（不含密码，密码任何接口都不回传）
 */
@Data
public class SysUserVO {

    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private Integer status;
    private String remark;
    private String createdAt;
    private String updatedAt;

    public static SysUserVO from(SysUser entity) {
        SysUserVO vo = new SysUserVO();
        vo.setId(entity.getId());
        vo.setUsername(entity.getUsername());
        vo.setName(entity.getName());
        vo.setEmail(entity.getEmail());
        vo.setPhone(entity.getPhone());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(DateUtils.format(entity.getCreatedAt()));
        vo.setUpdatedAt(DateUtils.format(entity.getUpdatedAt()));
        return vo;
    }
}
