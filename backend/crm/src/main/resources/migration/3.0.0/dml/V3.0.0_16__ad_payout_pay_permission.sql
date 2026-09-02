-- ============================================================
-- 广告付款单付款权限分配（DML）
-- 版本: 3.0.0  描述: AD_PAYOUT:PAY 权限授予 org_admin / ROLE_BOSS / ROLE_FINANCE
-- 说明:
--   1) 新权限点 AD_PAYOUT:PAY（付款：待付款 → 已付款），需在「系统-角色权限」中分配。
--   2) 三处一致红线：PermissionConstants.java == permission.json == 本脚本。
--   3) 幂等：WHERE NOT EXISTS 防重复。
-- ============================================================
SET SESSION innodb_lock_wait_timeout = 7200;

-- org_admin / ROLE_BOSS —— 授予付款权限
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), r.role_id, 'AD_PAYOUT:PAY'
FROM (SELECT 'org_admin' AS role_id UNION ALL SELECT 'ROLE_BOSS') r
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission p
    WHERE p.role_id = r.role_id AND p.permission_id = 'AD_PAYOUT:PAY'
);

-- ROLE_FINANCE（财务）—— 授予付款权限
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), 'ROLE_FINANCE', 'AD_PAYOUT:PAY'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission p
    WHERE p.role_id = 'ROLE_FINANCE' AND p.permission_id = 'AD_PAYOUT:PAY'
);

-- ROLE_MEDIA（媒介）—— 不授予付款权限（保持只读）

SET SESSION innodb_lock_wait_timeout = DEFAULT;