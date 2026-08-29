CREATE
DATABASE IF NOT EXISTS object_engine
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE
object_engine;


-- ============================================================
-- 1. 自定义对象
-- ============================================================

CREATE TABLE custom_object
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    api_name    VARCHAR(100) NOT NULL COMMENT '对象API名称，例如 project__c',
    object_name VARCHAR(100) NOT NULL COMMENT '对象名称，例如 项目',

    description VARCHAR(500)          DEFAULT NULL COMMENT '对象描述',
    remark      VARCHAR(500)          DEFAULT NULL COMMENT '备注',

    icon        VARCHAR(100)          DEFAULT NULL COMMENT '对象图标',

    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',

    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',

    created_by  BIGINT                DEFAULT NULL COMMENT '创建人ID',
    updated_by  BIGINT                DEFAULT NULL COMMENT '最后修改人ID',

    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),

    UNIQUE KEY uk_api_name (api_name),

    KEY         idx_status (status),
    KEY         idx_sort (sort)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='自定义对象';


-- ============================================================
-- 2. 自定义字段
-- ============================================================

CREATE TABLE custom_field
(
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    object_id       BIGINT       NOT NULL COMMENT '所属对象ID',

    api_name        VARCHAR(100) NOT NULL COMMENT '字段API名称，例如 amount',
    field_name      VARCHAR(100) NOT NULL COMMENT '字段名称，例如 项目金额',

    field_type      VARCHAR(50)  NOT NULL COMMENT '字段类型：TEXT/TEXTAREA/NUMBER/MONEY/DATE/SELECT',

    description     VARCHAR(500)          DEFAULT NULL COMMENT '字段描述',
    remark          VARCHAR(500)          DEFAULT NULL COMMENT '备注',

    required_flag   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否必填：1是，0否',
    unique_flag     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否唯一：1是，0否',

    searchable_flag TINYINT      NOT NULL DEFAULT 0 COMMENT '是否可搜索：1是，0否',
    sortable_flag   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否可排序：1是，0否',

    sort            INT          NOT NULL DEFAULT 0 COMMENT '字段排序',

    default_value   TEXT                  DEFAULT NULL COMMENT '默认值',

    config_json     JSON                  DEFAULT NULL COMMENT '字段扩展配置',

    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',

    created_by      BIGINT                DEFAULT NULL COMMENT '创建人ID',
    updated_by      BIGINT                DEFAULT NULL COMMENT '最后修改人ID',

    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),

    UNIQUE KEY uk_object_api_name (object_id, api_name),

    KEY             idx_object_id (object_id),
    KEY             idx_object_sort (object_id, sort),
    KEY             idx_object_status (object_id, status)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='自定义对象字段';


-- ============================================================
-- 3. 自定义对象记录
-- ============================================================

CREATE TABLE custom_record
(
    id         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    object_id  BIGINT   NOT NULL COMMENT '所属对象ID',

    data_json  JSON     NOT NULL COMMENT '自定义字段数据',

    remark     VARCHAR(500)      DEFAULT NULL COMMENT '备注',

    created_by BIGINT            DEFAULT NULL COMMENT '创建人ID',
    updated_by BIGINT            DEFAULT NULL COMMENT '最后修改人ID',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),

    KEY        idx_object_id (object_id),
    KEY        idx_object_created_at (object_id, created_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='自定义对象数据';