package com.myfinal.objectengine.dto;

import lombok.Data;

/**
 * 菜单列表查询参数
 */
@Data
public class MenuQueryRequest {

    /**
     * 菜单名称，模糊匹配
     */
    private String menuName;

    /**
     * 菜单类型：DIRECTORY/OBJECT/LINK，精确匹配
     */
    private String menuType;

    /**
     * 状态：0禁用，1启用
     */
    private Integer status;

    /**
     * 是否显示：0隐藏，1显示
     */
    private Integer visible;
}
