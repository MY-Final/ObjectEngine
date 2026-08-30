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

    /**
     * 关联对象ID（LOOKUP：目标对象；REFERENCE：由关联的 LOOKUP 字段推导）
     */
    private Long relationObjectId;

    /**
     * 关联的 LOOKUP 字段ID（REFERENCE 使用，必须属于当前对象）
     */
    private Long relationFieldId;

    /**
     * 引用的目标字段ID（REFERENCE 使用，属于关联对象）
     */
    private Long referenceFieldId;
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
        vo.setRelationObjectId(entity.getRelationObjectId());
        vo.setRelationFieldId(entity.getRelationFieldId());
        vo.setReferenceFieldId(entity.getReferenceFieldId());
        vo.setStatus(entity.getStatus());
        return vo;
    }
}
