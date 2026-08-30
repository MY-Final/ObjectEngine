package com.myfinal.objectengine.vo;

import com.myfinal.objectengine.common.DateUtils;
import com.myfinal.objectengine.common.JsonUtils;
import com.myfinal.objectengine.domain.CustomRecord;
import lombok.Data;

import java.util.Map;

@Data
public class RecordVO {

    private Long id;
    private Map<String, Object> data;
    private String createdAt;
    private String updatedAt;

    /**
     * LOOKUP / REFERENCE 字段的展示值（字段API名称 → 展示文本/引用值），
     * 由 Record 查询链路批量计算填充；data 中仍保存关联记录 ID
     */
    private Map<String, Object> displays;

    public static RecordVO from(CustomRecord entity) {
        RecordVO vo = new RecordVO();
        vo.setId(entity.getId());
        vo.setData(JsonUtils.toMap(entity.getDataJson()));
        vo.setCreatedAt(DateUtils.format(entity.getCreatedAt()));
        vo.setUpdatedAt(DateUtils.format(entity.getUpdatedAt()));
        return vo;
    }
}
