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
        RecordValidator.validate(fields, body);
        normalizeReferenceFields(object, fields, body);
        checkUniqueValues(object, fields, body, null);

        CustomRecord record = new CustomRecord();
        record.setObjectId(object.getId());
        record.setDataJson(JsonUtils.write(body));
        record.setCreatedAt(new Date());
        record.setUpdatedAt(new Date());
        save(record);
        return RecordVO.from(record);
    }

    @Override
    public RecordVO update(String objectApiName, Long id, Map<String, Object> body) {
        CustomObject object = customObjectService.requireByApiName(objectApiName);
        CustomRecord record = requireRecord(objectApiName, id);
        List<CustomField> fields = customFieldService.listByObjectId(object.getId());
        RecordValidator.validate(fields, body);
        normalizeReferenceFields(object, fields, body);
        checkUniqueValues(object, fields, body, record.getId());

        // 全量替换字段值，与创建的语义保持一致
        record.setDataJson(JsonUtils.write(body));
        record.setUpdatedAt(new Date());
        updateById(record);
        return RecordVO.from(record);
    }

    /**
     * REFERENCE 字段归一化：校验目标对象与记录存在，由服务端解析展示文本，
     * 改写为 { recordId, text } 快照——列表与表单展示不依赖二次查询
     */
    private void normalizeReferenceFields(CustomObject object, List<CustomField> fields, Map<String, Object> body) {
        for (CustomField field : fields) {
            if (!"REFERENCE".equals(field.getFieldType())) {
                continue;
            }
            Object value = body.get(field.getApiName());
            if (value == null || (value instanceof String s && s.isEmpty())) {
                body.put(field.getApiName(), null);
                continue;
            }
            long recordId = FieldTypeRegistry.referenceRecordId(field, value);
            String targetApiName = extractTargetApiName(field);
            CustomObject target = customObjectService.requireByApiName(targetApiName);
            if (!Integer.valueOf(1).equals(target.getStatus())) {
                throw BusinessException.badRequest("关联的对象已停用：" + target.getObjectName());
            }
            CustomRecord targetRecord = requireRecord(targetApiName, recordId);
            body.put(field.getApiName(),
                Map.of("recordId", recordId, "text", resolveDisplayText(target, targetRecord)));
        }
    }

    private String extractTargetApiName(CustomField field) {
        JSONObject config = parseConfig(field.getConfigJson());
        String targetApiName = config.getString("targetObjectApiName");
        if (targetApiName == null || targetApiName.isBlank()) {
            throw BusinessException.badRequest("字段【" + field.getFieldName() + "】未配置关联对象");
        }
        return targetApiName;
    }

    /** 关联记录展示文本：优先 name 字段，其次第一个启用中的文本字段，兜底 #记录ID */
    private String resolveDisplayText(CustomObject target, CustomRecord record) {
        List<CustomField> targetFields = customFieldService.listByObjectId(target.getId()).stream()
            .filter(field -> field.getStatus() == null || field.getStatus() == 1)
            .toList();
        CustomField displayField = targetFields.stream()
            .filter(field -> "name".equals(field.getApiName()))
            .findFirst()
            .or(() -> targetFields.stream().filter(field -> "TEXT".equals(field.getFieldType())).findFirst())
            .or(() -> targetFields.stream().filter(field -> "TEXTAREA".equals(field.getFieldType())).findFirst())
            .orElse(null);
        Map<String, Object> data = JsonUtils.toMap(record.getDataJson());
        if (displayField != null && data != null) {
            Object value = data.get(displayField.getApiName());
            if (value != null && !(value instanceof String s && s.isEmpty())) {
                return String.valueOf(value);
            }
        }
        return "#" + record.getId();
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
        return PageResult.of(records, result.getTotal(), p, ps);
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
