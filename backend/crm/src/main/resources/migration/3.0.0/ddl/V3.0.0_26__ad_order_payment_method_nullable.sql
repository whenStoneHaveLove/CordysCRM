-- 订单付款方式已改为由各下游客户付款明细推导：存在任一客户为预付则订单为预付，
-- 仅当所有客户均为后付时订单才为后付；无下游客户明细时该字段为空。
-- 因此 ad_order.payment_method 需要允许为空。
ALTER TABLE `ad_order`
    MODIFY COLUMN `payment_method` TINYINT NULL DEFAULT NULL COMMENT '付款方式:10预付/20后付(由下游客户明细推导,可空)';
