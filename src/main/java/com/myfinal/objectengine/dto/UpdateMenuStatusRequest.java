package com.myfinal.objectengine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 启用/禁用菜单请求
 */
@Data
public class UpdateMenuStatusRequest {

    /**
     * 状态：0禁用，1启用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
