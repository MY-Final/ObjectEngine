package com.myfinal.objectengine.controller;

import com.myfinal.objectengine.common.ApiResponse;
import com.myfinal.objectengine.service.CustomObjectLayoutService;
import com.myfinal.objectengine.service.ObjectMetadataService;
import com.myfinal.objectengine.vo.ObjectLayoutRuntimeVO;
import com.myfinal.objectengine.vo.ObjectMetadataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Metadata / Layout Runtime API：前端动态页面唯一依赖
 */
@RestController
@RequestMapping("/api/v1/objects")
@RequiredArgsConstructor
public class ObjectMetadataController {

    private final ObjectMetadataService objectMetadataService;
    private final CustomObjectLayoutService customObjectLayoutService;

    @GetMapping("/{apiName}/metadata")
    public ApiResponse<ObjectMetadataVO> metadata(@PathVariable String apiName) {
        return ApiResponse.ok(objectMetadataService.getMetadata(apiName));
    }

    @GetMapping("/{apiName}/layout")
    public ApiResponse<ObjectLayoutRuntimeVO> layout(@PathVariable String apiName) {
        return ApiResponse.ok(customObjectLayoutService.getRuntimeLayout(apiName));
    }
}
