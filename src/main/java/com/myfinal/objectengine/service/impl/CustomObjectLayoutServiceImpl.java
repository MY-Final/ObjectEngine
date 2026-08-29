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
import com.myfinal.objectengine.domain.CustomObjectLayout;
import com.myfinal.objectengine.dto.SaveLayoutRequest;
import com.myfinal.objectengine.mapper.CustomObjectLayoutMapper;
import com.myfinal.objectengine.service.CustomFieldService;
import com.myfinal.objectengine.service.CustomObjectLayoutService;
import com.myfinal.objectengine.service.CustomObjectService;
import com.myfinal.objectengine.vo.FieldVO;
import com.myfinal.objectengine.vo.ObjectLayoutRuntimeVO;
import com.myfinal.objectengine.vo.ObjectLayoutVO;
import com.myfinal.objectengine.vo.ObjectVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CustomObjectLayoutServiceImpl extends ServiceImpl<CustomObjectLayoutMapper, CustomObjectLayout>
    implements CustomObjectLayoutService {

    private static final Set<String> VALID_TYPES = Set.of("FRONTEND", "BACKEND");

    private final CustomObjectService customObjectService;
    private final CustomFieldService customFieldService;

    public CustomObjectLayoutServiceImpl(CustomObjectService customObjectService,
                                         CustomFieldService customFieldService) {
        this.customObjectService = customObjectService;
        this.customFieldService = customFieldService;
    }

    @Override
    public List<ObjectLayoutVO> listByApiName(String apiName) {
        customObjectService.requireByApiName(apiName);
        return list(new LambdaQueryWrapper<CustomObjectLayout>()
                .eq(CustomObjectLayout::getObjectApiName, apiName)
                .orderByAsc(CustomObjectLayout::getSort))
            .stream()
            .map(ObjectLayoutVO::from)
            .toList();
    }

    @Override
    public ObjectLayoutVO getByType(String apiName, String layoutType) {
        requireValidType(layoutType);
        customObjectService.requireByApiName(apiName);
        CustomObjectLayout layout = findByType(apiName, layoutType);
        return layout == null ? null : ObjectLayoutVO.from(layout);
    }

    @Override
    public ObjectLayoutVO create(String apiName, SaveLayoutRequest request) {
        CustomObject object = customObjectService.requireByApiName(apiName);
        String layoutType = requireValidType(request.getLayoutType());
        if (findByType(apiName, layoutType) != null) {
            throw BusinessException.badRequest("该类型的布局已存在：" + layoutType);
        }
        JSONObject config = validateConfig(object, request.getConfigJson());
        CustomObjectLayout layout = new CustomObjectLayout();
        applyLayout(layout, object.getApiName(), layoutType, request, config);
        save(layout);
        return ObjectLayoutVO.from(layout);
    }

    @Override
    public ObjectLayoutVO saveOrUpdate(String apiName, String layoutType, SaveLayoutRequest request) {
        CustomObject object = customObjectService.requireByApiName(apiName);
        String type = requireValidType(layoutType);
        JSONObject config = validateConfig(object, request.getConfigJson());
        CustomObjectLayout layout = findByType(apiName, type);
        if (layout == null) {
            layout = new CustomObjectLayout();
        }
        applyLayout(layout, object.getApiName(), type, request, config);
        saveOrUpdate(layout);
        return ObjectLayoutVO.from(layout);
    }

    @Override
    public void delete(String apiName, String layoutType) {
        requireValidType(layoutType);
        CustomObjectLayout layout = findByType(apiName, layoutType);
        if (layout == null) {
            throw BusinessException.notFound("布局不存在：" + layoutType);
        }
        removeById(layout.getId());
    }

    @Override
    public ObjectLayoutRuntimeVO getRuntimeLayout(String apiName) {
        CustomObject object = customObjectService.requireByApiName(apiName);
        List<CustomField> enabledFields = customFieldService.listByObjectId(object.getId()).stream()
            .filter(field -> !Integer.valueOf(0).equals(field.getStatus()))
            .toList();
        CustomObjectLayout layout = findByType(apiName, "FRONTEND");
        Object config;
        if (layout != null && layout.getConfigJson() != null) {
            config = JsonUtils.parse(layout.getConfigJson());
        } else {
            // 无布局时由后端生成默认布局：启用字段 sort ASC，span=6（规范第四十八节）
            config = buildDefaultConfig(enabledFields);
        }
        List<FieldVO> fields = enabledFields.stream().map(FieldVO::from).toList();
        return new ObjectLayoutRuntimeVO(ObjectVO.from(object), config, fields);
    }

    private CustomObjectLayout findByType(String apiName, String layoutType) {
        return getOne(new LambdaQueryWrapper<CustomObjectLayout>()
            .eq(CustomObjectLayout::getObjectApiName, apiName)
            .eq(CustomObjectLayout::getLayoutType, layoutType), false);
    }

    private String requireValidType(String layoutType) {
        if (layoutType == null || !VALID_TYPES.contains(layoutType)) {
            throw BusinessException.badRequest("布局类型不合法：" + layoutType);
        }
        return layoutType;
    }

    private void applyLayout(CustomObjectLayout layout, String apiName, String layoutType,
                             SaveLayoutRequest request, JSONObject config) {
        layout.setObjectApiName(apiName);
        layout.setLayoutType(layoutType);
        layout.setLayoutName(request.getLayoutName() == null || request.getLayoutName().isBlank()
            ? defaultLayoutName(layoutType)
            : request.getLayoutName());
        layout.setConfigJson(JsonUtils.write(config));
        layout.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        layout.setSort(request.getSort() == null ? 0 : request.getSort());
        layout.setRemark(request.getRemark());
        if (layout.getCreatedAt() == null) {
            layout.setCreatedAt(new Date());
        }
        layout.setUpdatedAt(new Date());
    }

    private String defaultLayoutName(String layoutType) {
        return "FRONTEND".equals(layoutType) ? "默认前台布局" : "默认后台布局";
    }

    /**
     * 后端最终数据完整性校验（规范第三十九节，不信任前端）：
     * Section id/title/sort 合法且不重复；fieldApiName 必须属于该对象且启用；
     * span 1～12；字段 sort 不重复；一个字段不能出现在多个 Section
     */
    private JSONObject validateConfig(CustomObject object, Object configJson) {
        if (configJson == null) {
            throw BusinessException.badRequest("布局配置不能为空");
        }
        JSONObject config = JSON.parseObject(JsonUtils.write(configJson));
        JSONArray sections = config.getJSONArray("sections");
        if (sections == null || sections.isEmpty()) {
            throw BusinessException.badRequest("布局必须包含至少一个 Section");
        }
        List<CustomField> allFields = customFieldService.listByObjectId(object.getId());
        Map<String, CustomField> allFieldMap = new LinkedHashMap<>();
        for (CustomField field : allFields) {
            allFieldMap.put(field.getApiName(), field);
        }
        Set<String> sectionIds = new HashSet<>();
        Set<Integer> sectionSorts = new HashSet<>();
        Set<String> usedFieldApiNames = new HashSet<>();
        for (int i = 0; i < sections.size(); i++) {
            JSONObject section = sections.getJSONObject(i);
            String sectionId = section.getString("id");
            if (sectionId == null || sectionId.isBlank()) {
                throw BusinessException.badRequest("第 " + (i + 1) + " 个 Section 缺少 id");
            }
            if (!sectionIds.add(sectionId)) {
                throw BusinessException.badRequest("Section id 重复：" + sectionId);
            }
            String title = section.getString("title");
            if (title == null || title.isBlank()) {
                throw BusinessException.badRequest("Section 标题不能为空：" + sectionId);
            }
            Integer sectionSort = section.getInteger("sort");
            if (sectionSort == null) {
                throw BusinessException.badRequest("Section 缺少 sort：" + sectionId);
            }
            if (!sectionSorts.add(sectionSort)) {
                throw BusinessException.badRequest("Section sort 重复：" + sectionSort);
            }
            JSONArray layoutFields = section.getJSONArray("fields");
            if (layoutFields == null) {
                continue;
            }
            Set<Integer> fieldSorts = new HashSet<>();
            for (int j = 0; j < layoutFields.size(); j++) {
                JSONObject layoutField = layoutFields.getJSONObject(j);
                String fieldApiName = layoutField.getString("fieldApiName");
                CustomField field = fieldApiName == null ? null : allFieldMap.get(fieldApiName);
                if (field == null) {
                    throw BusinessException.badRequest("布局中存在不存在的字段：" + fieldApiName);
                }
                if (Integer.valueOf(0).equals(field.getStatus())) {
                    throw BusinessException.badRequest("布局中包含已停用的字段：" + fieldApiName);
                }
                if (!usedFieldApiNames.add(fieldApiName)) {
                    throw BusinessException.badRequest("字段在多个 Section 中重复出现：" + fieldApiName);
                }
                Integer span = layoutField.getInteger("span");
                if (span == null || span < 1 || span > 12) {
                    throw BusinessException.badRequest("字段 span 必须在 1～12 之间：" + fieldApiName);
                }
                Integer fieldSort = layoutField.getInteger("sort");
                if (fieldSort == null) {
                    throw BusinessException.badRequest("字段缺少 sort：" + fieldApiName);
                }
                if (!fieldSorts.add(fieldSort)) {
                    throw BusinessException.badRequest("字段 sort 重复：" + fieldApiName);
                }
            }
        }
        return config;
    }

    private Object buildDefaultConfig(List<CustomField> enabledFields) {
        JSONArray layoutFields = new JSONArray();
        int sort = 1;
        for (CustomField field : enabledFields) {
            JSONObject layoutField = new JSONObject();
            layoutField.put("fieldApiName", field.getApiName());
            layoutField.put("span", 6);
            layoutField.put("sort", sort++);
            layoutFields.add(layoutField);
        }
        JSONObject section = new JSONObject(new LinkedHashMap<>());
        section.put("id", "default");
        section.put("title", "基本信息");
        section.put("sort", 1);
        section.put("columns", 12);
        section.put("fields", layoutFields);
        JSONArray sections = new JSONArray();
        sections.add(section);
        JSONObject config = new JSONObject(new LinkedHashMap<>());
        config.put("sections", sections);
        return config;
    }
}
