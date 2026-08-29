package com.myfinal.objectengine.controller;

import com.myfinal.objectengine.common.ApiResponse;
import com.myfinal.objectengine.dto.CreateFieldRequest;
import com.myfinal.objectengine.dto.UpdateFieldRequest;
import com.myfinal.objectengine.service.CustomFieldService;
import com.myfinal.objectengine.vo.FieldVO;
import jakarta.validation.Valid;
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
 * 自定义字段 CRUD，挂在对象之下
 */
@RestController
@RequestMapping("/api/v1/custom-objects/{apiName}/fields")
@RequiredArgsConstructor
public class CustomFieldController {

    private final CustomFieldService customFieldService;

    @PostMapping
    public ApiResponse<FieldVO> create(@PathVariable String apiName,
                                       @Valid @RequestBody CreateFieldRequest request) {
        return ApiResponse.ok(customFieldService.create(apiName, request));
    }

    @GetMapping
    public ApiResponse<List<FieldVO>> list(@PathVariable String apiName) {
        return ApiResponse.ok(customFieldService.listByObjectApiName(apiName));
    }

    @PutMapping("/{fieldApiName}")
    public ApiResponse<FieldVO> update(@PathVariable String apiName,
                                       @PathVariable String fieldApiName,
                                       @Valid @RequestBody UpdateFieldRequest request) {
        return ApiResponse.ok(customFieldService.update(apiName, fieldApiName, request));
    }

    @DeleteMapping("/{fieldApiName}")
    public ApiResponse<Void> delete(@PathVariable String apiName, @PathVariable String fieldApiName) {
        customFieldService.delete(apiName, fieldApiName);
        return ApiResponse.ok();
    }
}
