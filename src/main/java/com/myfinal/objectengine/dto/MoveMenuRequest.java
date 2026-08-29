package com.myfinal.objectengine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 移动菜单请求：把菜单移动到目标父菜单下并设置排序
 */
@Data
public class MoveMenuRequest {

    /**
     * 目标父菜单ID，0 表示移动到顶级
     */
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    @NotNull(message = "排序值不能为空")
    private Integer sort;
}
