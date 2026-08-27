-- 订单-下游媒体关联表：新增每个下游客户独立的付款方式字段
-- 付款方式（预付/后付）下放到每个客户，计算基数按该客户应付金额
ALTER TABLE `ad_order_downstream_media`
  ADD COLUMN `payment_method` tinyint DEFAULT NULL COMMENT '付款方式:10-预付/20-后付',
  ADD COLUMN `payment_prepay_mode` tinyint DEFAULT NULL COMMENT '预付模式:10-比例/20-固定金额',
  ADD COLUMN `payment_prepay_ratio` decimal(18,2) DEFAULT NULL COMMENT '预付比例(%)',
  ADD COLUMN `payment_prepay_amount` decimal(18,2) DEFAULT NULL COMMENT '预付金额(基数=该客户应付金额,自动计算)',
  ADD COLUMN `payment_prepay_deadline` bigint DEFAULT NULL COMMENT '预付截止日(时间戳)',
  ADD COLUMN `payment_postpay_trigger` tinyint DEFAULT NULL COMMENT '后付触发:10-收到上游全款/20-执行完成X天',
  ADD COLUMN `payment_postpay_days` int DEFAULT NULL COMMENT '后付天数(执行完成X天后付款)';
