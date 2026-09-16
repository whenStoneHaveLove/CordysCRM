-- ============================================================
-- CordysCRM 广告下单系统 —— 订单复制权限播种（DML）
-- 版本: 3.0.0  描述: ad_order_copy_permission
-- 说明:
--   1) 订单列表新增「复制」按钮（复制生成草稿订单），新增权限码：
--        - AD_ORDER:COPY （复制订单）
--   2) 三处一致红线：本脚本 == PermissionConstants.java（AD_ORDER_COPY）
--        == resources/permission.json（AD_ORDER:COPY）+ i18n（permission.copy）。
--   3) 幂等：sys_role_permission 无 (role_id, permission_id) 唯一键，
--      使用 INSERT ... SELECT ... WHERE NOT EXISTS，重复执行不会产生重复行。
--   4) 角色授权（与 AD_ORDER:CREATE 保持一致）：
--        - org_admin(管理员) ：授予
--        - ROLE_BOSS(管理组) ：授予
--        - ROLE_MEDIA(媒介)  ：授予
--        - ROLE_FINANCE(财务)：不授予（财务仅查看订单）
-- ============================================================
SET SESSION innodb_lock_wait_timeout = 7200;

INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), r.role_id, 'AD_ORDER:COPY'
FROM (SELECT 'org_admin' AS role_id UNION ALL SELECT 'ROLE_BOSS' UNION ALL SELECT 'ROLE_MEDIA') r
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission p
    WHERE p.role_id = r.role_id AND p.permission_id = 'AD_ORDER:COPY'
);

SET SESSION innodb_lock_wait_timeout = DEFAULT;
