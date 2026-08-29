package com.myfinal.objectengine.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myfinal.objectengine.domain.CustomRecord;
import com.myfinal.objectengine.service.CustomRecordService;
import com.myfinal.objectengine.mapper.CustomRecordMapper;
import org.springframework.stereotype.Service;

/**
* @author 34861
* @description 针对表【custom_record(自定义对象数据)】的数据库操作Service实现
* @createDate 2026-08-29 19:32:28
*/
@Service
public class CustomRecordServiceImpl extends ServiceImpl<CustomRecordMapper, CustomRecord>
    implements CustomRecordService{

}




