package com.myfinal.objectengine.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myfinal.objectengine.common.BusinessException;
import com.myfinal.objectengine.common.JsonUtils;
import com.myfinal.objectengine.common.PageResult;
import com.myfinal.objectengine.domain.CustomField;
import com.myfinal.objectengine.domain.CustomObject;
import com.myfinal.objectengine.domain.CustomRecord;
import com.myfinal.objectengine.engine.FieldTypeRegistry;
import com.myfinal.objectengine.engine.RecordValidator;
import com.myfinal.objectengine.mapper.CustomRecordMapper;
import com.myfinal.objectengine.service.CustomFieldService;
import com.myfinal.objectengine.service.CustomObjectService;
import com.myfinal.objectengine.service.CustomRecordService;
import com.myfinal.objectengine.vo.RecordVO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CustomRecordServiceImpl extends ServiceImpl<CustomRecordMapper, CustomRecord>
    implements CustomRecordService {

    /** 关键字匹配的候选字段类型：文本类字段（SELECT 单选按 value 匹配，多选不参与） */
    private static final Set<String> TEXT_SEARCH_TYPES =
        Set.of("TEXT", "TEXTAREA", "SELECT", "PHONE", "EMAIL", "URL");

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
        stripComputedFields(fields, body);
        RecordValidator.validate(fields, body);
        checkUniqueValues(object, fields, body, null);

        CustomRecord record = new CustomRecord();
        record.setObjectId(object.getId());
        record.setDataJson(JsonUtils.write(body));
        record.setCreatedAt(new Date());
        record.setUpdatedAt(new Date());
        save(record);
        RecordVO vo = RecordVO.from(record);
        enrichRelationFields(object, List.of(record), List.of(vo));
        return vo;
    }

    @Override
    public RecordVO update(String objectApiName, Long id, Map<String, Object> body) {
        CustomObject object = customObjectService.requireByApiName(objectApiName);
        CustomRecord record = requireRecord(objectApiName, id);
        List<CustomField> fields = customFieldService.listByObjectId(object.getId());
        stripComputedFields(fields, body);
        RecordValidator.validate(fields, body);
        checkUniqueValues(object, fields, body, record.getId());

        // 全量替换字段值，与创建的语义保持一致
        record.setDataJson(JsonUtils.write(body));
        record.setUpdatedAt(new Date());
        updateById(record);
        RecordVO vo = RecordVO.from(record);
        enrichRelationFields(object, List.of(record), List.of(vo));
        return vo;
    }

    /** REFERENCE 为运行时计算字段：丢弃请求体中的写入企图，查询时由 enrichRelationFields 统一计算 */
    private void stripComputedFields(List<CustomField> fields, Map<String, Object> body) {
        for (CustomField field : fields) {
            if ("REFERENCE".equals(field.getFieldType())) {
                body.remove(field.getApiName());
            }
        }
    }

    /**
     * 应用层唯一性校验（uniqueFlag）：同对象同字段出现相同值即拒绝。
     * 值存在 data_json 中，无法建数据库唯一索引，极端并发下存在竞态窗口；
     * 更新时排除记录自身，避免“未修改也报重复”
     */
    private void checkUniqueValues(CustomObject object, List<CustomField> fields, Map<String, Object> body,
                                   Long excludeId) {
        for (CustomField field : fields) {
            if (!Integer.valueOf(1).equals(field.getUniqueFlag())) {
                continue;
            }
            Object value = body.get(field.getApiName());
            if (value == null || (value instanceof String s && s.isEmpty())) {
                continue;
            }
            LambdaQueryWrapper<CustomRecord> wrapper = new LambdaQueryWrapper<CustomRecord>()
                .eq(CustomRecord::getObjectId, object.getId())
                .apply("JSON_UNQUOTE(JSON_EXTRACT(data_json, CONCAT('$.', {0}))) = {1}",
                    field.getApiName(), String.valueOf(value))
                .ne(excludeId != null, CustomRecord::getId, excludeId);
            if (count(wrapper) > 0) {
                throw BusinessException.badRequest("字段【" + field.getFieldName() + "】已存在相同值：" + value);
            }
        }
    }

    @Override
    public PageResult<RecordVO> page(String objectApiName, Integer page, Integer pageSize, String keyword) {
        CustomObject object = customObjectService.requireByApiName(objectApiName);
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 20 : pageSize;
        LambdaQueryWrapper<CustomRecord> wrapper = new LambdaQueryWrapper<CustomRecord>()
            .eq(CustomRecord::getObjectId, object.getId())
            .orderByDesc(CustomRecord::getId);
        applyKeywordFilter(wrapper, object.getId(), keyword);
        Page<CustomRecord> result = page(new Page<>(p, ps), wrapper);
        List<RecordVO> records = result.getRecords().stream().map(RecordVO::from).toList();
        enrichRelationFields(object, result.getRecords(), records);
        return PageResult.of(records, result.getTotal(), p, ps);
    }

    /**
     * 批量计算 LOOKUP / REFERENCE 展示值（displays）：
     * 先收集本页所有 LOOKUP 记录 ID，按目标对象分组后各执行一次批量查询，再做内存映射——
     * 查询次数只与涉及的目标对象个数有关，与记录条数无关（无 N+1）。
     * LOOKUP 展示文本启发式与前端 recordDisplayText 一致：name 字段 → 第一个启用文本字段 → #记录ID
     */
    private void enrichRelationFields(CustomObject object, List<CustomRecord> records, List<RecordVO> vos) {
        if (records.isEmpty()) {
            return;
        }
        List<CustomField> fields = customFieldService.listByObjectId(object.getId());
        List<CustomField> lookups = fields.stream()
            .filter(field -> "LOOKUP".equals(field.getFieldType()) && field.getRelationObjectId() != null)
            .toList();
        List<CustomField> references = fields.stream()
            .filter(field -> "REFERENCE".equals(field.getFieldType())
                && field.getRelationFieldId() != null && field.getReferenceFieldId() != null)
            .toList();
        if (lookups.isEmpty() && references.isEmpty()) {
            return;
        }

        // 1. 收集本页全部 LOOKUP 记录 ID（兼容历史 { recordId } 快照结构）
        Map<Long, Set<Long>> idsByObject = new HashMap<>();
        for (CustomRecord record : records) {
            Map<String, Object> data = JsonUtils.toMap(record.getDataJson());
            if (data == null) {
                continue;
            }
            for (CustomField lookup : lookups) {
                Long id = extractRecordIdSafely(data.get(lookup.getApiName()));
                if (id != null) {
                    idsByObject.computeIfAbsent(lookup.getRelationObjectId(), key -> new HashSet<>()).add(id);
                }
            }
        }

        // 2. 每个目标对象一次批量查询：记录数据 + 字段 ID→API名称 映射 + 展示字段
        Map<Long, Map<String, Object>> targetDataById = new HashMap<>();
        Map<Long, Map<Long, String>> targetFieldApiById = new HashMap<>();
        Map<Long, CustomField> targetDisplayFieldById = new HashMap<>();
        for (Map.Entry<Long, Set<Long>> entry : idsByObject.entrySet()) {
            List<CustomRecord> targets = list(new LambdaQueryWrapper<CustomRecord>()
                .eq(CustomRecord::getObjectId, entry.getKey())
                .in(CustomRecord::getId, entry.getValue()));
            for (CustomRecord target : targets) {
                Map<String, Object> data = JsonUtils.toMap(target.getDataJson());
                if (data != null) {
                    targetDataById.put(target.getId(), data);
                }
            }
            List<CustomField> targetFields = customFieldService.listByObjectId(entry.getKey());
            Map<Long, String> apiById = new HashMap<>();
            for (CustomField field : targetFields) {
                apiById.put(field.getId(), field.getApiName());
            }
            targetFieldApiById.put(entry.getKey(), apiById);
            targetDisplayFieldById.put(entry.getKey(), pickDisplayField(targetFields));
        }

        // 3. 填充 displays：LOOKUP → 记录展示文本；REFERENCE → 经 LOOKUP 解析目标字段值（仅一跳）
        for (int i = 0; i < records.size(); i++) {
            CustomRecord record = records.get(i);
            RecordVO vo = vos.get(i);
            Map<String, Object> data = JsonUtils.toMap(record.getDataJson());
            if (data == null) {
                continue;
            }
            Map<String, Object> displays = new HashMap<>();
            for (CustomField lookup : lookups) {
                Long id = extractRecordIdSafely(data.get(lookup.getApiName()));
                if (id == null) {
                    continue;
                }
                Map<String, Object> targetData = targetDataById.get(id);
                CustomField displayField = targetDisplayFieldById.get(lookup.getRelationObjectId());
                String display = displayField != null && targetData != null
                    ? textOf(targetData.get(displayField.getApiName()))
                    : null;
                displays.put(lookup.getApiName(), display != null ? display : "#" + id);
            }
            for (CustomField reference : references) {
                CustomField lookupField = fields.stream()
                    .filter(field -> field.getId().equals(reference.getRelationFieldId()))
                    .findFirst()
                    .orElse(null);
                if (lookupField == null) {
                    continue;
                }
                Long lookupId = extractRecordIdSafely(data.get(lookupField.getApiName()));
                Map<String, Object> targetData = lookupId == null ? null : targetDataById.get(lookupId);
                String referenceApi = targetFieldApiById
                    .getOrDefault(lookupField.getRelationObjectId(), Map.of())
                    .get(reference.getReferenceFieldId());
                Object value = targetData != null && referenceApi != null ? targetData.get(referenceApi) : null;
                displays.put(reference.getApiName(), value != null ? value : null);
            }
            vo.setDisplays(displays);
        }
    }

    /** 宽松提取记录 ID：数字或 { recordId: 数字 }，其余返回 null（历史数据兼容，读路径不抛错） */
    private Long extractRecordIdSafely(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof Map<?, ?> map && map.get("recordId") instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    /** 关联记录展示字段：name 字段 → 第一个启用中的 TEXT → 第一个启用中的 TEXTAREA */
    private CustomField pickDisplayField(List<CustomField> targetFields) {
        return targetFields.stream()
            .filter(field -> field.getStatus() == null || field.getStatus() == 1)
            .filter(field -> field.getApiName() != null)
            .filter(field -> "name".equals(field.getApiName()))
            .findFirst()
            .or(() -> targetFields.stream()
                .filter(field -> field.getStatus() == null || field.getStatus() == 1)
                .filter(field -> "TEXT".equals(field.getFieldType()))
                .findFirst())
            .or(() -> targetFields.stream()
                .filter(field -> field.getStatus() == null || field.getStatus() == 1)
                .filter(field -> "TEXTAREA".equals(field.getFieldType()))
                .findFirst())
            .orElse(null);
    }

    private static String textOf(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String s) {
            return s.isEmpty() ? null : s;
        }
        return String.valueOf(value);
    }

    /**
     * 关键字筛选：优先匹配「可搜索」的文本类字段，未配置时回退到全部文本类字段。
     * 记录数据存于 data_json，按字段 JSON 路径匹配；字段 apiName 走参数绑定，防注入
     */
    private void applyKeywordFilter(LambdaQueryWrapper<CustomRecord> wrapper, Long objectId, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        List<String> searchPaths = resolveSearchPaths(objectId);
        if (searchPaths.isEmpty()) {
            return;
        }
        String trimmed = keyword.trim();
        wrapper.and(q -> {
            for (String path : searchPaths) {
                q.or().apply("JSON_UNQUOTE(JSON_EXTRACT(data_json, CONCAT('$.', {0}))) LIKE CONCAT('%', {1}, '%')",
                    path, trimmed);
            }
        });
    }

    /** 启用字段的 JSON 路径：优先取 searchableFlag=1 的，没有则回退全部文本类字段 */
    private List<String> resolveSearchPaths(Long objectId) {
        List<CustomField> fields = customFieldService.listByObjectId(objectId).stream()
            .filter(field -> field.getStatus() == null || field.getStatus() == 1)
            .toList();
        List<String> flagged = searchPaths(fields, true);
        return flagged.isEmpty() ? searchPaths(fields, false) : flagged;
    }

    private List<String> searchPaths(List<CustomField> fields, boolean onlySearchable) {
        return fields.stream()
            .filter(field -> !onlySearchable || Integer.valueOf(1).equals(field.getSearchableFlag()))
            .filter(field -> TEXT_SEARCH_TYPES.contains(field.getFieldType()))
            .map(CustomField::getApiName)
            .toList();
    }

    @Override
    public RecordVO get(String objectApiName, Long id) {
        CustomObject object = customObjectService.requireByApiName(objectApiName);
        CustomRecord record = requireRecord(objectApiName, id);
        RecordVO vo = RecordVO.from(record);
        enrichRelationFields(object, List.of(record), List.of(vo));
        return vo;
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
}
