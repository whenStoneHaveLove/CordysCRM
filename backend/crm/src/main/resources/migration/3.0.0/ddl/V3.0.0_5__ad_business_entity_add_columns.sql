ALTER TABLE ad_business_entity ADD COLUMN is_cross_entity TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否跨主体';
ALTER TABLE ad_business_entity ADD COLUMN remark VARCHAR(255) DEFAULT NULL COMMENT '备注';
