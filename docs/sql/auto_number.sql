-- 自动编号字段（AUTO_NUMBER）
-- 每个自动编号字段一行序列，取号通过 UPDATE 原子递增，避免并发重号

CREATE TABLE `custom_sequence` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `field_id` BIGINT NOT NULL COMMENT '自动编号字段ID',
    `current_value` BIGINT NOT NULL DEFAULT 0 COMMENT '当前已用序号',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_field_id` (`field_id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='自动编号序列';
