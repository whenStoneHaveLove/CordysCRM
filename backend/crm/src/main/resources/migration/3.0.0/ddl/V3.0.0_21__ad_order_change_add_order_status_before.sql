-- 发起改单时订单切入改单审核中(60)并记录改单前状态，驳回/执行后恢复该状态
ALTER TABLE ad_order_change
    ADD COLUMN order_status_before TINYINT NULL COMMENT '改单前订单主状态(用于改单结束后恢复订单原状态流转)';
