package com.myfinal.objectengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myfinal.objectengine.common.PageResult;
import com.myfinal.objectengine.domain.CustomObject;
import com.myfinal.objectengine.dto.CreateObjectRequest;
import com.myfinal.objectengine.dto.UpdateObjectRequest;
import com.myfinal.objectengine.vo.ObjectVO;

/**
 * 自定义对象 Service
 */
public interface CustomObjectService extends IService<CustomObject> {

    /**
     * 按 apiName 查询对象，不存在抛 404
     */
    CustomObject requireByApiName(String apiName);

    /**
     * 分页查询对象列表，keyword 匹配对象名称或 apiName
     */
    PageResult<ObjectVO> page(String keyword, Integer page, Integer pageSize);

    ObjectVO create(CreateObjectRequest request);

    /**
     * 修改对象，apiName 不可修改
     */
    ObjectVO update(String apiName, UpdateObjectRequest request);

    /**
     * 删除对象，事务内级联删除字段与记录
     */
    void deleteByApiName(String apiName);
}
