package com.myfinal.objectengine.vo;

import com.myfinal.objectengine.common.DateUtils;
import com.myfinal.objectengine.domain.CustomMenu;
import lombok.Data;

/**
 * 菜单完整信息（后台管理端）
 */
@Data
public class MenuVO {

    private Long id;
    private Long parentId;
    private String menuName;
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
    private String createdAt;
    private String updatedAt;

    /**
     * 关联对象状态（仅 OBJECT 菜单有值）：0停用 1启用，对象已删除时为 null。
     * 对象菜单的有效状态由它派生，菜单管理侧不允许单独启停
     */
    private Integer objectStatus;

    public static MenuVO from(CustomMenu entity) {
        MenuVO vo = new MenuVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setMenuName(entity.getMenuName());
        vo.setMenuType(entity.getMenuType());
        vo.setObjectApiName(entity.getObjectApiName());
        vo.setRoutePath(entity.getRoutePath());
        vo.setComponent(entity.getComponent());
        vo.setIcon(entity.getIcon());
        vo.setSort(entity.getSort());
        vo.setStatus(entity.getStatus());
        vo.setVisible(entity.getVisible());
        vo.setTarget(entity.getTarget());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(DateUtils.format(entity.getCreatedAt()));
        vo.setUpdatedAt(DateUtils.format(entity.getUpdatedAt()));
        return vo;
    }
}
