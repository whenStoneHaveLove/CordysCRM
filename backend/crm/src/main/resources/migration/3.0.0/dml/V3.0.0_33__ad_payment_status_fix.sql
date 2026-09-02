-- ============================================================
-- 付款单历史数据状态修正（DML）
-- 版本: 3.0.0.33
-- 背景:
--   付款单状态机由「草稿/待审核/审核通过/驳回」调整为「草稿/待审核/待付款/已付款」，
--   旧数据中 status=20 表示「审核通过」，对应新语义应为「待付款」(20)。
--   用户要求：把已存在的「审核通过」付款单直接置为「已付款」(30)。
-- 说明:
--   1) 仅处理未删除(deleted=0)且当前 status=20 的付款单。
--   2) 幂等：重复执行只影响 status=20 的记录，已为 30 的不受影响。
--   3) 如需同步回写订单 payment_done=1，取消下方「订单回写」段落注释。
-- ============================================================
SET SESSION innodb_lock_wait_timeout = 7200;

-- 付款单：审核通过(20) -> 已付款(30)
UPDATE ad_payment
SET `status` = 30
WHERE `deleted` = 0
  AND `status` = 20;

-- 订单回写（按需开启）：把已付款付款单对应的订单付款状态置为已付
-- UPDATE ad_order o
-- JOIN ad_payment p ON p.order_id = o.id
-- SET o.payment_done = 1
-- WHERE p.deleted = 0
--   AND p.status = 30
--   AND o.deleted = 0;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
