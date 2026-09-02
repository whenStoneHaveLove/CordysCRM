-- ============================================================
-- 广告付款单付款权限分配（DML）
-- 版本: 3.0.0.32  描述: AD_PAYOUT:PAY 权限授予 org_admin
-- 说明:
--   1) 新权限点 AD_PAYOUT:PAY（付款：待付款 → 已付款），需在「系统-角色权限」中分配。
--   2) 三处一致红线：PermissionConstants.java == permission.json == 本脚本。
--   3) 幂等：WHERE NOT EXISTS 防重复。
--   4) 仅保证管理员（org_admin）默认拥有该权限；其他角色由用户在界面自行分配。
-- ============================================================
SET SESSION innodb_lock_wait_timeout = 7200;

-- org_admin（管理员）—— 授予付款权限
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), 'org_admin', 'AD_PAYOUT:PAY'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission p
    WHERE p.role_id = 'org_admin' AND p.permission_id = 'AD_PAYOUT:PAY'
);

SET SESSION innodb_lock_wait_timeout = DEFAULT;
