package com.myfinal.objectengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myfinal.objectengine.common.BusinessException;
import com.myfinal.objectengine.common.PageResult;
import com.myfinal.objectengine.domain.CustomField;
import com.myfinal.objectengine.domain.CustomOption;
import com.myfinal.objectengine.domain.CustomOptionSet;
import com.myfinal.objectengine.dto.CreateOptionRequest;
import com.myfinal.objectengine.dto.CreateOptionSetRequest;
import com.myfinal.objectengine.dto.OptionSetQueryRequest;
import com.myfinal.objectengine.dto.UpdateOptionRequest;
import com.myfinal.objectengine.dto.UpdateOptionSetRequest;
import com.myfinal.objectengine.mapper.CustomFieldMapper;
import com.myfinal.objectengine.mapper.CustomOptionMapper;
import com.myfinal.objectengine.mapper.CustomOptionSetMapper;
import com.myfinal.objectengine.service.CustomOptionSetService;
import com.myfinal.objectengine.vo.OptionSetVO;
import com.myfinal.objectengine.vo.OptionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomOptionSetServiceImpl extends ServiceImpl<CustomOptionSetMapper, CustomOptionSet>
    implements CustomOptionSetService {

    private final CustomOptionMapper customOptionMapper;
    private final CustomFieldMapper customFieldMapper;

    public CustomOptionSetServiceImpl(CustomOptionMapper customOptionMapper, CustomFieldMapper customFieldMapper) {
        this.customOptionMapper = customOptionMapper;
        this.customFieldMapper = customFieldMapper;
    }

    @Override
    public PageResult<OptionSetVO> page(OptionSetQueryRequest query) {
        int p = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int ps = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : query.getPageSize();
        String keyword = query.getKeyword();
        LambdaQueryWrapper<CustomOptionSet> wrapper = new LambdaQueryWrapper<CustomOptionSet>()
            .and(keyword != null && !keyword.isBlank(),
                q -> q.like(CustomOptionSet::getName, keyword).or().like(CustomOptionSet::getApiName, keyword))
            .eq(query.getStatus() != null, CustomOptionSet::getStatus, query.getStatus())
            .orderByAsc(CustomOptionSet::getSort)
            .orderByAsc(CustomOptionSet::getId);
        Page<CustomOptionSet> result = page(new Page<>(p, ps), wrapper);
        List<OptionSetVO> records = result.getRecords().stream().map(OptionSetVO::from).toList();
        fillOptionsSummary(records);
        return PageResult.of(records, result.getTotal(), p, ps);
    }

    /** 分页列表补充选项摘要，便于不进弹窗就能看到选项内容 */
    private void fillOptionsSummary(List<OptionSetVO> records) {
        List<Long> setIds = records.stream().map(OptionSetVO::getId).toList();
        if (setIds.isEmpty()) {
            return;
        }
        Map<Long, String> summary = new HashMap<>();
        customOptionMapper.selectList(new LambdaQueryWrapper<CustomOption>()
                .in(CustomOption::getOptionSetId, setIds)
                .eq(CustomOption::getStatus, 1)
                .orderByAsc(CustomOption::getSort)
                .orderByAsc(CustomOption::getId))
            .forEach(option -> summary.merge(option.getOptionSetId(), option.getLabel(),
                (existing, label) -> existing + "、" + label));
        for (OptionSetVO vo : records) {
            vo.setOptionsSummary(summary.get(vo.getId()));
        }
    }

    @Override
    public OptionSetVO requireDetail(Long id) {
        CustomOptionSet set = requireEntity(id);
        OptionSetVO vo = OptionSetVO.from(set);
        // 管理端详情返回全部选项（含停用），运行时请使用 listEnabledOptions
        vo.setOptions(listOptions(id, null).stream().map(OptionVO::from).toList());
        return vo;
    }

    @Override
    public Long create(CreateOptionSetRequest request) {
        long count = count(new LambdaQueryWrapper<CustomOptionSet>()
            .eq(CustomOptionSet::getApiName, request.getApiName()));
        if (count > 0) {
            throw BusinessException.badRequest("选项集API名称已存在：" + request.getApiName());
        }
        CustomOptionSet set = new CustomOptionSet();
        set.setName(request.getName());
        set.setApiName(request.getApiName());
        set.setDescription(request.getDescription());
        set.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        set.setSort(request.getSort() == null ? 0 : request.getSort());
        set.setRemark(request.getRemark());
        set.setCreatedAt(new Date());
        set.setUpdatedAt(new Date());
        save(set);
        return set.getId();
    }

    @Override
    public void update(Long id, UpdateOptionSetRequest request) {
        CustomOptionSet set = requireEntity(id);
        if (request.getName() != null) {
            set.setName(request.getName());
        }
        if (request.getDescription() != null) {
            set.setDescription(request.getDescription());
        }
        if (request.getRemark() != null) {
            set.setRemark(request.getRemark());
        }
        if (request.getSort() != null) {
            set.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            set.setStatus(request.getStatus());
        }
        set.setUpdatedAt(new Date());
        updateById(set);
    }

    /**
     * 引用保护：存在 custom_field 引用时禁止删除，避免 GLOBAL 字段运行时悬空
     */
    @Override
    @Transactional
    public void delete(Long id) {
        requireEntity(id);
        long referenceCount = customFieldMapper.selectCount(new LambdaQueryWrapper<CustomField>()
            .eq(CustomField::getOptionSetId, id));
        if (referenceCount > 0) {
            throw BusinessException.badRequest("该选项集已被字段引用，无法删除。");
        }
        customOptionMapper.delete(new LambdaQueryWrapper<CustomOption>()
            .eq(CustomOption::getOptionSetId, id));
        removeById(id);
    }

    @Override
    public List<OptionVO> listEnabledOptions(Long optionSetId) {
        requireEntity(optionSetId);
        return listOptions(optionSetId, 1).stream().map(OptionVO::from).toList();
    }

    @Override
    public Long createOption(Long optionSetId, CreateOptionRequest request) {
        requireEntity(optionSetId);
        checkValueUnique(optionSetId, request.getValue(), null);

        CustomOption option = new CustomOption();
        option.setOptionSetId(optionSetId);
        option.setLabel(request.getLabel());
        option.setValue(request.getValue());
        option.setSort(request.getSort() == null ? 0 : request.getSort());
        option.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        option.setIsDefault(request.getIsDefault() == null ? 0 : request.getIsDefault());
        option.setDescription(request.getDescription());
        option.setRemark(request.getRemark());
        option.setCreatedAt(new Date());
        option.setUpdatedAt(new Date());
        customOptionMapper.insert(option);
        clearOtherDefaults(optionSetId, option.getId(), option.getIsDefault());
        return option.getId();
    }

    @Override
    public void updateOption(Long optionSetId, Long optionId, UpdateOptionRequest request) {
        CustomOption option = requireOption(optionSetId, optionId);
        // value 可能已被 Record 引用，创建后不可修改（规则 4），只允许调整展示与排序属性
        if (request.getLabel() != null) {
            option.setLabel(request.getLabel());
        }
        if (request.getSort() != null) {
            option.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            option.setStatus(request.getStatus());
        }
        if (request.getIsDefault() != null) {
            option.setIsDefault(request.getIsDefault());
        }
        if (request.getDescription() != null) {
            option.setDescription(request.getDescription());
        }
        if (request.getRemark() != null) {
            option.setRemark(request.getRemark());
        }
        option.setUpdatedAt(new Date());
        customOptionMapper.updateById(option);
        clearOtherDefaults(optionSetId, option.getId(), option.getIsDefault());
    }

    @Override
    public void deleteOption(Long optionSetId, Long optionId) {
        CustomOption option = requireOption(optionSetId, optionId);
        customOptionMapper.deleteById(option.getId());
        // 删除默认选项后允许选项集暂无默认项，保证「最多一个默认」即可
    }

    private List<CustomOption> listOptions(Long optionSetId, Integer status) {
        return customOptionMapper.selectList(new LambdaQueryWrapper<CustomOption>()
            .eq(CustomOption::getOptionSetId, optionSetId)
            .eq(status != null, CustomOption::getStatus, status)
            .orderByAsc(CustomOption::getSort)
            .orderByAsc(CustomOption::getId));
    }

    private void checkValueUnique(Long optionSetId, String value, Long excludeId) {
        long count = customOptionMapper.selectCount(new LambdaQueryWrapper<CustomOption>()
            .eq(CustomOption::getOptionSetId, optionSetId)
            .eq(CustomOption::getValue, value)
            .ne(excludeId != null, CustomOption::getId, excludeId));
        if (count > 0) {
            throw BusinessException.badRequest("选项值已存在：" + value);
        }
    }

    /** 保证一个选项集最多一个默认选项：当前选项为默认时清除同集其他默认标记 */
    private void clearOtherDefaults(Long optionSetId, Long optionId, Integer isDefault) {
        if (isDefault == null || isDefault != 1) {
            return;
        }
        CustomOption cleared = new CustomOption();
        cleared.setIsDefault(0);
        customOptionMapper.update(cleared, new LambdaQueryWrapper<CustomOption>()
            .eq(CustomOption::getOptionSetId, optionSetId)
            .eq(CustomOption::getIsDefault, 1)
            .ne(CustomOption::getId, optionId));
    }

    private CustomOptionSet requireEntity(Long id) {
        CustomOptionSet set = getById(id);
        if (set == null) {
            throw BusinessException.notFound("选项集不存在：" + id);
        }
        return set;
    }

    private CustomOption requireOption(Long optionSetId, Long optionId) {
        CustomOption option = customOptionMapper.selectById(optionId);
        if (option == null || !option.getOptionSetId().equals(optionSetId)) {
            throw BusinessException.notFound("选项不存在：" + optionId);
        }
        return option;
    }
}
