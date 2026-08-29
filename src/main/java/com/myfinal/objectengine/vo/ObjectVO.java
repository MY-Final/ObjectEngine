package com.myfinal.objectengine.vo;

import com.myfinal.objectengine.common.DateUtils;
import com.myfinal.objectengine.domain.CustomObject;
import lombok.Data;

@Data
public class ObjectVO {

    private Long id;
    private String apiName;
    private String objectName;
    private String description;
    private String remark;
    private String icon;
    private Integer status;
    private Integer sort;
    private String createdAt;
    private String updatedAt;

    public static ObjectVO from(CustomObject entity) {
        ObjectVO vo = new ObjectVO();
        vo.setId(entity.getId());
        vo.setApiName(entity.getApiName());
        vo.setObjectName(entity.getObjectName());
        vo.setDescription(entity.getDescription());
        vo.setRemark(entity.getRemark());
        vo.setIcon(entity.getIcon());
        vo.setStatus(entity.getStatus());
        vo.setSort(entity.getSort());
        vo.setCreatedAt(DateUtils.format(entity.getCreatedAt()));
        vo.setUpdatedAt(DateUtils.format(entity.getUpdatedAt()));
        return vo;
    }
}
