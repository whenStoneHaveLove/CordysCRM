-- 付款单新增付款信息字段（V3.1.1 流程调整）
-- 字段名与后端 entity (AdPayout.payUser/payTime/payRemark) 一致。
ALTER TABLE ad_payment
    ADD COLUMN `pay_user` VARCHAR(32) DEFAULT NULL COMMENT '付款人' AFTER `approve_remark`,
    ADD COLUMN `pay_time` BIGINT DEFAULT NULL COMMENT '付款时间(epoch ms)' AFTER `pay_user`,
    ADD COLUMN `pay_remark` TEXT DEFAULT NULL COMMENT '付款备注' AFTER `pay_time`;