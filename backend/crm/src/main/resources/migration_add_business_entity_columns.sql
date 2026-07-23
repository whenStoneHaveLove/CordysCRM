-- 业务主体表补列迁移（无 Flyway/Liquibase，需手动执行）
-- 说明：ad_business_entity 缺 is_cross_entity / remark 两列，导致保存时字段丢失、列表/详情空白。

ALTER TABLE ad_business_entity ADD COLUMN is_cross_entity TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否跨主体';
ALTER TABLE ad_business_entity ADD COLUMN remark VARCHAR(255) DEFAULT NULL COMMENT '备注';
