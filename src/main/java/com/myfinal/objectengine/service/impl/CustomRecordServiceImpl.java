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
        checkUniqueValues(object, fields, body);

        CustomRecord record = new CustomRecord();
        record.setObjectId(object.getId());
        record.setDataJson(JsonUtils.write(body));
        record.setCreatedAt(new Date());
        record.setUpdatedAt(new Date());
        save(record);
        return RecordVO.from(record);
    }

    /**
     * 应用层唯一性校验（uniqueFlag）：同对象同字段出现相同值即拒绝。
     * 值存在 data_json 中，无法建数据库唯一索引，极端并发下存在竞态窗口；
     * 后续若新增记录编辑接口，同样必须调用本方法
     */
    private void checkUniqueValues(CustomObject object, List<CustomField> fields, Map<String, Object> body) {
        for (CustomField field : fields) {
            if (!Integer.valueOf(1).equals(field.getUniqueFlag())) {
                continue;
            }
            Object value = body.get(field.getApiName());
            if (value == null || (value instanceof String s && s.isEmpty())) {
                continue;
            }
            long duplicates = count(new LambdaQueryWrapper<CustomRecord>()
                .eq(CustomRecord::getObjectId, object.getId())
                .apply("JSON_UNQUOTE(JSON_EXTRACT(data_json, CONCAT('$.', {0}))) = {1}",
                    field.getApiName(), String.valueOf(value)));
            if (duplicates > 0) {
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
}
