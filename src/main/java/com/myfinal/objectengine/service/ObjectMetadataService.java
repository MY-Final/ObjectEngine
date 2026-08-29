package com.myfinal.objectengine.service;

import com.myfinal.objectengine.domain.CustomObject;
import com.myfinal.objectengine.vo.FieldVO;
import com.myfinal.objectengine.vo.ObjectMetadataVO;
import com.myfinal.objectengine.vo.ObjectVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 组装完整 Metadata：对象 + 字段列表
 */
@Service
public class ObjectMetadataService {

    private final CustomObjectService customObjectService;
    private final CustomFieldService customFieldService;

    public ObjectMetadataService(CustomObjectService customObjectService, CustomFieldService customFieldService) {
        this.customObjectService = customObjectService;
        this.customFieldService = customFieldService;
    }

    public ObjectMetadataVO getMetadata(String apiName) {
        CustomObject object = customObjectService.requireByApiName(apiName);
        List<FieldVO> fields = customFieldService.listByObjectId(object.getId())
            .stream()
            .map(FieldVO::from)
            .toList();
        return new ObjectMetadataVO(ObjectVO.from(object), fields);
    }
}
