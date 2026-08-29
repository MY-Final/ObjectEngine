package com.myfinal.objectengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myfinal.objectengine.common.PageResult;
import com.myfinal.objectengine.domain.CustomRecord;
import com.myfinal.objectengine.vo.RecordVO;

import java.util.Map;

/**
 * 自定义对象数据 Service：动态对象统一入口，禁止按业务对象单独建 Service
 */
public interface CustomRecordService extends IService<CustomRecord> {

    /**
     * 创建记录：先读取元数据校验请求（字段存在 / 必填 / 类型 / SELECT 选项），再保存
     *
     * @param body 请求体直接是 fieldApiName -> value
     */
    RecordVO create(String objectApiName, Map<String, Object> body);

    /**
     * 分页查询记录，V1 只支持 page/pageSize
     */
    PageResult<RecordVO> page(String objectApiName, Integer page, Integer pageSize);

    RecordVO get(String objectApiName, Long id);

    void delete(String objectApiName, Long id);
}
