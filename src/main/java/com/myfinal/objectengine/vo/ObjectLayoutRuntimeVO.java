package com.myfinal.objectengine.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Runtime 布局响应：Layout 决定字段如何排列，字段定义仍来自 Field Metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectLayoutRuntimeVO {

    private ObjectVO object;

    /**
     * 布局配置：{ layoutType, sections }，无布局时为后端生成的默认布局
     */
    private Object layout;

    private List<FieldVO> fields;
}
