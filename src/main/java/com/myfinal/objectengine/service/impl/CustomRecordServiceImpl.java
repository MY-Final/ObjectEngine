package com.myfinal.objectengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myfinal.objectengine.common.BusinessException;
import com.myfinal.objectengine.common.JsonUtils;
import com.myfinal.objectengine.common.PageResult;
import com.myfinal.objectengine.domain.CustomField;
import com.myfinal.objectengine.domain.CustomObject;
import com.myfinal.objectengine.domain.CustomRecord;
import com.myfinal.objectengine.engine.RecordValidator;
import com.myfinal.objectengine.mapper.CustomRecordMapper;
import com.myfinal.objectengine.service.CustomFieldService;
import com.myfinal.objectengine.service.CustomObjectService;
import com.myfinal.objectengine.service.CustomRecordService;
import com.myfinal.objectengine.vo.RecordVO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class CustomRecordServiceImpl extends ServiceImpl<CustomRecordMapper, CustomRecord>
    implements CustomRecordService {

    private final CustomObjectService customObjectService;
    private final CustomFieldService customFieldService;

    public CustomRecordServiceImpl(CustomObjectService customObjectService, CustomFieldService customFieldService) {
        this.customObjectService = customObjectService;
        this.customFieldService = customFieldService;
    }

    @Override
    public RecordVO create(String objectApiName, Map<String, Object> body) {
        CustomObject object = customObjectService.requireByApiName(objectApiName);
        List<CustomField> fields = customFieldService.listByObjectId(object.getId());
        RecordValidator.validate(fields, body);

        CustomRecord record = new CustomRecord();
        record.setObjectId(object.getId());
        record.setDataJson(JsonUtils.write(body));
        record.setCreatedAt(new Date());
        record.setUpdatedAt(new Date());
        save(record);
        return RecordVO.from(record);
    }

    @Override
    public PageResult<RecordVO> page(String objectApiName, Integer page, Integer pageSize) {
        CustomObject object = customObjectService.requireByApiName(objectApiName);
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 20 : pageSize;
        Page<CustomRecord> result = page(new Page<>(p, ps),
            new LambdaQueryWrapper<CustomRecord>()
                .eq(CustomRecord::getObjectId, object.getId())
                .orderByDesc(CustomRecord::getId));
        List<RecordVO> records = result.getRecords().stream().map(RecordVO::from).toList();
        return PageResult.of(records, result.getTotal(), p, ps);
    }

    @Override
    public RecordVO get(String objectApiName, Long id) {
        return RecordVO.from(requireRecord(objectApiName, id));
    }

    @Override
    public void delete(String objectApiName, Long id) {
        CustomRecord record = requireRecord(objectApiName, id);
        removeById(record.getId());
    }

    private CustomRecord requireRecord(String objectApiName, Long id) {
        CustomObject object = customObjectService.requireByApiName(objectApiName);
        CustomRecord record = getOne(new LambdaQueryWrapper<CustomRecord>()
            .eq(CustomRecord::getObjectId, object.getId())
            .eq(CustomRecord::getId, id), false);
        if (record == null) {
            throw BusinessException.notFound("记录不存在：" + id);
        }
        return record;
    }
}
