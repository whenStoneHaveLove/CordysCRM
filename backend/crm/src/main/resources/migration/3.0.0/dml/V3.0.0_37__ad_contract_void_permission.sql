-- ============================================================
-- CordysCRM 广告下单系统 —— 合同作废流程权限播种（DML）
-- 版本: 3.0.0  描述: ad_contract_void_permission
-- 说明:
--   1) 合同新增作废流程权限：
--        - AD_CONTRACT:VOID_SUBMIT  （提交作废，媒介操作）
--        - AD_CONTRACT:VOID_APPROVE （作废审批，管理组操作）
--   2) 三处一致红线：本脚本 == PermissionConstants.java == resources/permission.json + i18n。
--   3) 幂等：sys_role_permission 无 (role_id, permission_id) 唯一键，
--      使用 INSERT ... SELECT ... WHERE NOT EXISTS，重复执行不会产生重复行。
--   4) 角色授权：
--        - org_admin(管理员)  ：授予两个权限
--        - ROLE_BOSS(管理组)  ：授予两个权限
--        - ROLE_MEDIA(媒介)   ：授予 AD_CONTRACT:VOID_SUBMIT（提交作废）
-- ============================================================
SET SESSION innodb_lock_wait_timeout = 7200;

-- ------------------------------------------------------------
-- 1) org_admin / ROLE_BOSS —— 授予两个作废权限（管理员/管理组）
-- ------------------------------------------------------------
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), r.role_id, c.code
FROM (SELECT 'org_admin' AS role_id UNION ALL SELECT 'ROLE_BOSS') r
JOIN (
    SELECT 'AD_CONTRACT:VOID_SUBMIT'  AS code UNION ALL
    SELECT 'AD_CONTRACT:VOID_APPROVE'
) c
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission p
    WHERE p.role_id = r.role_id AND p.permission_id = c.code
);

-- ------------------------------------------------------------
-- 2) ROLE_MEDIA(媒介) —— 授予提交作废权限
-- ------------------------------------------------------------
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), 'ROLE_MEDIA', c.code
FROM (
    SELECT 'AD_CONTRACT:VOID_SUBMIT' AS code
) c
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission p
    WHERE p.role_id = 'ROLE_MEDIA' AND p.permission_id = c.code
);

SET SESSION innodb_lock_wait_timeout = DEFAULT;
