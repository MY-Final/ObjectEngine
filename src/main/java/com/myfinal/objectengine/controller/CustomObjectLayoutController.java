package com.myfinal.objectengine.controller;

import com.myfinal.objectengine.common.ApiResponse;
import com.myfinal.objectengine.dto.SaveLayoutRequest;
import com.myfinal.objectengine.service.CustomObjectLayoutService;
import com.myfinal.objectengine.vo.ObjectLayoutVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 布局配置 API（后台管理端）
 */
@RestController
@RequestMapping("/api/v1/custom-objects/{apiName}/layouts")
@RequiredArgsConstructor
public class CustomObjectLayoutController {

    private final CustomObjectLayoutService customObjectLayoutService;

    @GetMapping
    public ApiResponse<List<ObjectLayoutVO>> list(@PathVariable String apiName) {
        return ApiResponse.ok(customObjectLayoutService.listByApiName(apiName));
    }

    @GetMapping("/{layoutType}")
    public ApiResponse<ObjectLayoutVO> getByType(@PathVariable String apiName,
                                                 @PathVariable String layoutType) {
        return ApiResponse.ok(customObjectLayoutService.getByType(apiName, layoutType));
    }

    @PostMapping
    public ApiResponse<ObjectLayoutVO> create(@PathVariable String apiName,
                                              @RequestBody SaveLayoutRequest request) {
        return ApiResponse.ok(customObjectLayoutService.create(apiName, request));
    }

    @PutMapping("/{layoutType}")
    public ApiResponse<ObjectLayoutVO> saveOrUpdate(@PathVariable String apiName,
                                                    @PathVariable String layoutType,
                                                    @RequestBody SaveLayoutRequest request) {
        return ApiResponse.ok(customObjectLayoutService.saveOrUpdate(apiName, layoutType, request));
    }

    @DeleteMapping("/{layoutType}")
    public ApiResponse<Void> delete(@PathVariable String apiName, @PathVariable String layoutType) {
        customObjectLayoutService.delete(apiName, layoutType);
        return ApiResponse.ok();
    }
}
