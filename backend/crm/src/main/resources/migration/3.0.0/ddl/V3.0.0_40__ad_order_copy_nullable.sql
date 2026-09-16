-- ============================================================
-- CordysCRM 广告下单系统 —— 订单复制（生成草稿订单）放开非空约束（DDL）
-- 版本: 3.0.0  描述: ad_order_copy_nullable
-- 背景:
--   订单列表新增「复制」能力：按用户勾选的字段生成一张草稿订单，未勾选的字段一律置空。
--   原 ad_order 表对业务字段做了大量 NOT NULL 约束（下单表单必填），
--   复制出的草稿订单会因这些约束插入失败，故此处放开为可空。
-- 说明:
--   1) 放开所有「业务可缺省」的字段（含金额列）；后端仍会为金额派生列统一写入 0，
--      仅当来源订单字段未被勾选时保留 NULL，语义更准确。
--   2) 前端下单表单的非空校验保持不变，只有复制走宽松入库。
--   3) 已存在的 payment_method 可空（见 V3.0.0_26），此处不重复处理。
--   4) no_rebate_amount / invoiced_amount / received_amount / media_paid_amount 等
--      自带 NOT NULL DEFAULT 0，无需放开。
-- ============================================================
ALTER TABLE `ad_order`
    MODIFY COLUMN `business_entity_id`      VARCHAR(32)   NULL DEFAULT NULL COMMENT '业务主体(下单必选,L-29;复制草稿可空)',
    MODIFY COLUMN `customer_id`             VARCHAR(32)   NULL DEFAULT NULL COMMENT '客户id(复制草稿可空)',
    MODIFY COLUMN `order_type`              TINYINT       NULL DEFAULT NULL COMMENT '订单类型:10框架合同/20单笔合同(复制草稿可空)',
    MODIFY COLUMN `total_amount`            DECIMAL(15,2) NULL DEFAULT NULL COMMENT '订单金额(复制草稿可空)',
    MODIFY COLUMN `rebate_mode`             TINYINT       NULL DEFAULT NULL COMMENT '返点方式:10比例/20固定金额(复制草稿可空)',
    MODIFY COLUMN `rebate_value`            DECIMAL(10,2) NULL DEFAULT NULL COMMENT '返点值(复制草稿可空)',
    MODIFY COLUMN `rebate_amount`           DECIMAL(15,2) NULL DEFAULT NULL COMMENT '返点金额(自动,复制草稿可空)',
    MODIFY COLUMN `receivable_amount`       DECIMAL(15,2) NULL DEFAULT NULL COMMENT '应收金额=总额-返点(复制草稿可空)',
    MODIFY COLUMN `media_payable_amount`    DECIMAL(15,2) NULL DEFAULT NULL COMMENT '应付总额(下游口径,L-28;复制草稿可空)',
    MODIFY COLUMN `delivery_start_date`     DATE          NULL DEFAULT NULL COMMENT '投放起始日(复制草稿可空)',
    MODIFY COLUMN `delivery_end_date`       DATE          NULL DEFAULT NULL COMMENT '投放结束日(复制草稿可空)',
    MODIFY COLUMN `receipt_method`          TINYINT       NULL DEFAULT NULL COMMENT '收款方式:10预收/20账期(复制草稿可空)';
