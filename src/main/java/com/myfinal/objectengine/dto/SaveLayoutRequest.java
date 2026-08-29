package com.myfinal.objectengine.dto;

import lombok.Data;

/**
 * 保存布局请求（POST 创建 / PUT 按 layoutType 更新均可使用）
 */
@Data
public class SaveLayoutRequest {

    /**
     * 布局名称，不传时按布局类型生成默认名称
     */
    private String layoutName;

    /**
     * 布局类型：FRONTEND/BACKEND，POST 时必填，PUT 以路径参数为准
     */
    private String layoutType;

    /**
     * 布局配置：{ sections: [ { id, title, sort, columns, fields: [ { fieldApiName, span, sort } ] } ] }
     */
    private Object configJson;

    /**
     * 状态：0停用，1启用，默认 1
     */
    private Integer status;

    private Integer sort;

    private String remark;
}
