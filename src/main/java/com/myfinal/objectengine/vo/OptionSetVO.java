package com.myfinal.objectengine.vo;

import com.myfinal.objectengine.common.DateUtils;
import com.myfinal.objectengine.domain.CustomOptionSet;
import java.util.List;
import lombok.Data;

/**
 * 通用选项集信息；detail 场景携带 options，分页列表为 null
 */
@Data
public class OptionSetVO {

    private Long id;
    private String name;
    private String apiName;
    private String description;
    private Integer status;
    private Integer sort;
    private String remark;
    private String createdAt;
    private String updatedAt;

    /**
     * 选项明细（仅详情接口返回）
     */
    private List<OptionVO> options;

    /**
     * 选项摘要（分页列表用）：启用选项的 label 顿号拼接
     */
    private String optionsSummary;

    public static OptionSetVO from(CustomOptionSet entity) {
        OptionSetVO vo = new OptionSetVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setApiName(entity.getApiName());
        vo.setDescription(entity.getDescription());
        vo.setStatus(entity.getStatus());
        vo.setSort(entity.getSort());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(DateUtils.format(entity.getCreatedAt()));
        vo.setUpdatedAt(DateUtils.format(entity.getUpdatedAt()));
        return vo;
    }
}
