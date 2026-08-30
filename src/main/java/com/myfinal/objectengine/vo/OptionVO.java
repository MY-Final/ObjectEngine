package com.myfinal.objectengine.vo;

import com.myfinal.objectengine.common.DateUtils;
import com.myfinal.objectengine.domain.CustomOption;
import lombok.Data;

/**
 * 通用选项信息
 */
@Data
public class OptionVO {

    private Long id;
    private Long optionSetId;
    private String label;
    private String value;
    private Integer sort;
    private Integer status;
    private Boolean isDefault;
    private String description;
    private String remark;
    private String createdAt;
    private String updatedAt;

    public static OptionVO from(CustomOption entity) {
        OptionVO vo = new OptionVO();
        vo.setId(entity.getId());
        vo.setOptionSetId(entity.getOptionSetId());
        vo.setLabel(entity.getLabel());
        vo.setValue(entity.getValue());
        vo.setSort(entity.getSort());
        vo.setStatus(entity.getStatus());
        vo.setIsDefault(entity.getIsDefault() != null && entity.getIsDefault() == 1);
        vo.setDescription(entity.getDescription());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(DateUtils.format(entity.getCreatedAt()));
        vo.setUpdatedAt(DateUtils.format(entity.getUpdatedAt()));
        return vo;
    }
}
