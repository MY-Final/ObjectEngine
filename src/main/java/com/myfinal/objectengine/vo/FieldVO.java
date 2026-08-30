package com.myfinal.objectengine.vo;

import com.myfinal.objectengine.common.JsonUtils;
import com.myfinal.objectengine.domain.CustomField;
import lombok.Data;

@Data
public class FieldVO {

    private Long id;
    private String apiName;
    private String fieldName;
    private String fieldType;
    private String description;
    private String remark;
    private Integer requiredFlag;
    private Integer uniqueFlag;
    private Integer searchableFlag;
    private Integer sortableFlag;
    private Integer sort;
    private String defaultValue;
    private Object configJson;
    private String optionSource;
    private Long optionSetId;
    private Integer status;

    public static FieldVO from(CustomField entity) {
        FieldVO vo = new FieldVO();
        vo.setId(entity.getId());
        vo.setApiName(entity.getApiName());
        vo.setFieldName(entity.getFieldName());
        vo.setFieldType(entity.getFieldType());
        vo.setDescription(entity.getDescription());
        vo.setRemark(entity.getRemark());
        vo.setRequiredFlag(entity.getRequiredFlag());
        vo.setUniqueFlag(entity.getUniqueFlag());
        vo.setSearchableFlag(entity.getSearchableFlag());
        vo.setSortableFlag(entity.getSortableFlag());
        vo.setSort(entity.getSort());
        vo.setDefaultValue(entity.getDefaultValue());
        vo.setConfigJson(JsonUtils.parse(entity.getConfigJson()));
        vo.setOptionSource(entity.getOptionSource());
        vo.setOptionSetId(entity.getOptionSetId());
        vo.setStatus(entity.getStatus());
        return vo;
    }
}
