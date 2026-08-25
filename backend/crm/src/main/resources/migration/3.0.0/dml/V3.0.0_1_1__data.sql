-- ============================================================
-- CordysCRM 广告下单系统 V3.1 数据播种（DML）
-- 版本: 3.0.0  描述: ad_system_v31_data
-- 说明:
--   1) ad_dict 初始数据(行业/类型/用印类型/收付款方式)
--   2) AD_* 权限码播种到 sys_role_permission（授权给 org_admin，admin 用户因此具备全部广告模块权限）
-- 幂等: 使用 INSERT IGNORE，重跑安全。
-- 租户 organization_id 取开源基线初始组织 '100001'。
-- ============================================================
SET SESSION innodb_lock_wait_timeout = 7200;

-- ---------- 1) ad_dict 字典初始数据 ----------
INSERT IGNORE INTO ad_dict (id, dict_code, dict_value, dict_label, parent_value, sort, status, organization_id, create_time, update_time, create_user, update_user, deleted)
VALUES
-- 行业类别 industry
(UUID_SHORT(), 'industry', '10', '互联网',   NULL, 10, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
(UUID_SHORT(), 'industry', '20', '快消',     NULL, 20, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
(UUID_SHORT(), 'industry', '30', '金融',     NULL, 30, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
(UUID_SHORT(), 'industry', '40', '汽车',     NULL, 40, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
(UUID_SHORT(), 'industry', '50', '教育',     NULL, 50, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
(UUID_SHORT(), 'industry', '60', '医疗',     NULL, 60, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
(UUID_SHORT(), 'industry', '70', '房地产',   NULL, 70, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
(UUID_SHORT(), 'industry', '80', '其他',     NULL, 80, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
-- 类型 media_type
(UUID_SHORT(), 'media_type', '10', '搜索引擎',   NULL, 10, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
(UUID_SHORT(), 'media_type', '20', '信息流',     NULL, 20, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
(UUID_SHORT(), 'media_type', '30', '视频',       NULL, 30, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
(UUID_SHORT(), 'media_type', '40', '社交',   NULL, 40, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
(UUID_SHORT(), 'media_type', '50', '户外',       NULL, 50, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
(UUID_SHORT(), 'media_type', '60', '其他',       NULL, 60, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
-- 用印类型 seal_type
(UUID_SHORT(), 'seal_type', '10', '公章',   NULL, 10, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
(UUID_SHORT(), 'seal_type', '20', '合同章', NULL, 20, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
-- 收款方式 receipt_method
(UUID_SHORT(), 'receipt_method', '10', '预收', NULL, 10, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
(UUID_SHORT(), 'receipt_method', '20', '账期',   NULL, 20, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
-- 付款方式 payment_method
(UUID_SHORT(), 'payment_method', '10', '预付', NULL, 10, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0),
(UUID_SHORT(), 'payment_method', '20', '后付', NULL, 20, 10, '100001', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'admin', 'admin', 0);

-- ---------- 2) AD_* 权限码播种 ----------
-- 授权给 org_admin 角色（admin 用户具备全部广告模块权限位）
INSERT IGNORE INTO sys_role_permission (id, role_id, permission_id) VALUES
-- 订单 AD_ORDER
(UUID_SHORT(), 'org_admin', 'AD_ORDER:READ'),
(UUID_SHORT(), 'org_admin', 'AD_ORDER:CREATE'),
(UUID_SHORT(), 'org_admin', 'AD_ORDER:SUBMIT'),
(UUID_SHORT(), 'org_admin', 'AD_ORDER:APPROVE'),
(UUID_SHORT(), 'org_admin', 'AD_ORDER:CHANGE'),
(UUID_SHORT(), 'org_admin', 'AD_ORDER:VOID'),
(UUID_SHORT(), 'org_admin', 'AD_ORDER:ARCHIVE'),
(UUID_SHORT(), 'org_admin', 'AD_ORDER:FORCE_ARCHIVE'),
-- 合同 AD_CONTRACT
(UUID_SHORT(), 'org_admin', 'AD_CONTRACT:READ'),
(UUID_SHORT(), 'org_admin', 'AD_CONTRACT:CREATE'),
(UUID_SHORT(), 'org_admin', 'AD_CONTRACT:UPDATE'),
(UUID_SHORT(), 'org_admin', 'AD_CONTRACT:DELETE'),
(UUID_SHORT(), 'org_admin', 'AD_CONTRACT:SEAL_APPLY'),
(UUID_SHORT(), 'org_admin', 'AD_CONTRACT:SEAL_APPROVE'),
(UUID_SHORT(), 'org_admin', 'AD_CONTRACT:SEAL_REJECT'),
(UUID_SHORT(), 'org_admin', 'AD_CONTRACT:SEAL_UPLOAD'),
-- 财务/收付款 AD_PAYMENT
(UUID_SHORT(), 'org_admin', 'AD_PAYMENT:CONFIRM_PREPAY'),
(UUID_SHORT(), 'org_admin', 'AD_PAYMENT:PAY_MEDIA_PREPAY'),
(UUID_SHORT(), 'org_admin', 'AD_PAYMENT:INVOICE'),
(UUID_SHORT(), 'org_admin', 'AD_PAYMENT:RECEIVE'),
(UUID_SHORT(), 'org_admin', 'AD_PAYMENT:PAY_MEDIA_POSTPAY'),
(UUID_SHORT(), 'org_admin', 'AD_PAYMENT:RED_INVOICE_CLEAR'),
-- 字典 AD_DICT
(UUID_SHORT(), 'org_admin', 'AD_DICT:READ'),
(UUID_SHORT(), 'org_admin', 'AD_DICT:CREATE'),
(UUID_SHORT(), 'org_admin', 'AD_DICT:UPDATE'),
(UUID_SHORT(), 'org_admin', 'AD_DICT:DELETE'),
-- 业务主体 AD_BUSINESS_ENTITY
(UUID_SHORT(), 'org_admin', 'AD_BUSINESS_ENTITY:READ'),
(UUID_SHORT(), 'org_admin', 'AD_BUSINESS_ENTITY:CREATE'),
(UUID_SHORT(), 'org_admin', 'AD_BUSINESS_ENTITY:UPDATE'),
(UUID_SHORT(), 'org_admin', 'AD_BUSINESS_ENTITY:DELETE'),
-- 用户-主体 AD_USER_BUSINESS_ENTITY
(UUID_SHORT(), 'org_admin', 'AD_USER_BUSINESS_ENTITY:READ'),
(UUID_SHORT(), 'org_admin', 'AD_USER_BUSINESS_ENTITY:CREATE'),
(UUID_SHORT(), 'org_admin', 'AD_USER_BUSINESS_ENTITY:UPDATE'),
(UUID_SHORT(), 'org_admin', 'AD_USER_BUSINESS_ENTITY:DELETE'),
-- 客户 AD_CUSTOMER
(UUID_SHORT(), 'org_admin', 'AD_CUSTOMER:READ'),
(UUID_SHORT(), 'org_admin', 'AD_CUSTOMER:CREATE'),
(UUID_SHORT(), 'org_admin', 'AD_CUSTOMER:UPDATE'),
(UUID_SHORT(), 'org_admin', 'AD_CUSTOMER:DELETE'),
-- 资源 AD_RESOURCE
(UUID_SHORT(), 'org_admin', 'AD_RESOURCE:READ'),
(UUID_SHORT(), 'org_admin', 'AD_RESOURCE:CREATE'),
(UUID_SHORT(), 'org_admin', 'AD_RESOURCE:UPDATE'),
(UUID_SHORT(), 'org_admin', 'AD_RESOURCE:DELETE'),
-- 报表 AD_REPORT
(UUID_SHORT(), 'org_admin', 'AD_REPORT:READ');

SET SESSION innodb_lock_wait_timeout = DEFAULT;
