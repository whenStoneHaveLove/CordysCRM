-- 非订单类型的付款单没有关联订单，明细表冗余的 order_id 需允许为空
ALTER TABLE ad_payment_media
    MODIFY COLUMN `order_id` VARCHAR(32) NULL COMMENT '关联订单id(冗余,非订单类型时为空)';
