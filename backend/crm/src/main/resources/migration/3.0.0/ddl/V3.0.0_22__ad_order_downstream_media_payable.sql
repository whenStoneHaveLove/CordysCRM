-- 订单-下游媒体关联表：新增每个下游客户的付款返点明细字段
ALTER TABLE `ad_order_downstream_media`
  ADD COLUMN `payable_amount` decimal(18,2) DEFAULT NULL COMMENT '应付金额',
  ADD COLUMN `no_rebate_amount` decimal(18,2) DEFAULT NULL COMMENT '不记返金额',
  ADD COLUMN `rebate_mode` tinyint DEFAULT NULL COMMENT '返点方式:10-比例/20-固定金额',
  ADD COLUMN `rebate_value` decimal(18,2) DEFAULT NULL COMMENT '返点值:比例时存百分比数值,固定金额时存金额',
  ADD COLUMN `rebate_amount` decimal(18,2) DEFAULT NULL COMMENT '返点金额(自动计算)',
  ADD COLUMN `actual_payable` decimal(18,2) DEFAULT NULL COMMENT '实际应付(自动计算)';
