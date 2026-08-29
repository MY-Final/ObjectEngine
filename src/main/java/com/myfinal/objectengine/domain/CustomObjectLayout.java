package com.myfinal.objectengine.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 自定义对象布局配置
 * <p>
 * Field Metadata 决定字段是什么（What），Layout Metadata 决定字段放在哪里（Where），
 * 两者通过 object_api_name 关联，无外键。
 */
@Data
@TableName(value = "custom_object_layout")
public class CustomObjectLayout {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 对象API名称
     */
    private String objectApiName;

    /**
     * 布局名称
     */
    private String layoutName;

    /**
     * 布局类型：FRONTEND/BACKEND
     */
    private String layoutType;

    /**
     * 布局配置JSON：{ sections: [ { id, title, sort, columns, fields: [ { fieldApiName, span, sort } ] } ] }
     */
    private Object configJson;

    /**
     * 状态：0停用，1启用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

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
}
