-- ============================================================
-- 管理员角色补充「广告工作台」板块权限（DML）
-- 版本: 3.0.0.42  描述: ad_workbench_section_permission
-- 说明:
--   1) 背景：广告工作台首页的「媒体板块 / 老板板块 / 财务板块」由
--      AdDashboardService.resolveAdRoles() 按三个权限点决定是否展示：
--        AD_WORKBENCH:MEDIA   -> MEDIA   媒体板块
--        AD_WORKBENCH:BOSS    -> BOSS    老板板块（管理组）
--        AD_WORKBENCH:FINANCE -> FINANCE 财务板块
--      历史初始化脚本未把这三个权限点授予 org_admin（管理员），
--      导致「系统-角色权限」页中该三项默认未勾选、工作台对应板块不显示。
--   2) 权限码本身已存在（PermissionConstants.AD_WORKBENCH_* 与
--      permission.json 的 AD_WORKBENCH 节点），本脚本只补角色授权数据。
--   3) 幂等：sys_role_permission 无 (role_id, permission_id) 唯一键，
--      使用 INSERT ... SELECT ... WHERE NOT EXISTS，重复执行不会产生重复行。
--   4) 仅补 org_admin；ROLE_BOSS / ROLE_MEDIA / ROLE_FINANCE 等业务角色
--      由用户在「角色权限」页自行勾选。
-- ============================================================
SET SESSION innodb_lock_wait_timeout = 7200;

INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), 'org_admin', c.code
FROM (
    SELECT 'AD_WORKBENCH:MEDIA'   AS code UNION ALL
    SELECT 'AD_WORKBENCH:BOSS'    UNION ALL
    SELECT 'AD_WORKBENCH:FINANCE'
) c
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission p
    WHERE p.role_id = 'org_admin' AND p.permission_id = c.code
);

SET SESSION innodb_lock_wait_timeout = DEFAULT;
