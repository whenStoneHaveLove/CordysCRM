-- ============================================================
-- CordysCRM 广告下单系统 V3.1 —— 补齐管理员/管理组默认缺失权限（DML）
-- 版本: 3.0.0  描述: fix_missing_default_permissions
-- 说明:
--   1) 上游代理、下游客户、操作日志模块初始化时未授予 org_admin/ROLE_BOSS。
--   2) 改单模块 V3.0.0_2 漏授 SUBMIT 权限。
--   3) 幂等：INSERT ... SELECT ... WHERE NOT EXISTS，重跑安全。
-- ============================================================
SET SESSION innodb_lock_wait_timeout = 7200;

-- ------------------------------------------------------------
-- 1) org_admin / ROLE_BOSS —— 授予缺失模块的完整权限
-- ------------------------------------------------------------
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), r.role_id, c.code
FROM (SELECT 'org_admin' AS role_id UNION ALL SELECT 'ROLE_BOSS') r
JOIN (
    -- 上游代理
    SELECT 'AD_UPSTREAM_AGENT:READ'   AS code UNION ALL
    SELECT 'AD_UPSTREAM_AGENT:CREATE' UNION ALL
    SELECT 'AD_UPSTREAM_AGENT:UPDATE' UNION ALL
    SELECT 'AD_UPSTREAM_AGENT:DELETE' UNION ALL
    -- 下游客户
    SELECT 'AD_DOWNSTREAM_MEDIA:READ'   UNION ALL
    SELECT 'AD_DOWNSTREAM_MEDIA:CREATE' UNION ALL
    SELECT 'AD_DOWNSTREAM_MEDIA:UPDATE' UNION ALL
    SELECT 'AD_DOWNSTREAM_MEDIA:DELETE' UNION ALL
    -- 操作日志
    SELECT 'OPERATION_LOG:READ' UNION ALL
    -- 改单提交审核
    SELECT 'AD_ORDER_CHANGE:SUBMIT'
) c
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission p
    WHERE p.role_id = r.role_id AND p.permission_id = c.code
);

SET SESSION innodb_lock_wait_timeout = DEFAULT;
