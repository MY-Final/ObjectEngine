package com.myfinal.objectengine.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 完整 Metadata：前端动态页面唯一依赖
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectMetadataVO {

    private ObjectVO object;

    private List<FieldVO> fields;
}
