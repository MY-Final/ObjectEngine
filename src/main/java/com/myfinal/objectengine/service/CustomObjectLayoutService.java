package com.myfinal.objectengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myfinal.objectengine.domain.CustomObjectLayout;
import com.myfinal.objectengine.dto.SaveLayoutRequest;
import com.myfinal.objectengine.vo.ObjectLayoutRuntimeVO;
import com.myfinal.objectengine.vo.ObjectLayoutVO;

import java.util.List;

/**
 * 自定义对象布局 Service
 */
public interface CustomObjectLayoutService extends IService<CustomObjectLayout> {

    List<ObjectLayoutVO> listByApiName(String apiName);

    /**
     * 按布局类型查询，不存在返回 null
     */
    ObjectLayoutVO getByType(String apiName, String layoutType);

    /**
     * 创建布局，同一对象同一类型只允许一个
     */
    ObjectLayoutVO create(String apiName, SaveLayoutRequest request);

    /**
     * 按布局类型保存（不存在则创建）
     */
    ObjectLayoutVO saveOrUpdate(String apiName, String layoutType, SaveLayoutRequest request);

    void delete(String apiName, String layoutType);

    /**
     * Runtime 布局：无布局时返回后端生成的默认布局（启用字段 sort ASC，span=6）
     */
    ObjectLayoutRuntimeVO getRuntimeLayout(String apiName);
}
