package com.myfinal.objectengine.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myfinal.objectengine.domain.CustomField;
import com.myfinal.objectengine.service.CustomFieldService;
import com.myfinal.objectengine.mapper.CustomFieldMapper;
import org.springframework.stereotype.Service;

/**
* @author 34861
* @description 针对表【custom_field(自定义对象字段)】的数据库操作Service实现
* @createDate 2026-08-29 19:32:28
*/
@Service
public class CustomFieldServiceImpl extends ServiceImpl<CustomFieldMapper, CustomField>
    implements CustomFieldService{

}




