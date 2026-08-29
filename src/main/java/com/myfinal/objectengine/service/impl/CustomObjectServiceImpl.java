package com.myfinal.objectengine.service.impl;

import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import com.myfinal.objectengine.domain.CustomObject;
import com.myfinal.objectengine.service.CustomObjectService;
import com.myfinal.objectengine.mapper.CustomObjectMapper;
import org.springframework.stereotype.Service;

/**
* @author 34861
* @description 针对表【custom_object(自定义对象)】的数据库操作Service实现
* @createDate 2026-08-29 18:18:21
*/
@Service
public class CustomObjectServiceImpl extends CrudRepository<CustomObjectMapper, CustomObject>
    implements CustomObjectService{

}




