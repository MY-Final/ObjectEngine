package com.myfinal.objectengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myfinal.objectengine.common.BusinessException;
import com.myfinal.objectengine.common.JsonUtils;
import com.myfinal.objectengine.domain.CustomField;
import com.myfinal.objectengine.domain.CustomObject;
import com.myfinal.objectengine.dto.CreateFieldRequest;
import com.myfinal.objectengine.dto.UpdateFieldRequest;
import com.myfinal.objectengine.engine.FieldTypeRegistry;
import com.myfinal.objectengine.enums.FieldType;
import com.myfinal.objectengine.mapper.CustomFieldMapper;
import com.myfinal.objectengine.service.CustomFieldService;
import com.myfinal.objectengine.service.CustomObjectService;
import com.myfinal.objectengine.vo.FieldVO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CustomFieldServiceImpl extends ServiceImpl<CustomFieldMapper, CustomField>
    implements CustomFieldService {

    private final CustomObjectService customObjectService;

    public CustomFieldServiceImpl(CustomObjectService customObjectService) {
        this.customObjectService = customObjectService;
    }

    @Override
    public List<CustomField> listByObjectId(Long objectId) {
        return list(new LambdaQueryWrapper<CustomField>()
            .eq(CustomField::getObjectId, objectId)
            .orderByAsc(CustomField::getSort)
            .orderByAsc(CustomField::getId));
    }

    @Override
    public List<FieldVO> listByObjectApiName(String objectApiName) {
        CustomObject object = customObjectService.requireByApiName(objectApiName);
        return listByObjectId(object.getId()).stream().map(FieldVO::from).toList();
    }

    @Override
    public CustomField requireByApiName(Long objectId, String fieldApiName) {
        CustomField field = getOne(new LambdaQueryWrapper<CustomField>()
            .eq(CustomField::getObjectId, objectId)
            .eq(CustomField::getApiName, fieldApiName), false);
        if (field == null) {
            throw BusinessException.notFound("字段不存在：" + fieldApiName);
        }
        return field;
    }

    @Override
    public FieldVO create(String objectApiName, CreateFieldRequest request) {
        CustomObject object = customObjectService.requireByApiName(objectApiName);
        FieldType fieldType = FieldType.parse(request.getFieldType());
        if (fieldType == null) {
            throw BusinessException.badRequest("字段类型不合法：" + request.getFieldType());
        }
        long count = count(new LambdaQueryWrapper<CustomField>()
            .eq(CustomField::getObjectId, object.getId())
            .eq(CustomField::getApiName, request.getApiName()));
        if (count > 0) {
            throw BusinessException.badRequest("字段API名称已存在：" + request.getApiName());
        }
        FieldTypeRegistry.validateConfig(fieldType.name(), request.getFieldName(), request.getConfigJson());

        CustomField field = new CustomField();
        field.setObjectId(object.getId());
        field.setApiName(request.getApiName());
        field.setFieldName(request.getFieldName());
        field.setFieldType(fieldType.name());
        field.setDescription(request.getDescription());
        field.setRemark(request.getRemark());
        field.setRequiredFlag(defaultZero(request.getRequiredFlag()));
        field.setUniqueFlag(defaultZero(request.getUniqueFlag()));
        field.setSearchableFlag(defaultZero(request.getSearchableFlag()));
        field.setSortableFlag(defaultZero(request.getSortableFlag()));
        field.setSort(defaultZero(request.getSort()));
        field.setDefaultValue(request.getDefaultValue());
        field.setConfigJson(JsonUtils.write(request.getConfigJson()));
        field.setStatus(1);
        field.setCreatedAt(new Date());
        field.setUpdatedAt(new Date());
        save(field);
        return FieldVO.from(field);
    }

    @Override
    public FieldVO update(String objectApiName, String fieldApiName, UpdateFieldRequest request) {
        CustomObject object = customObjectService.requireByApiName(objectApiName);
        CustomField field = requireByApiName(object.getId(), fieldApiName);
        if (request.getFieldName() != null) {
            field.setFieldName(request.getFieldName());
        }
        if (request.getDescription() != null) {
            field.setDescription(request.getDescription());
        }
        if (request.getRemark() != null) {
            field.setRemark(request.getRemark());
        }
        if (request.getRequiredFlag() != null) {
            field.setRequiredFlag(request.getRequiredFlag());
        }
        if (request.getUniqueFlag() != null) {
            field.setUniqueFlag(request.getUniqueFlag());
        }
        if (request.getSearchableFlag() != null) {
            field.setSearchableFlag(request.getSearchableFlag());
        }
        if (request.getSortableFlag() != null) {
            field.setSortableFlag(request.getSortableFlag());
        }
        if (request.getSort() != null) {
            field.setSort(request.getSort());
        }
        if (request.getDefaultValue() != null) {
            field.setDefaultValue(request.getDefaultValue());
        }
        if (request.getStatus() != null) {
            field.setStatus(request.getStatus());
        }
        if (request.getConfigJson() != null) {
            FieldTypeRegistry.validateConfig(field.getFieldType(), field.getFieldName(), request.getConfigJson());
            field.setConfigJson(JsonUtils.write(request.getConfigJson()));
        }
        field.setUpdatedAt(new Date());
        updateById(field);
        return FieldVO.from(field);
    }

    @Override
    public void delete(String objectApiName, String fieldApiName) {
        CustomObject object = customObjectService.requireByApiName(objectApiName);
        CustomField field = requireByApiName(object.getId(), fieldApiName);
        removeById(field.getId());
    }

    private static Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }
}
