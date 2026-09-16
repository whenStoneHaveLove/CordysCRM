-- ============================================================
-- CordysCRM 广告下单系统 —— 修正 ad_payout_invoice 列类型（DDL）
-- 版本: 3.0.0  描述: fix_ad_payout_invoice_columns
-- 背景: V3.0.0_38 建表时把 id/create_user/update_user 写成了 BIGINT、
--       create_time/update_time 写成了 DATETIME，与 BaseModel（String/Long）不一致，
--       导致新增发票时报 "Incorrect integer value: 'admin' for column 'create_user'"。
-- 说明: 幂等 MODIFY，全新库（38 已正确）执行亦无副作用。
-- ============================================================
SET SESSION innodb_lock_wait_timeout = 7200;

ALTER TABLE `ad_payout_invoice`
    MODIFY COLUMN `id`          VARCHAR(32) NOT NULL COMMENT '主键',
    MODIFY COLUMN `payout_id`   VARCHAR(32) NOT NULL COMMENT '付款单ID(ad_payout.id)',
    MODIFY COLUMN `create_user` VARCHAR(32)          DEFAULT NULL COMMENT '创建人',
    MODIFY COLUMN `update_user` VARCHAR(32)          DEFAULT NULL COMMENT '修改人',
    MODIFY COLUMN `create_time` BIGINT               DEFAULT NULL COMMENT '创建时间',
    MODIFY COLUMN `update_time` BIGINT               DEFAULT NULL COMMENT '更新时间';

SET SESSION innodb_lock_wait_timeout = DEFAULT;
