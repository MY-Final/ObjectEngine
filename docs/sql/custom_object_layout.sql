-- Object Engine V1 Phase 4：自定义对象布局配置表
-- 规范见 docs/controller/Phase 4 第十节；无外键，通过 object_api_name 关联

CREATE TABLE custom_object_layout (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    object_api_name VARCHAR(100) NOT NULL COMMENT '对象API名称',
    layout_name VARCHAR(100) NOT NULL COMMENT '布局名称',
    layout_type VARCHAR(30) NOT NULL COMMENT '布局类型：FRONTEND/BACKEND',
    config_json JSON NOT NULL COMMENT '布局配置JSON',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用，1启用',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_object_layout_type (object_api_name, layout_type)
) COMMENT='自定义对象布局配置';
