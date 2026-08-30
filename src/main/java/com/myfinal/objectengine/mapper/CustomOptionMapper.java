package com.myfinal.objectengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myfinal.objectengine.domain.CustomOption;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通用选项 Mapper
 */
@Mapper
public interface CustomOptionMapper extends BaseMapper<CustomOption> {
}
