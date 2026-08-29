package com.myfinal.objectengine.controller;

import com.myfinal.objectengine.common.ApiResponse;
import com.myfinal.objectengine.common.PageResult;
import com.myfinal.objectengine.dto.CreateObjectRequest;
import com.myfinal.objectengine.dto.UpdateObjectRequest;
import com.myfinal.objectengine.service.CustomObjectService;
import com.myfinal.objectengine.vo.ObjectVO;
import jakarta.validation.Valid;
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

/**
 * 自定义对象 CRUD
 */
@RestController
@RequestMapping("/api/v1/custom-objects")
@RequiredArgsConstructor
public class CustomObjectController {

    private final CustomObjectService customObjectService;

    @PostMapping
    public ApiResponse<ObjectVO> create(@Valid @RequestBody CreateObjectRequest request) {
        return ApiResponse.ok(customObjectService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResult<ObjectVO>> page(@RequestParam(required = false) String keyword,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "20") Integer pageSize) {
        return ApiResponse.ok(customObjectService.page(keyword, page, pageSize));
    }

    @GetMapping("/{apiName}")
    public ApiResponse<ObjectVO> detail(@PathVariable String apiName) {
        return ApiResponse.ok(ObjectVO.from(customObjectService.requireByApiName(apiName)));
    }

    @PutMapping("/{apiName}")
    public ApiResponse<ObjectVO> update(@PathVariable String apiName,
                                        @Valid @RequestBody UpdateObjectRequest request) {
        return ApiResponse.ok(customObjectService.update(apiName, request));
    }

    @DeleteMapping("/{apiName}")
    public ApiResponse<Void> delete(@PathVariable String apiName) {
        customObjectService.deleteByApiName(apiName);
        return ApiResponse.ok();
    }
}
