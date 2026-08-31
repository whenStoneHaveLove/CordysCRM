-- 订单收入相关派生列（保存订单时由后端计算写入，便于列表展示/导出/检索/统计）
-- 应付返点 = 应付金额(media_payable_amount) - 实际应付(actual_media_payable_amount)
-- 订单收入 = 实际应收(receivable_amount) - 实际应付(actual_media_payable_amount)
ALTER TABLE `ad_order`
    ADD COLUMN `media_rebate_amount` DECIMAL(15,2) NOT NULL DEFAULT 0
    COMMENT '应付返点(应付金额-实际应付)',
    ADD COLUMN `order_income_amount` DECIMAL(15,2) NOT NULL DEFAULT 0
    COMMENT '订单收入(实际应收-实际应付)';
