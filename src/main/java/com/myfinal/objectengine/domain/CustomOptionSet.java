package com.myfinal.objectengine.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 通用选项集
 * @TableName custom_option_set
 */
@TableName(value = "custom_option_set")
@Data
public class CustomOptionSet {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 选项集名称
     */
    private String name;

    /**
     * 选项集API名称，全局唯一
     */
    private String apiName;

    /**
     * 选项集描述
     */
    private String description;

    /**
     * 状态：0禁用，1启用
     */
    private Integer status;

    /**
     * 排序值
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
