package com.myfinal.objectengine.vo;

import com.myfinal.objectengine.domain.CustomMenu;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点（前台导航）
 */
@Data
public class MenuTreeVO {

    private Long id;
    private Long parentId;
    private String menuName;
    private String menuType;
    private String objectApiName;
    private String routePath;
    private String icon;
    private Integer sort;
    private List<MenuTreeVO> children = new ArrayList<>();

    public static MenuTreeVO from(CustomMenu entity) {
        MenuTreeVO vo = new MenuTreeVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setMenuName(entity.getMenuName());
        vo.setMenuType(entity.getMenuType());
        vo.setObjectApiName(entity.getObjectApiName());
        vo.setRoutePath(entity.getRoutePath());
        vo.setIcon(entity.getIcon());
        vo.setSort(entity.getSort());
        return vo;
    }
}
