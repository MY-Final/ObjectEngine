package com.myfinal.objectengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myfinal.objectengine.common.BusinessException;
import com.myfinal.objectengine.common.PageResult;
import com.myfinal.objectengine.domain.CustomField;
import com.myfinal.objectengine.domain.CustomObject;
import com.myfinal.objectengine.domain.CustomRecord;
import com.myfinal.objectengine.dto.CreateObjectRequest;
import com.myfinal.objectengine.dto.UpdateObjectRequest;
import com.myfinal.objectengine.mapper.CustomFieldMapper;
import com.myfinal.objectengine.mapper.CustomObjectMapper;
import com.myfinal.objectengine.mapper.CustomRecordMapper;
import com.myfinal.objectengine.service.CustomObjectService;
import com.myfinal.objectengine.vo.ObjectVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class CustomObjectServiceImpl extends ServiceImpl<CustomObjectMapper, CustomObject>
    implements CustomObjectService {

    private final CustomFieldMapper customFieldMapper;
    private final CustomRecordMapper customRecordMapper;

    public CustomObjectServiceImpl(CustomFieldMapper customFieldMapper, CustomRecordMapper customRecordMapper) {
        this.customFieldMapper = customFieldMapper;
        this.customRecordMapper = customRecordMapper;
    }

    @Override
    public CustomObject requireByApiName(String apiName) {
        CustomObject object = getOne(new LambdaQueryWrapper<CustomObject>().eq(CustomObject::getApiName, apiName), false);
        if (object == null) {
            throw BusinessException.notFound("对象不存在：" + apiName);
        }
        return object;
    }

    @Override
    public PageResult<ObjectVO> page(String keyword, Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 20 : pageSize;
        LambdaQueryWrapper<CustomObject> wrapper = new LambdaQueryWrapper<CustomObject>()
            .and(keyword != null && !keyword.isBlank(),
                q -> q.like(CustomObject::getObjectName, keyword).or().like(CustomObject::getApiName, keyword))
            .orderByAsc(CustomObject::getSort)
            .orderByAsc(CustomObject::getId);
        Page<CustomObject> result = this.page(new Page<>(p, ps), wrapper);
        List<ObjectVO> records = result.getRecords().stream().map(ObjectVO::from).toList();
        return PageResult.of(records, result.getTotal(), p, ps);
    }

    @Override
    public ObjectVO create(CreateObjectRequest request) {
        long count = count(new LambdaQueryWrapper<CustomObject>().eq(CustomObject::getApiName, request.getApiName()));
        if (count > 0) {
            throw BusinessException.badRequest("对象API名称已存在：" + request.getApiName());
        }
        CustomObject object = new CustomObject();
        object.setApiName(request.getApiName());
        object.setObjectName(request.getObjectName());
        object.setDescription(request.getDescription());
        object.setRemark(request.getRemark());
        object.setIcon(request.getIcon());
        object.setSort(request.getSort() == null ? 0 : request.getSort());
        object.setStatus(1);
        object.setCreatedAt(new Date());
        object.setUpdatedAt(new Date());
        save(object);
        return ObjectVO.from(object);
    }

    @Override
    public ObjectVO update(String apiName, UpdateObjectRequest request) {
        CustomObject object = requireByApiName(apiName);
        if (request.getObjectName() != null) {
            object.setObjectName(request.getObjectName());
        }
        if (request.getDescription() != null) {
            object.setDescription(request.getDescription());
        }
        if (request.getRemark() != null) {
            object.setRemark(request.getRemark());
        }
        if (request.getIcon() != null) {
            object.setIcon(request.getIcon());
        }
        if (request.getSort() != null) {
            object.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            object.setStatus(request.getStatus());
        }
        object.setUpdatedAt(new Date());
        updateById(object);
        return ObjectVO.from(object);
    }

    /**
     * 数据库无外键，按 字段 → 记录 → 对象 的顺序在事务内级联删除
     */
    @Override
    @Transactional
    public void deleteByApiName(String apiName) {
        CustomObject object = requireByApiName(apiName);
        customFieldMapper.delete(new LambdaQueryWrapper<CustomField>().eq(CustomField::getObjectId, object.getId()));
        customRecordMapper.delete(new LambdaQueryWrapper<CustomRecord>().eq(CustomRecord::getObjectId, object.getId()));
        removeById(object.getId());
    }
}
