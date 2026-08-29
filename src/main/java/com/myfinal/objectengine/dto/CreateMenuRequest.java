package com.myfinal.objectengine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增菜单请求
 */
@Data
public class CreateMenuRequest {

    /**
     * 父菜单ID，0 表示顶级菜单
     */
    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    /**
     * 菜单类型：DIRECTORY/OBJECT/LINK
     */
    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    /**
     * 关联对象API名称，menuType=OBJECT 时必填且必须存在
     */
    private String objectApiName;

    /**
     * 前端路由地址，OBJECT 类型不传时自动生成为 /custom/{objectApiName}，LINK 类型必填
     */
    private String routePath;

    private String component;

    private String icon;

    private Integer sort;

    private Integer status;

    private Integer visible;

    private String target;

    private String remark;
}
