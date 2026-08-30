-- 关联字段（LOOKUP）与引用字段（REFERENCE）
-- 执行顺序：加列 → 存量快照式关联迁移 → 记录数据迁移

ALTER TABLE `custom_field`
    ADD COLUMN `relation_object_id` BIGINT DEFAULT NULL COMMENT '关联对象ID（LOOKUP：目标对象；REFERENCE：由关联的 LOOKUP 字段推导）' AFTER `option_set_id`,
    ADD COLUMN `relation_field_id` BIGINT DEFAULT NULL COMMENT '关联的 LOOKUP 字段ID（REFERENCE 使用，必须属于当前对象）' AFTER `relation_object_id`,
    ADD COLUMN `reference_field_id` BIGINT DEFAULT NULL COMMENT '引用的目标字段ID（REFERENCE 使用，属于关联对象）' AFTER `relation_field_id`;

-- 存量迁移：旧版 REFERENCE（快照式关联，configJson 存 targetObjectApiName）升级为 LOOKUP
UPDATE custom_field f
JOIN custom_object o
  ON o.api_name = JSON_UNQUOTE(JSON_EXTRACT(f.config_json, '$.targetObjectApiName'))
SET f.field_type = 'LOOKUP',
    f.relation_object_id = o.id
WHERE f.field_type = 'REFERENCE';

-- 记录数据迁移：把 { "recordId": x, "text": y } 快照改写为记录 ID x。
-- JSON 路径无法参数化，需要对每个存量关联字段执行一次（把 <api_name> 替换为字段 API 名称）：
-- UPDATE custom_record
-- SET data_json = JSON_SET(data_json, '$.<api_name>',
--       JSON_EXTRACT(data_json, CONCAT('$.', '<api_name>', '.recordId')))
-- WHERE JSON_EXTRACT(data_json, CONCAT('$.', '<api_name>', '.recordId')) IS NOT NULL;
