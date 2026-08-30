package com.myfinal.objectengine.controller;

import com.myfinal.objectengine.common.ApiResponse;
import com.myfinal.objectengine.common.PageResult;
import com.myfinal.objectengine.service.CustomRecordService;
import com.myfinal.objectengine.vo.RecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Record CRUD：请求体直接使用 fieldApiName -> value
 */
@RestController
@RequestMapping("/api/v1/objects/{apiName}/records")
@RequiredArgsConstructor
public class CustomRecordController {

    private final CustomRecordService customRecordService;

    @PostMapping
    public ApiResponse<RecordVO> create(@PathVariable String apiName,
                                        @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(customRecordService.create(apiName, body));
    }

    @GetMapping
    public ApiResponse<PageResult<RecordVO>> page(@PathVariable String apiName,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "20") Integer pageSize,
                                                  @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(customRecordService.page(apiName, page, pageSize, keyword));
    }

    @PutMapping("/{id}")
    public ApiResponse<RecordVO> update(@PathVariable String apiName,
                                        @PathVariable Long id,
                                        @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(customRecordService.update(apiName, id, body));
    }

    @GetMapping("/{id}")
    public ApiResponse<RecordVO> get(@PathVariable String apiName, @PathVariable Long id) {
        return ApiResponse.ok(customRecordService.get(apiName, id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String apiName, @PathVariable Long id) {
        customRecordService.delete(apiName, id);
        return ApiResponse.ok();
    }
}
