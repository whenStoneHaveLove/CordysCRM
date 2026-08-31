-- 订单实际应付总额（返点后），由各下游客户明细 actual_payable 累加。
-- 工作台「应付」指标改用本字段（返点后实际应付）；订单详情页底部也按返点前/后两列展示。
ALTER TABLE `ad_order`
    ADD COLUMN `actual_media_payable_amount` DECIMAL(15,2) NOT NULL DEFAULT 0
    COMMENT '实际应付总额(返点后,各下游客户actual_payable累加)';
