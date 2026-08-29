package com.myfinal.objectengine.controller;

import com.myfinal.objectengine.common.ApiResponse;
import com.myfinal.objectengine.dto.CreateMenuRequest;
import com.myfinal.objectengine.dto.MenuQueryRequest;
import com.myfinal.objectengine.dto.MoveMenuRequest;
import com.myfinal.objectengine.dto.UpdateMenuRequest;
import com.myfinal.objectengine.dto.UpdateMenuSortRequest;
import com.myfinal.objectengine.dto.UpdateMenuStatusRequest;
import com.myfinal.objectengine.dto.UpdateMenuVisibleRequest;
import com.myfinal.objectengine.service.CustomMenuService;
import com.myfinal.objectengine.vo.MenuTreeVO;
import com.myfinal.objectengine.vo.MenuVO;
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
 * 菜单管理 API
 */
@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class CustomMenuController {

    private final CustomMenuService customMenuService;

    @GetMapping
    public ApiResponse<List<MenuVO>> list(MenuQueryRequest query) {
        return ApiResponse.ok(customMenuService.list(query));
    }

    @GetMapping("/tree")
    public ApiResponse<List<MenuTreeVO>> tree() {
        return ApiResponse.ok(customMenuService.tree());
    }

    @GetMapping("/{id}")
    public ApiResponse<MenuVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(customMenuService.requireById(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody CreateMenuRequest request) {
        return ApiResponse.ok(customMenuService.create(request), "菜单创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateMenuRequest request) {
        customMenuService.update(id, request);
        return ApiResponse.ok(null, "菜单修改成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        customMenuService.delete(id);
        return ApiResponse.ok(null, "菜单删除成功");
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody UpdateMenuStatusRequest request) {
        customMenuService.updateStatus(id, request.getStatus());
        return ApiResponse.ok(null, "菜单状态修改成功");
    }

    @PutMapping("/{id}/visible")
    public ApiResponse<Void> updateVisible(@PathVariable Long id,
                                           @Valid @RequestBody UpdateMenuVisibleRequest request) {
        customMenuService.updateVisible(id, request.getVisible());
        return ApiResponse.ok(null, "菜单显示状态修改成功");
    }

    @PutMapping("/{id}/sort")
    public ApiResponse<Void> updateSort(@PathVariable Long id,
                                        @Valid @RequestBody UpdateMenuSortRequest request) {
        customMenuService.updateSort(id, request.getSort());
        return ApiResponse.ok(null, "菜单排序修改成功");
    }

    @PutMapping("/{id}/move")
    public ApiResponse<Void> move(@PathVariable Long id, @Valid @RequestBody MoveMenuRequest request) {
        customMenuService.move(id, request);
        return ApiResponse.ok(null, "菜单移动成功");
    }
}
