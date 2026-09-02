-- 付款单类型：订单类型 / 非订单类型
-- 非订单类型的付款单没有关联订单，order_id 需允许为空
ALTER TABLE ad_payment
    MODIFY COLUMN `order_id` VARCHAR(32) NULL COMMENT '关联订单(非订单类型时为空)';

ALTER TABLE ad_payment
    ADD COLUMN `bill_type` TINYINT NOT NULL DEFAULT 10 COMMENT '付款单类型:10订单类型/20非订单类型' AFTER `payment_no`;
