package com.myfinal.objectengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myfinal.objectengine.domain.CustomSequence;
import org.apache.ibatis.annotations.Mapper;

/**
 * 自动编号序列 Mapper
 */
@Mapper
public interface CustomSequenceMapper extends BaseMapper<CustomSequence> {
}
