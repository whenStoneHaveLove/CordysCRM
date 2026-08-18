-- ============================================================
-- CordysCRM 广告下单系统 V3.1 —— 移除废弃的 AD_USER_BUSINESS_ENTITY 权限
-- 版本: 3.0.0_18  描述: remove_ad_user_business_entity_permissions
-- 说明:
--   AD_USER_BUSINESS_ENTITY（用户-业务主体）没有独立前端菜单，仅作为
--   数据权限关联表（ad_user_business_entity）使用，不应出现在角色权限
--   配置页面。本迁移删除已播种到各默认角色中的相关权限码。
-- 幂等：DELETE 语句天然幂等。
-- ============================================================
SET SESSION innodb_lock_wait_timeout = 7200;

DELETE FROM sys_role_permission
WHERE permission_id IN (
    'AD_USER_BUSINESS_ENTITY:READ',
    'AD_USER_BUSINESS_ENTITY:CREATE',
    'AD_USER_BUSINESS_ENTITY:UPDATE',
    'AD_USER_BUSINESS_ENTITY:DELETE'
);

SET SESSION innodb_lock_wait_timeout = DEFAULT;
