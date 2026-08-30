package com.myfinal.objectengine.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 自动编号序列：每个 AUTO_NUMBER 字段一行，保存已用到的最大序号。
 * 取号通过 UPDATE current_value = current_value + 1 行级锁原子递增，避免并发重号
 * @TableName custom_sequence
 */
@TableName(value = "custom_sequence")
@Data
public class CustomSequence {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 自动编号字段ID，全局唯一
     */
    private Long fieldId;

    /**
     * 当前已用序号
     */
    private Long currentValue;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
