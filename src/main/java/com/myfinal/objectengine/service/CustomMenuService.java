package com.myfinal.objectengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myfinal.objectengine.domain.CustomMenu;
import com.myfinal.objectengine.dto.CreateMenuRequest;
import com.myfinal.objectengine.dto.MenuQueryRequest;
import com.myfinal.objectengine.dto.MoveMenuRequest;
import com.myfinal.objectengine.dto.UpdateMenuRequest;
import com.myfinal.objectengine.vo.MenuTreeVO;
import com.myfinal.objectengine.vo.MenuVO;

import java.util.List;

/**
 * 菜单配置 Service
 */
public interface CustomMenuService extends IService<CustomMenu> {

    /**
     * 后台菜单列表，支持 menuName/menuType/status/visible 过滤
     */
    List<MenuVO> list(MenuQueryRequest query);

    /**
     * 前台菜单树：仅启用且显示的菜单，按 sort ASC 排序，内存构建
     */
    List<MenuTreeVO> tree();

    MenuVO requireById(Long id);

    /**
     * 创建菜单，返回新菜单 ID；OBJECT 类型校验对象存在，LINK 类型校验路由
     */
    Long create(CreateMenuRequest request);

    void update(Long id, UpdateMenuRequest request);

    /**
     * 删除菜单：DIRECTORY 存在子菜单时禁止删除
     */
    void delete(Long id);

    void updateStatus(Long id, Integer status);

    void updateVisible(Long id, Integer visible);

    void updateSort(Long id, Integer sort);

    /**
     * 移动菜单到目标父菜单下并设置排序
     */
    void move(Long id, MoveMenuRequest request);
}
