package com.myfinal.objectengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myfinal.objectengine.common.PageResult;
import com.myfinal.objectengine.domain.CustomOptionSet;
import com.myfinal.objectengine.dto.CreateOptionRequest;
import com.myfinal.objectengine.dto.CreateOptionSetRequest;
import com.myfinal.objectengine.dto.OptionSetQueryRequest;
import com.myfinal.objectengine.dto.UpdateOptionRequest;
import com.myfinal.objectengine.dto.UpdateOptionSetRequest;
import com.myfinal.objectengine.vo.OptionSetVO;
import com.myfinal.objectengine.vo.OptionVO;

import java.util.List;

/**
 * 通用选项集 Service：选项集与选项的配置管理，供 SELECT / MULTI_SELECT 字段引用
 */
public interface CustomOptionSetService extends IService<CustomOptionSet> {

    PageResult<OptionSetVO> page(OptionSetQueryRequest query);

    OptionSetVO requireDetail(Long id);

    Long create(CreateOptionSetRequest request);

    void update(Long id, UpdateOptionSetRequest request);

    void delete(Long id);

    /** 运行时使用：仅启用状态的选项，按 sort 升序 */
    List<OptionVO> listEnabledOptions(Long optionSetId);

    Long createOption(Long optionSetId, CreateOptionRequest request);

    void updateOption(Long optionSetId, Long optionId, UpdateOptionRequest request);

    void deleteOption(Long optionSetId, Long optionId);
}
