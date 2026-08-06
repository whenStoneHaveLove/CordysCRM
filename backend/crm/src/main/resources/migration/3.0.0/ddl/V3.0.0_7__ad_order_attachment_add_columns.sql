-- ad_order_attachment 表补充 update_user / update_time 列（BaseModel 继承需要）
ALTER TABLE ad_order_attachment
    ADD COLUMN `update_user` VARCHAR(32) NULL COMMENT '修改人' AFTER `create_user`,
    ADD COLUMN `update_time` BIGINT NULL COMMENT '更新时间' AFTER `create_time`;
