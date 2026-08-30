package com.myfinal.objectengine.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myfinal.objectengine.common.BusinessException;
import com.myfinal.objectengine.common.JsonUtils;
import com.myfinal.objectengine.domain.CustomField;
import com.myfinal.objectengine.domain.CustomObject;
import com.myfinal.objectengine.domain.CustomOption;
import com.myfinal.objectengine.domain.CustomOptionSet;
import com.myfinal.objectengine.dto.CreateFieldRequest;
import com.myfinal.objectengine.dto.UpdateFieldRequest;
import com.myfinal.objectengine.engine.FieldTypeRegistry;
import com.myfinal.objectengine.enums.FieldType;
import com.myfinal.objectengine.mapper.CustomFieldMapper;
import com.myfinal.objectengine.mapper.CustomOptionMapper;
import com.myfinal.objectengine.mapper.CustomOptionSetMapper;
import com.myfinal.objectengine.service.CustomFieldService;
import com.myfinal.objectengine.service.CustomObjectService;
import com.myfinal.objectengine.vo.FieldVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomFieldServiceImpl extends ServiceImpl<CustomFieldMapper, CustomField>
    implements CustomFieldService {

    private static final String OPTION_SOURCE_LOCAL = "LOCAL";
    private static final String OPTION_SOURCE_GLOBAL = "GLOBAL";

    private final CustomObjectService customObjectService;
    private final CustomOptionSetMapper customOptionSetMapper;
    private final CustomOptionMapper customOptionMapper;

    public CustomFieldServiceImpl(CustomObjectService customObjectService,
                                  CustomOptionSetMapper customOptionSetMapper,
                                  CustomOptionMapper customOptionMapper) {
        this.customObjectService = customObjectService;
        this.customOptionSetMapper = customOptionSetMapper;
        this.customOptionMapper = customOptionMapper;
    }

    @Override
    public List<CustomField> listByObjectId(Long objectId) {
        List<CustomField> fields = list(new LambdaQueryWrapper<CustomField>()
            .eq(CustomField::getObjectId, objectId)
            .orderByAsc(CustomField::getSort)
            .orderByAsc(CustomField::getId));
        enrichGlobalOptions(fields);
        return fields;
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
        Long optionSetId = normalizeOptionSetId(fieldType.name(), request.getOptionSource(),
            request.getOptionSetId());
        FieldTypeRegistry.validateConfig(fieldType.name(), request.getFieldName(), request.getConfigJson(),
            optionSetId == null);

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
        field.setOptionSource(optionSetId != null ? OPTION_SOURCE_GLOBAL : OPTION_SOURCE_LOCAL);
        field.setOptionSetId(optionSetId);
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
        // 选项来源重新归一化（只传其一则与现值合并）；GLOBAL 时不强制字段自带 options
        if (request.getOptionSource() != null || request.getOptionSetId() != null) {
            String source = request.getOptionSource() != null && !request.getOptionSource().isBlank()
                ? request.getOptionSource()
                : field.getOptionSource();
            Long requestedSetId = request.getOptionSetId() != null
                ? request.getOptionSetId()
                : field.getOptionSetId();
            Long optionSetId = normalizeOptionSetId(field.getFieldType(), source, requestedSetId);
            field.setOptionSource(optionSetId != null ? OPTION_SOURCE_GLOBAL : OPTION_SOURCE_LOCAL);
            field.setOptionSetId(optionSetId);
        }
        boolean enforceOptions = !OPTION_SOURCE_GLOBAL.equals(field.getOptionSource());
        if (request.getConfigJson() != null) {
            FieldTypeRegistry.validateConfig(field.getFieldType(), field.getFieldName(), request.getConfigJson(),
                enforceOptions);
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

    /**
     * 校验并归一化选项来源：仅选择类字段支持；GLOBAL 必须关联启用中的选项集。
     * 返回应写入的 optionSetId（LOCAL 返回 null）
     */
    private Long normalizeOptionSetId(String fieldType, String optionSource, Long optionSetId) {
        boolean optionType = FieldType.SELECT.name().equals(fieldType)
            || FieldType.MULTI_SELECT.name().equals(fieldType);
        String source = optionSource == null || optionSource.isBlank()
            ? OPTION_SOURCE_LOCAL
            : optionSource.trim().toUpperCase();
        if (!OPTION_SOURCE_LOCAL.equals(source) && !OPTION_SOURCE_GLOBAL.equals(source)) {
            throw BusinessException.badRequest("选项来源不合法：" + optionSource);
        }
        if (!optionType) {
            if (optionSetId != null) {
                throw BusinessException.badRequest("仅选择类字段支持选项来源配置");
            }
            return null;
        }
        if (OPTION_SOURCE_GLOBAL.equals(source)) {
            if (optionSetId == null) {
                throw BusinessException.badRequest("引用通用选项集时必须选择选项集");
            }
            CustomOptionSet set = customOptionSetMapper.selectById(optionSetId);
            if (set == null) {
                throw BusinessException.badRequest("选项集不存在：" + optionSetId);
            }
            if (!Integer.valueOf(1).equals(set.getStatus())) {
                throw BusinessException.badRequest("选项集已停用：" + set.getName());
            }
            return optionSetId;
        }
        if (optionSetId != null) {
            throw BusinessException.badRequest("自定义选项来源不能关联选项集");
        }
        return null;
    }

    /**
     * GLOBAL 选择类字段把启用中的选项集选项注入 configJson.options：
     * 记录校验（FieldTypeRegistry）与前端动态渲染统一读取字段自身配置，运行时无需感知选项来源。
     * 选项集被停用或无启用选项时不注入，字段呈现为「未配置选项」
     */
    private void enrichGlobalOptions(List<CustomField> fields) {
        List<Long> setIds = fields.stream()
            .filter(field -> OPTION_SOURCE_GLOBAL.equals(field.getOptionSource()) && field.getOptionSetId() != null)
            .map(CustomField::getOptionSetId)
            .distinct()
            .toList();
        if (setIds.isEmpty()) {
            return;
        }
        Set<Long> enabledSetIds = customOptionSetMapper.selectBatchIds(setIds).stream()
            .filter(set -> Integer.valueOf(1).equals(set.getStatus()))
            .map(CustomOptionSet::getId)
            .collect(Collectors.toSet());
        if (enabledSetIds.isEmpty()) {
            return;
        }
        List<CustomOption> options = customOptionMapper.selectList(new LambdaQueryWrapper<CustomOption>()
            .in(CustomOption::getOptionSetId, enabledSetIds)
            .eq(CustomOption::getStatus, 1)
            .orderByAsc(CustomOption::getSort)
            .orderByAsc(CustomOption::getId));
        Map<Long, List<CustomOption>> optionsBySet = new HashMap<>();
        for (CustomOption option : options) {
            optionsBySet.computeIfAbsent(option.getOptionSetId(), key -> new ArrayList<>()).add(option);
        }
        for (CustomField field : fields) {
            if (!OPTION_SOURCE_GLOBAL.equals(field.getOptionSource()) || field.getOptionSetId() == null) {
                continue;
            }
            List<CustomOption> setOptions = optionsBySet.get(field.getOptionSetId());
            if (setOptions == null || setOptions.isEmpty()) {
                continue;
            }
            JSONObject config = parseConfig(field.getConfigJson());
            JSONArray optionArray = new JSONArray();
            for (CustomOption option : setOptions) {
                JSONObject item = new JSONObject();
                item.put("label", option.getLabel());
                item.put("value", option.getValue());
                optionArray.add(item);
            }
            config.put("options", optionArray);
            field.setConfigJson(config.toJSONString());
        }
    }

    /** configJson 统一解析为 JSONObject，空值返回空对象 */
    private static JSONObject parseConfig(Object configJson) {
        if (configJson == null) {
            return new JSONObject();
        }
        if (configJson instanceof String s) {
            return s.isBlank() ? new JSONObject() : JSON.parseObject(s);
        }
        return JSON.parseObject(JSON.toJSONString(configJson));
    }

    private static Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }
}
