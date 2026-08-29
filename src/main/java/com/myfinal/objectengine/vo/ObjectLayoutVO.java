package com.myfinal.objectengine.vo;

import com.myfinal.objectengine.common.JsonUtils;
import com.myfinal.objectengine.domain.CustomObjectLayout;
import lombok.Data;

@Data
public class ObjectLayoutVO {

    private Long id;
    private String objectApiName;
    private String layoutName;
    private String layoutType;
    private Object configJson;
    private Integer status;
    private Integer sort;
    private String remark;

    public static ObjectLayoutVO from(CustomObjectLayout entity) {
        ObjectLayoutVO vo = new ObjectLayoutVO();
        vo.setId(entity.getId());
        vo.setObjectApiName(entity.getObjectApiName());
        vo.setLayoutName(entity.getLayoutName());
        vo.setLayoutType(entity.getLayoutType());
        vo.setConfigJson(JsonUtils.parse(entity.getConfigJson()));
        vo.setStatus(entity.getStatus());
        vo.setSort(entity.getSort());
        vo.setRemark(entity.getRemark());
        return vo;
    }
}
