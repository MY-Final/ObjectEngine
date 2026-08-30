-- 通用选项集（Global Option Set）
-- 执行顺序：建表 → custom_field 加列 → 注册菜单

CREATE TABLE `custom_option_set` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '选项集名称',
    `api_name` VARCHAR(100) NOT NULL COMMENT '选项集API名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '选项集描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1启用',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_api_name` (`api_name`),
    KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='通用选项集';

CREATE TABLE `custom_option` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `option_set_id` BIGINT NOT NULL COMMENT '所属选项集ID',
    `label` VARCHAR(100) NOT NULL COMMENT '选项显示名称',
    `value` VARCHAR(100) NOT NULL COMMENT '选项实际值',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1启用',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认选项：0否，1是',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '选项描述',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_option_set_value` (`option_set_id`, `value`),
    KEY `idx_option_set_id` (`option_set_id`),
    KEY `idx_option_set_status_sort` (`option_set_id`, `status`, `sort`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='通用选项';

-- 字段级选项来源：LOCAL 字段自有选项（默认），GLOBAL 引用通用选项集
ALTER TABLE `custom_field`
    ADD COLUMN `option_source` VARCHAR(20) NOT NULL DEFAULT 'LOCAL' COMMENT '选项来源：LOCAL字段自有，GLOBAL通用选项集' AFTER `config_json`,
    ADD COLUMN `option_set_id` BIGINT DEFAULT NULL COMMENT '通用选项集ID（GLOBAL时使用）' AFTER `option_source`;

-- 后台菜单入口（挂在「后台管理」目录下，排序在菜单管理之后）
INSERT INTO custom_menu
  (parent_id, menu_name, menu_type, route_path, icon, sort, status, visible, target, remark, created_at, updated_at)
SELECT id, '通用选项集', 'LINK', '/admin/option-sets', 'Collection', 3, 1, 1, '_self', '通用选项集管理', NOW(), NOW()
FROM custom_menu
WHERE menu_name = '后台管理' AND menu_type = 'DIRECTORY'
LIMIT 1;
