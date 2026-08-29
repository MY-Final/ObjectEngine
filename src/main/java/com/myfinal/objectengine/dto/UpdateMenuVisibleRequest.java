package com.myfinal.objectengine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 显示/隐藏菜单请求
 */
@Data
public class UpdateMenuVisibleRequest {

    /**
     * 是否显示：0隐藏，1显示
     */
    @NotNull(message = "显示状态不能为空")
    private Integer visible;
}
