package com.myfinal.objectengine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单排序请求
 */
@Data
public class UpdateMenuSortRequest {

    /**
     * 排序值，越小越靠前
     */
    @NotNull(message = "排序值不能为空")
    private Integer sort;
}
