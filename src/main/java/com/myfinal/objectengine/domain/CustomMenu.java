package com.myfinal.objectengine.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 菜单配置表
 * @TableName custom_menu
 */
@TableName(value ="custom_menu")
@Data
public class CustomMenu {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父菜单ID，0表示顶级菜单
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单类型：DIRECTORY目录、OBJECT对象、LINK链接
     */
    private String menuType;

    /**
     * 关联对象API名称，menu_type=OBJECT时使用
     */
    private String objectApiName;

    /**
     * 前端路由地址
     */
    private String routePath;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 排序值，越小越靠前
     */
    private Integer sort;

    /**
     * 状态：0禁用，1启用
     */
    private Integer status;

    /**
     * 是否显示：0隐藏，1显示
     */
    private Integer visible;

    /**
     * 打开方式：_self当前窗口、_blank新窗口
     */
    private String target;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        CustomMenu other = (CustomMenu) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getParentId() == null ? other.getParentId() == null : this.getParentId().equals(other.getParentId()))
            && (this.getMenuName() == null ? other.getMenuName() == null : this.getMenuName().equals(other.getMenuName()))
            && (this.getMenuType() == null ? other.getMenuType() == null : this.getMenuType().equals(other.getMenuType()))
            && (this.getObjectApiName() == null ? other.getObjectApiName() == null : this.getObjectApiName().equals(other.getObjectApiName()))
            && (this.getRoutePath() == null ? other.getRoutePath() == null : this.getRoutePath().equals(other.getRoutePath()))
            && (this.getComponent() == null ? other.getComponent() == null : this.getComponent().equals(other.getComponent()))
            && (this.getIcon() == null ? other.getIcon() == null : this.getIcon().equals(other.getIcon()))
            && (this.getSort() == null ? other.getSort() == null : this.getSort().equals(other.getSort()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getVisible() == null ? other.getVisible() == null : this.getVisible().equals(other.getVisible()))
            && (this.getTarget() == null ? other.getTarget() == null : this.getTarget().equals(other.getTarget()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getParentId() == null) ? 0 : getParentId().hashCode());
        result = prime * result + ((getMenuName() == null) ? 0 : getMenuName().hashCode());
        result = prime * result + ((getMenuType() == null) ? 0 : getMenuType().hashCode());
        result = prime * result + ((getObjectApiName() == null) ? 0 : getObjectApiName().hashCode());
        result = prime * result + ((getRoutePath() == null) ? 0 : getRoutePath().hashCode());
        result = prime * result + ((getComponent() == null) ? 0 : getComponent().hashCode());
        result = prime * result + ((getIcon() == null) ? 0 : getIcon().hashCode());
        result = prime * result + ((getSort() == null) ? 0 : getSort().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getVisible() == null) ? 0 : getVisible().hashCode());
        result = prime * result + ((getTarget() == null) ? 0 : getTarget().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", parentId=").append(parentId);
        sb.append(", menuName=").append(menuName);
        sb.append(", menuType=").append(menuType);
        sb.append(", objectApiName=").append(objectApiName);
        sb.append(", routePath=").append(routePath);
        sb.append(", component=").append(component);
        sb.append(", icon=").append(icon);
        sb.append(", sort=").append(sort);
        sb.append(", status=").append(status);
        sb.append(", visible=").append(visible);
        sb.append(", target=").append(target);
        sb.append(", remark=").append(remark);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}