-- V3.0.0_17: 上下游资源的业务主体改为非必填
ALTER TABLE ad_upstream_agent
    MODIFY COLUMN `business_entity_id` VARCHAR(32) NULL COMMENT '业务主体';

ALTER TABLE ad_downstream_media
    MODIFY COLUMN `business_entity_id` VARCHAR(32) NULL COMMENT '业务主体';
