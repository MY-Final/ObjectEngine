package com.myfinal.objectengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myfinal.objectengine.domain.CustomField;
import com.myfinal.objectengine.dto.CreateFieldRequest;
import com.myfinal.objectengine.dto.UpdateFieldRequest;
import com.myfinal.objectengine.vo.FieldVO;

import java.util.List;

/**
 * 自定义字段 Service
 */
public interface CustomFieldService extends IService<CustomField> {

    /**
     * 查询对象下全部字段，按 sort ASC 排序（供 Metadata 与 Record 校验使用）
     */
    List<CustomField> listByObjectId(Long objectId);

    /**
     * 查询对象下字段 VO 列表，对象不存在抛 404
     */
    List<FieldVO> listByObjectApiName(String objectApiName);

    /**
     * 按对象内字段 apiName 查询字段，不存在抛 404
     */
    CustomField requireByApiName(Long objectId, String fieldApiName);

    FieldVO create(String objectApiName, CreateFieldRequest request);

    /**
     * 修改字段，fieldApiName 与 fieldType 不可修改
     */
    FieldVO update(String objectApiName, String fieldApiName, UpdateFieldRequest request);

    /**
     * 删除字段元数据，不修改历史 Record 的 data_json
     */
    void delete(String objectApiName, String fieldApiName);
}
