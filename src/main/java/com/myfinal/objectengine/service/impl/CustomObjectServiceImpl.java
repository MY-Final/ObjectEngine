package com.myfinal.objectengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myfinal.objectengine.common.BusinessException;
import com.myfinal.objectengine.common.PageResult;
import com.myfinal.objectengine.domain.CustomField;
import com.myfinal.objectengine.domain.CustomMenu;
import com.myfinal.objectengine.domain.CustomObject;
import com.myfinal.objectengine.domain.CustomRecord;
import com.myfinal.objectengine.dto.CreateObjectRequest;
import com.myfinal.objectengine.dto.UpdateObjectRequest;
import com.myfinal.objectengine.mapper.CustomFieldMapper;
import com.myfinal.objectengine.mapper.CustomMenuMapper;
import com.myfinal.objectengine.mapper.CustomObjectMapper;
import com.myfinal.objectengine.mapper.CustomRecordMapper;
import com.myfinal.objectengine.service.CustomObjectService;
import com.myfinal.objectengine.vo.ObjectVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class CustomObjectServiceImpl extends ServiceImpl<CustomObjectMapper, CustomObject>
    implements CustomObjectService {

    /** 对象导航菜单统一挂在这个系统目录下，目录不存在时自动创建顶级目录 */
    private static final String OBJECT_MENU_DIRECTORY = "自定义对象";
    private static final long ROOT_PARENT_ID = 0L;

    private final CustomFieldMapper customFieldMapper;
    private final CustomRecordMapper customRecordMapper;
    private final CustomMenuMapper customMenuMapper;

    public CustomObjectServiceImpl(CustomFieldMapper customFieldMapper, CustomRecordMapper customRecordMapper,
                                   CustomMenuMapper customMenuMapper) {
        this.customFieldMapper = customFieldMapper;
        this.customRecordMapper = customRecordMapper;
        this.customMenuMapper = customMenuMapper;
    }

    @Override
    public CustomObject requireByApiName(String apiName) {
        CustomObject object = getOne(new LambdaQueryWrapper<CustomObject>().eq(CustomObject::getApiName, apiName), false);
        if (object == null) {
            throw BusinessException.notFound("对象不存在：" + apiName);
        }
        return object;
    }

    @Override
    public PageResult<ObjectVO> page(String keyword, Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 20 : pageSize;
        LambdaQueryWrapper<CustomObject> wrapper = new LambdaQueryWrapper<CustomObject>()
            .and(keyword != null && !keyword.isBlank(),
                q -> q.like(CustomObject::getObjectName, keyword).or().like(CustomObject::getApiName, keyword))
            .orderByAsc(CustomObject::getSort)
            .orderByAsc(CustomObject::getId);
        Page<CustomObject> result = this.page(new Page<>(p, ps), wrapper);
        List<ObjectVO> records = result.getRecords().stream().map(ObjectVO::from).toList();
        return PageResult.of(records, result.getTotal(), p, ps);
    }

    @Override
    @Transactional
    public ObjectVO create(CreateObjectRequest request) {
        long count = count(new LambdaQueryWrapper<CustomObject>().eq(CustomObject::getApiName, request.getApiName()));
        if (count > 0) {
            throw BusinessException.badRequest("对象API名称已存在：" + request.getApiName());
        }
        CustomObject object = new CustomObject();
        object.setApiName(request.getApiName());
        object.setObjectName(request.getObjectName());
        object.setDescription(request.getDescription());
        object.setRemark(request.getRemark());
        object.setIcon(request.getIcon());
        object.setSort(request.getSort() == null ? 0 : request.getSort());
        object.setStatus(1);
        object.setCreatedAt(new Date());
        object.setUpdatedAt(new Date());
        save(object);
        registerObjectMenu(object);
        return ObjectVO.from(object);
    }

    /**
     * 创建对象后自动注册导航菜单，与对象生命周期绑定：
     * 对象删除时级联删除，菜单管理中不允许单独删除
     */
    private void registerObjectMenu(CustomObject object) {
        CustomMenu directory = findObjectDirectory();
        if (directory == null) {
            directory = createObjectDirectory();
        }
        CustomMenu menu = new CustomMenu();
        menu.setParentId(directory.getId());
        menu.setMenuName(object.getObjectName());
        menu.setMenuType("OBJECT");
        menu.setObjectApiName(object.getApiName());
        menu.setRoutePath("/custom/" + object.getApiName());
        menu.setSort(nextSiblingSort(directory.getId()));
        menu.setStatus(1);
        menu.setVisible(1);
        menu.setTarget("_self");
        menu.setRemark("由自定义对象自动生成");
        menu.setCreatedAt(new Date());
        menu.setUpdatedAt(new Date());
        customMenuMapper.insert(menu);
    }

    private CustomMenu findObjectDirectory() {
        List<CustomMenu> result = customMenuMapper.selectList(new LambdaQueryWrapper<CustomMenu>()
            .eq(CustomMenu::getMenuType, "DIRECTORY")
            .eq(CustomMenu::getMenuName, OBJECT_MENU_DIRECTORY)
            .orderByAsc(CustomMenu::getId)
            .last("LIMIT 1"));
        return result.isEmpty() ? null : result.get(0);
    }

    private CustomMenu createObjectDirectory() {
        CustomMenu directory = new CustomMenu();
        directory.setParentId(ROOT_PARENT_ID);
        directory.setMenuName(OBJECT_MENU_DIRECTORY);
        directory.setMenuType("DIRECTORY");
        directory.setIcon("Folder");
        directory.setSort(nextSiblingSort(ROOT_PARENT_ID));
        directory.setStatus(1);
        directory.setVisible(1);
        directory.setRemark("由自定义对象自动生成");
        directory.setCreatedAt(new Date());
        directory.setUpdatedAt(new Date());
        customMenuMapper.insert(directory);
        return directory;
    }

    /** 同级现有最大 sort + 1，新菜单排在末尾；同级为空时从 1 开始 */
    private int nextSiblingSort(Long parentId) {
        List<CustomMenu> last = customMenuMapper.selectList(new LambdaQueryWrapper<CustomMenu>()
            .eq(CustomMenu::getParentId, parentId)
            .orderByDesc(CustomMenu::getSort)
            .last("LIMIT 1"));
        return last.isEmpty() ? 1 : last.get(0).getSort() + 1;
    }

    @Override
    public ObjectVO update(String apiName, UpdateObjectRequest request) {
        CustomObject object = requireByApiName(apiName);
        if (request.getObjectName() != null) {
            object.setObjectName(request.getObjectName());
        }
        if (request.getDescription() != null) {
            object.setDescription(request.getDescription());
        }
        if (request.getRemark() != null) {
            object.setRemark(request.getRemark());
        }
        if (request.getIcon() != null) {
            object.setIcon(request.getIcon());
        }
        if (request.getSort() != null) {
            object.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            object.setStatus(request.getStatus());
        }
        object.setUpdatedAt(new Date());
        updateById(object);
        return ObjectVO.from(object);
    }

    /**
     * 数据库无外键，按 字段 → 记录 → 对象菜单 → 对象 的顺序在事务内级联删除
     */
    @Override
    @Transactional
    public void deleteByApiName(String apiName) {
        CustomObject object = requireByApiName(apiName);
        // 被其他对象的 LOOKUP / REFERENCE 字段引用时禁止删除，避免关联悬空
        long relationFieldCount = customFieldMapper.selectCount(new LambdaQueryWrapper<CustomField>()
            .in(CustomField::getFieldType, "LOOKUP", "REFERENCE")
            .eq(CustomField::getRelationObjectId, object.getId()));
        if (relationFieldCount > 0) {
            throw BusinessException.badRequest("该对象正被关联/引用字段使用，无法删除");
        }
        long referenceFieldCount = customFieldMapper.selectCount(new LambdaQueryWrapper<CustomField>()
            .eq(CustomField::getFieldType, "REFERENCE")
            .like(CustomField::getConfigJson, "\"targetObjectApiName\":\"" + apiName + "\""));
        if (referenceFieldCount > 0) {
            throw BusinessException.badRequest("该对象正被关联字段引用，无法删除");
        }
        customFieldMapper.delete(new LambdaQueryWrapper<CustomField>().eq(CustomField::getObjectId, object.getId()));
        customRecordMapper.delete(new LambdaQueryWrapper<CustomRecord>().eq(CustomRecord::getObjectId, object.getId()));
        // 对象菜单生命周期与对象绑定，一起删除（含手动创建的 OBJECT 菜单）
        customMenuMapper.delete(new LambdaQueryWrapper<CustomMenu>()
            .eq(CustomMenu::getMenuType, "OBJECT")
            .eq(CustomMenu::getObjectApiName, apiName));
        removeById(object.getId());
    }
}
