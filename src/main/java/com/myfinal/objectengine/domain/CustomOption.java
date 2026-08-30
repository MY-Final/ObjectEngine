package com.myfinal.objectengine.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 通用选项
 * @TableName custom_option
 */
@TableName(value = "custom_option")
@Data
public class CustomOption {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属选项集ID
     */
    private Long optionSetId;

    /**
     * 选项显示名称
     */
    private String label;

    /**
     * 选项实际值，同一选项集内唯一
     */
    private String value;

    /**
     * 排序值，越小越靠前
     */
    private Integer sort;

    /**
     * 状态：0禁用，1启用
     */
    private Integer status;

    /**
     * 是否默认选项：0否，1是
     */
    private Integer isDefault;

    /**
     * 选项描述
     */
    private String description;

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
