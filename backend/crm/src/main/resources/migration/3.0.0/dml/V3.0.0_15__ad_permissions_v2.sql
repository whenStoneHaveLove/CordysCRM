-- ============================================================
-- CordysCRM 广告下单系统 V3.1 —— 权限码重构（DML）
-- 版本: 3.0.0  描述: ad_permissions v2
-- 说明:
--   1) 收付款拆模块：旧 AD_PAYMENT:*（7个）废弃，替换为
--        - AD_RECEIPT:*（收款单，5个：READ/CREATE/UPDATE/DELETE/APPROVE）
--        - AD_PAYOUT:*（付款单，5个：READ/CREATE/UPDATE/DELETE/APPROVE）
--   2) 订单新增动作权限：AD_ORDER:CONFIRM_EXECUTE（确认执行，媒介操作）
--   3) 合同新增归档审批权限：AD_CONTRACT:ARCHIVE_APPROVE（双盖审核，管理组操作）
--   4) 幂等：INSERT ... SELECT ... WHERE NOT EXISTS；删除旧码用 DELETE（幂等）。
--   5) 三处一致红线：本脚本 == PermissionConstants.java == 前端 permission.json / meta.permissions。
-- ============================================================
SET SESSION innodb_lock_wait_timeout = 7200;

-- ------------------------------------------------------------
-- 0) 删除废弃的旧权限码 AD_PAYMENT:*（幂等）
-- ------------------------------------------------------------
DELETE FROM sys_role_permission
WHERE permission_id IN (
    'AD_PAYMENT:READ',
    'AD_PAYMENT:CONFIRM_PREPAY',
    'AD_PAYMENT:PAY_MEDIA_PREPAY',
    'AD_PAYMENT:INVOICE',
    'AD_PAYMENT:RECEIVE',
    'AD_PAYMENT:PAY_MEDIA_POSTPAY',
    'AD_PAYMENT:RED_INVOICE_CLEAR'
);

-- ------------------------------------------------------------
-- 1) org_admin / ROLE_BOSS —— 授予全部新权限（管理员/管理组）
-- ------------------------------------------------------------
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), r.role_id, c.code
FROM (SELECT 'org_admin' AS role_id UNION ALL SELECT 'ROLE_BOSS') r
JOIN (
    SELECT 'AD_RECEIPT:READ'     AS code UNION ALL
    SELECT 'AD_RECEIPT:CREATE'   UNION ALL
    SELECT 'AD_RECEIPT:UPDATE'   UNION ALL
    SELECT 'AD_RECEIPT:DELETE'   UNION ALL
    SELECT 'AD_RECEIPT:APPROVE'  UNION ALL
    SELECT 'AD_PAYOUT:READ'      UNION ALL
    SELECT 'AD_PAYOUT:CREATE'    UNION ALL
    SELECT 'AD_PAYOUT:UPDATE'    UNION ALL
    SELECT 'AD_PAYOUT:DELETE'    UNION ALL
    SELECT 'AD_PAYOUT:APPROVE'   UNION ALL
    SELECT 'AD_ORDER:CONFIRM_EXECUTE'     UNION ALL
    SELECT 'AD_CONTRACT:ARCHIVE_APPROVE'
) c
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission p
    WHERE p.role_id = r.role_id AND p.permission_id = c.code
);

-- ------------------------------------------------------------
-- 2) ROLE_FINANCE（财务）—— 收款/付款全部动作 + 查看
-- ------------------------------------------------------------
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), 'ROLE_FINANCE', c.code
FROM (
    SELECT 'AD_RECEIPT:READ'     AS code UNION ALL
    SELECT 'AD_RECEIPT:CREATE'   UNION ALL
    SELECT 'AD_RECEIPT:UPDATE'   UNION ALL
    SELECT 'AD_RECEIPT:DELETE'   UNION ALL
    SELECT 'AD_RECEIPT:APPROVE'  UNION ALL
    SELECT 'AD_PAYOUT:READ'      UNION ALL
    SELECT 'AD_PAYOUT:CREATE'    UNION ALL
    SELECT 'AD_PAYOUT:UPDATE'    UNION ALL
    SELECT 'AD_PAYOUT:DELETE'    UNION ALL
    SELECT 'AD_PAYOUT:APPROVE'
) c
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission p
    WHERE p.role_id = 'ROLE_FINANCE' AND p.permission_id = c.code
);

-- ------------------------------------------------------------
-- 3) ROLE_MEDIA（媒介）—— 确认执行动作 + 收款/付款查看
-- ------------------------------------------------------------
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), 'ROLE_MEDIA', c.code
FROM (
    SELECT 'AD_ORDER:CONFIRM_EXECUTE' AS code UNION ALL
    SELECT 'AD_RECEIPT:READ'          UNION ALL
    SELECT 'AD_PAYOUT:READ'
) c
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission p
    WHERE p.role_id = 'ROLE_MEDIA' AND p.permission_id = c.code
);

SET SESSION innodb_lock_wait_timeout = DEFAULT;
