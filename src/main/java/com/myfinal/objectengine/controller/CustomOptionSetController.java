package com.myfinal.objectengine.controller;

import com.myfinal.objectengine.common.ApiResponse;
import com.myfinal.objectengine.common.PageResult;
import com.myfinal.objectengine.dto.CreateOptionRequest;
import com.myfinal.objectengine.dto.CreateOptionSetRequest;
import com.myfinal.objectengine.dto.OptionSetQueryRequest;
import com.myfinal.objectengine.dto.UpdateOptionRequest;
import com.myfinal.objectengine.dto.UpdateOptionSetRequest;
import com.myfinal.objectengine.service.CustomOptionSetService;
import com.myfinal.objectengine.vo.OptionSetVO;
import com.myfinal.objectengine.vo.OptionVO;
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
 * 通用选项集 API：SELECT / MULTI_SELECT 字段的可复用选项来源
 */
@RestController
@RequestMapping("/api/v1/option-sets")
@RequiredArgsConstructor
public class CustomOptionSetController {

    private final CustomOptionSetService customOptionSetService;

    @GetMapping
    public ApiResponse<PageResult<OptionSetVO>> page(OptionSetQueryRequest query) {
        return ApiResponse.ok(customOptionSetService.page(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<OptionSetVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(customOptionSetService.requireDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody CreateOptionSetRequest request) {
        return ApiResponse.ok(customOptionSetService.create(request), "选项集创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateOptionSetRequest request) {
        customOptionSetService.update(id, request);
        return ApiResponse.ok(null, "选项集修改成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        customOptionSetService.delete(id);
        return ApiResponse.ok(null, "选项集删除成功");
    }

    @GetMapping("/{id}/options")
    public ApiResponse<List<OptionVO>> listOptions(@PathVariable Long id) {
        return ApiResponse.ok(customOptionSetService.listEnabledOptions(id));
    }

    @PostMapping("/{id}/options")
    public ApiResponse<Long> createOption(@PathVariable Long id, @Valid @RequestBody CreateOptionRequest request) {
        return ApiResponse.ok(customOptionSetService.createOption(id, request), "选项创建成功");
    }

    @PutMapping("/{id}/options/{optionId}")
    public ApiResponse<Void> updateOption(@PathVariable Long id, @PathVariable Long optionId,
                                          @Valid @RequestBody UpdateOptionRequest request) {
        customOptionSetService.updateOption(id, optionId, request);
        return ApiResponse.ok(null, "选项修改成功");
    }

    @DeleteMapping("/{id}/options/{optionId}")
    public ApiResponse<Void> deleteOption(@PathVariable Long id, @PathVariable Long optionId) {
        customOptionSetService.deleteOption(id, optionId);
        return ApiResponse.ok(null, "选项删除成功");
    }
}
