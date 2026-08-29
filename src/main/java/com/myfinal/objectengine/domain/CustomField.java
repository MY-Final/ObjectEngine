package com.myfinal.objectengine.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 自定义对象字段
 * @TableName custom_field
 */
@TableName(value ="custom_field")
@Data
public class CustomField {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属对象ID
     */
    private Long objectId;

    /**
     * 字段API名称，例如 amount
     */
    private String apiName;

    /**
     * 字段名称，例如 项目金额
     */
    private String fieldName;

    /**
     * 字段类型：TEXT/TEXTAREA/NUMBER/MONEY/DATE/SELECT
     */
    private String fieldType;

    /**
     * 字段描述
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否必填：1是，0否
     */
    private Integer requiredFlag;

    /**
     * 是否唯一：1是，0否
     */
    private Integer uniqueFlag;

    /**
     * 是否可搜索：1是，0否
     */
    private Integer searchableFlag;

    /**
     * 是否可排序：1是，0否
     */
    private Integer sortableFlag;

    /**
     * 字段排序
     */
    private Integer sort;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 字段扩展配置
     */
    private Object configJson;

    /**
     * 状态：1启用，0停用
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 最后修改人ID
     */
    private Long updatedBy;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        CustomField other = (CustomField) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getObjectId() == null ? other.getObjectId() == null : this.getObjectId().equals(other.getObjectId()))
            && (this.getApiName() == null ? other.getApiName() == null : this.getApiName().equals(other.getApiName()))
            && (this.getFieldName() == null ? other.getFieldName() == null : this.getFieldName().equals(other.getFieldName()))
            && (this.getFieldType() == null ? other.getFieldType() == null : this.getFieldType().equals(other.getFieldType()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
            && (this.getRequiredFlag() == null ? other.getRequiredFlag() == null : this.getRequiredFlag().equals(other.getRequiredFlag()))
            && (this.getUniqueFlag() == null ? other.getUniqueFlag() == null : this.getUniqueFlag().equals(other.getUniqueFlag()))
            && (this.getSearchableFlag() == null ? other.getSearchableFlag() == null : this.getSearchableFlag().equals(other.getSearchableFlag()))
            && (this.getSortableFlag() == null ? other.getSortableFlag() == null : this.getSortableFlag().equals(other.getSortableFlag()))
            && (this.getSort() == null ? other.getSort() == null : this.getSort().equals(other.getSort()))
            && (this.getDefaultValue() == null ? other.getDefaultValue() == null : this.getDefaultValue().equals(other.getDefaultValue()))
            && (this.getConfigJson() == null ? other.getConfigJson() == null : this.getConfigJson().equals(other.getConfigJson()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreatedBy() == null ? other.getCreatedBy() == null : this.getCreatedBy().equals(other.getCreatedBy()))
            && (this.getUpdatedBy() == null ? other.getUpdatedBy() == null : this.getUpdatedBy().equals(other.getUpdatedBy()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getObjectId() == null) ? 0 : getObjectId().hashCode());
        result = prime * result + ((getApiName() == null) ? 0 : getApiName().hashCode());
        result = prime * result + ((getFieldName() == null) ? 0 : getFieldName().hashCode());
        result = prime * result + ((getFieldType() == null) ? 0 : getFieldType().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        result = prime * result + ((getRequiredFlag() == null) ? 0 : getRequiredFlag().hashCode());
        result = prime * result + ((getUniqueFlag() == null) ? 0 : getUniqueFlag().hashCode());
        result = prime * result + ((getSearchableFlag() == null) ? 0 : getSearchableFlag().hashCode());
        result = prime * result + ((getSortableFlag() == null) ? 0 : getSortableFlag().hashCode());
        result = prime * result + ((getSort() == null) ? 0 : getSort().hashCode());
        result = prime * result + ((getDefaultValue() == null) ? 0 : getDefaultValue().hashCode());
        result = prime * result + ((getConfigJson() == null) ? 0 : getConfigJson().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreatedBy() == null) ? 0 : getCreatedBy().hashCode());
        result = prime * result + ((getUpdatedBy() == null) ? 0 : getUpdatedBy().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", objectId=").append(objectId);
        sb.append(", apiName=").append(apiName);
        sb.append(", fieldName=").append(fieldName);
        sb.append(", fieldType=").append(fieldType);
        sb.append(", description=").append(description);
        sb.append(", remark=").append(remark);
        sb.append(", requiredFlag=").append(requiredFlag);
        sb.append(", uniqueFlag=").append(uniqueFlag);
        sb.append(", searchableFlag=").append(searchableFlag);
        sb.append(", sortableFlag=").append(sortableFlag);
        sb.append(", sort=").append(sort);
        sb.append(", defaultValue=").append(defaultValue);
        sb.append(", configJson=").append(configJson);
        sb.append(", status=").append(status);
        sb.append(", createdBy=").append(createdBy);
        sb.append(", updatedBy=").append(updatedBy);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}