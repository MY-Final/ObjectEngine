package com.myfinal.objectengine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 编辑菜单请求（全量更新，id 由路径传入）
 */
@Data
public class UpdateMenuRequest {

    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    private String objectApiName;

    private String routePath;

    private String component;

    private String icon;

    private Integer sort;

    private Integer status;

    private Integer visible;

    private String target;

    private String remark;
}
