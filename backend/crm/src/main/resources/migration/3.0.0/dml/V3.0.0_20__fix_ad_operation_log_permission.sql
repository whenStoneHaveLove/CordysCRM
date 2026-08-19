-- ============================================================
-- CordysCRM 广告下单系统 V3.1 —— 补齐管理员/管理组广告操作日志查看权限（DML）
-- 版本: 3.0.0  描述: fix_ad_operation_log_permission
-- 说明:
--   广告操作日志（AD_OPERATION_LOG）初始化时未授予 org_admin/ROLE_BOSS，
--   导致管理员角色权限树中该模块未默认勾选。
--   幂等：INSERT ... SELECT ... WHERE NOT EXISTS，重跑安全。
-- ============================================================
SET SESSION innodb_lock_wait_timeout = 7200;

-- ------------------------------------------------------------
-- 1) org_admin / ROLE_BOSS —— 授予广告操作日志查看权限
-- ------------------------------------------------------------
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), r.role_id, c.code
FROM (SELECT 'org_admin' AS role_id UNION ALL SELECT 'ROLE_BOSS') r
JOIN (
    SELECT 'AD_OPERATION_LOG:READ' AS code
) c
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission p
    WHERE p.role_id = r.role_id AND p.permission_id = c.code
);

SET SESSION innodb_lock_wait_timeout = DEFAULT;
