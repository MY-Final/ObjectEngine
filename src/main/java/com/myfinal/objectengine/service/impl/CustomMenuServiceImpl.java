package com.myfinal.objectengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myfinal.objectengine.common.BusinessException;
import com.myfinal.objectengine.domain.CustomMenu;
import com.myfinal.objectengine.domain.CustomObject;
import com.myfinal.objectengine.dto.CreateMenuRequest;
import com.myfinal.objectengine.dto.MenuQueryRequest;
import com.myfinal.objectengine.dto.MoveMenuRequest;
import com.myfinal.objectengine.dto.UpdateMenuRequest;
import com.myfinal.objectengine.mapper.CustomMenuMapper;
import com.myfinal.objectengine.service.CustomMenuService;
import com.myfinal.objectengine.service.CustomObjectService;
import com.myfinal.objectengine.vo.MenuTreeVO;
import com.myfinal.objectengine.vo.MenuVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CustomMenuServiceImpl extends ServiceImpl<CustomMenuMapper, CustomMenu>
    implements CustomMenuService {

    private static final Set<String> MENU_TYPES = Set.of("DIRECTORY", "OBJECT", "LINK");
    private static final long ROOT_PARENT_ID = 0L;

    private final CustomObjectService customObjectService;

    public CustomMenuServiceImpl(CustomObjectService customObjectService) {
        this.customObjectService = customObjectService;
    }

    @Override
    public List<MenuVO> list(MenuQueryRequest query) {
        LambdaQueryWrapper<CustomMenu> wrapper = new LambdaQueryWrapper<CustomMenu>()
            .like(hasText(query.getMenuName()), CustomMenu::getMenuName, query.getMenuName())
            .eq(hasText(query.getMenuType()), CustomMenu::getMenuType, query.getMenuType())
            .eq(query.getStatus() != null, CustomMenu::getStatus, query.getStatus())
            .eq(query.getVisible() != null, CustomMenu::getVisible, query.getVisible())
            .orderByAsc(CustomMenu::getSort)
            .orderByAsc(CustomMenu::getId);
        return list(wrapper).stream().map(MenuVO::from).toList();
    }

    @Override
    public List<MenuTreeVO> tree() {
        // 仅启用且显示的菜单参与前台导航，内存构建树（规范第十五节）
        List<CustomMenu> menus = list(new LambdaQueryWrapper<CustomMenu>()
            .eq(CustomMenu::getStatus, 1)
            .eq(CustomMenu::getVisible, 1)
            .orderByAsc(CustomMenu::getSort)
            .orderByAsc(CustomMenu::getId));
        Map<Long, MenuTreeVO> nodeMap = new LinkedHashMap<>();
        for (CustomMenu menu : menus) {
            nodeMap.put(menu.getId(), MenuTreeVO.from(menu));
        }
        List<MenuTreeVO> roots = new ArrayList<>();
        for (CustomMenu menu : menus) {
            MenuTreeVO node = nodeMap.get(menu.getId());
            MenuTreeVO parent = menu.getParentId() == null ? null : nodeMap.get(menu.getParentId());
            // 父菜单不在可见集合中时按顶级处理，避免节点丢失
            if (parent == null || menu.getParentId() == null || menu.getParentId() == ROOT_PARENT_ID) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    @Override
    public MenuVO requireById(Long id) {
        CustomMenu menu = getById(id);
        if (menu == null) {
            throw BusinessException.notFound("菜单不存在：" + id);
        }
        return MenuVO.from(menu);
    }

    @Override
    public Long create(CreateMenuRequest request) {
        CustomMenu menu = new CustomMenu();
        applyRequest(menu, request.getParentId(), request.getMenuName(), request.getMenuType(),
            request.getObjectApiName(), request.getRoutePath(), request.getComponent(),
            request.getIcon(), request.getSort(), request.getStatus(), request.getVisible(),
            request.getTarget(), request.getRemark());
        menu.setCreatedAt(new Date());
        menu.setUpdatedAt(new Date());
        save(menu);
        return menu.getId();
    }

    @Override
    public void update(Long id, UpdateMenuRequest request) {
        CustomMenu menu = requireEntity(id);
        applyRequest(menu, request.getParentId(), request.getMenuName(), request.getMenuType(),
            request.getObjectApiName(), request.getRoutePath(), request.getComponent(),
            request.getIcon(), request.getSort(), request.getStatus(), request.getVisible(),
            request.getTarget(), request.getRemark());
        menu.setUpdatedAt(new Date());
        updateById(menu);
    }

    @Override
    public void delete(Long id) {
        CustomMenu menu = requireEntity(id);
        // 目录菜单存在子菜单时禁止删除（规范第八节）
        if ("DIRECTORY".equals(menu.getMenuType())) {
            long childCount = count(new LambdaQueryWrapper<CustomMenu>().eq(CustomMenu::getParentId, id));
            if (childCount > 0) {
                throw BusinessException.badRequest("该菜单存在子菜单，请先删除子菜单");
            }
        }
        removeById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        requireFlagValue(status, "状态");
        CustomMenu menu = requireEntity(id);
        menu.setStatus(status);
        menu.setUpdatedAt(new Date());
        updateById(menu);
    }

    @Override
    public void updateVisible(Long id, Integer visible) {
        requireFlagValue(visible, "显示状态");
        CustomMenu menu = requireEntity(id);
        menu.setVisible(visible);
        menu.setUpdatedAt(new Date());
        updateById(menu);
    }

    @Override
    public void updateSort(Long id, Integer sort) {
        CustomMenu menu = requireEntity(id);
        menu.setSort(sort);
        menu.setUpdatedAt(new Date());
        updateById(menu);
    }

    @Override
    public void move(Long id, MoveMenuRequest request) {
        CustomMenu menu = requireEntity(id);
        requireParent(request.getParentId(), id);
        menu.setParentId(request.getParentId());
        menu.setSort(request.getSort());
        menu.setUpdatedAt(new Date());
        updateById(menu);
    }

    private void applyRequest(CustomMenu menu, Long parentId, String menuName, String menuType,
                              String objectApiName, String routePath, String component,
                              String icon, Integer sort, Integer status, Integer visible,
                              String target, String remark) {
        if (menuType == null || !MENU_TYPES.contains(menuType)) {
            throw BusinessException.badRequest("菜单类型不合法：" + menuType);
        }
        if ("OBJECT".equals(menuType)) {
            if (objectApiName == null || objectApiName.isBlank()) {
                throw BusinessException.badRequest("OBJECT 菜单必须关联自定义对象");
            }
            // 应用层校验对象存在，不建外键（规范第十三节）
            long objectCount = customObjectService.count(
                new LambdaQueryWrapper<CustomObject>().eq(CustomObject::getApiName, objectApiName));
            if (objectCount == 0) {
                throw BusinessException.badRequest("关联的自定义对象不存在：" + objectApiName);
            }
            // routePath 未填写时按约定自动生成
            if (routePath == null || routePath.isBlank()) {
                routePath = "/custom/" + objectApiName;
            }
        }
        if ("LINK".equals(menuType) && (routePath == null || routePath.isBlank())) {
            throw BusinessException.badRequest("LINK 菜单必须配置 routePath");
        }
        Long resolvedParentId = parentId == null ? ROOT_PARENT_ID : parentId;
        requireParent(resolvedParentId, menu.getId());
        menu.setParentId(resolvedParentId);
        menu.setMenuName(menuName);
        menu.setMenuType(menuType);
        menu.setObjectApiName("OBJECT".equals(menuType) ? objectApiName : null);
        menu.setRoutePath(routePath);
        menu.setComponent(component);
        menu.setIcon(icon);
        menu.setSort(sort == null ? 0 : sort);
        menu.setStatus(status == null ? 1 : status);
        menu.setVisible(visible == null ? 1 : visible);
        menu.setTarget(target);
        menu.setRemark(remark);
    }

    private void requireParent(Long parentId, Long selfId) {
        if (parentId == null || parentId == ROOT_PARENT_ID) {
            return;
        }
        if (parentId.equals(selfId)) {
            throw BusinessException.badRequest("父菜单不能是当前菜单自身");
        }
        long parentCount = count(new LambdaQueryWrapper<CustomMenu>().eq(CustomMenu::getId, parentId));
        if (parentCount == 0) {
            throw BusinessException.badRequest("父菜单不存在：" + parentId);
        }
    }

    private void requireFlagValue(Integer value, String label) {
        if (value == null || (value != 0 && value != 1)) {
            throw BusinessException.badRequest(label + "只能为 0 或 1");
        }
    }

    private CustomMenu requireEntity(Long id) {
        CustomMenu menu = getById(id);
        if (menu == null) {
            throw BusinessException.notFound("菜单不存在：" + id);
        }
        return menu;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
