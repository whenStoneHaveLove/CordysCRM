-- ============================================================
-- CordysCRM 广告下单系统 V3.1 —— AD_* 权限码播种（DML）
-- 版本: 3.0.0  描述: ad_permissions
-- 说明:
--   1) 权限码来源：PRD 开发版 V3.1 §3.3 功能权限矩阵（按钮级 @CsPermission）。
--      模块划分对齐 PRD §3.3 / §8 / §9：
--        订单 AD_ORDER / 改单 AD_ORDER_CHANGE / 合同 AD_CONTRACT / 用印 AD_SEAL
--        / 付款(财务) AD_PAYMENT / 资源 AD_RESOURCE / 客户 AD_CUSTOMER
--        / 业务主体 AD_BUSINESS_ENTITY / 用户-主体 AD_USER_BUSINESS_ENTITY
--        / 字典 AD_DICT / 工作台 AD_WORKBENCH / 审批中心 AD_APPROVAL
--        / 报表 AD_REPORT / 系统配置 AD_SYSTEM
--   2) 权威码：AD_ORDER_CHANGE:* 与 AD_SEAL:*（替代旧 AD_ORDER:CHANGE / AD_CONTRACT:SEAL_*）。
--      旧码已废弃，本脚本不写入任何废弃码。
--   3) 角色授权：
--        - org_admin           ：授予全部 AD_* 权限（管理员/老板视角，前端菜单完整显示）
--        - ROLE_BOSS(老板)     ：授予全部 AD_* 权限（与 §3.3 老板列一致）
--        - ROLE_FINANCE(财务)  ：按 §3.3 财务列授予（全模块查看 + 收付款相关动作 + 归档）
--        - ROLE_MEDIA(媒介)    ：按 §3.3 媒介列授予（新建编辑/提交/改单申请/用印申请/合同资源客户CRUD + 全模块查看）
--   4) 幂等：sys_role_permission 无 (role_id, permission_id) 唯一键，故使用
--        INSERT ... SELECT ... WHERE NOT EXISTS，重复执行不会产生重复行。
--   5) 关联表：sys_role_permission(id, role_id, permission_id)。
--      运行时鉴权链：CsPermissionAspect -> ResourcePermissionService -> PermissionUtils.hasPermission
--      -> PermissionCache -> RoleService.getPermissions(roleIds)，仅校验本表，不依赖 sys_permission 定义表。
--   6) 三处一致红线：本脚本 permission_id == PermissionConstants.java 常量值 == 前端 advertising.ts meta.permissions。
--   7) 不改动任何 ad 业务代码；本脚本仅做权限播种。
-- ============================================================
SET SESSION innodb_lock_wait_timeout = 7200;

-- ============================================================
-- 1) org_admin —— 授予全部 AD_* 权限（管理员完整可见）
-- ============================================================
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), 'org_admin', c.code FROM (
    -- 订单 AD_ORDER
    SELECT 'AD_ORDER:READ'            AS code UNION ALL
    SELECT 'AD_ORDER:CREATE'          UNION ALL
    SELECT 'AD_ORDER:SUBMIT'          UNION ALL
    SELECT 'AD_ORDER:APPROVE'         UNION ALL
    SELECT 'AD_ORDER:REJECT'          UNION ALL
    SELECT 'AD_ORDER:ARCHIVE'         UNION ALL
    SELECT 'AD_ORDER:FORCE_ARCHIVE'   UNION ALL
    SELECT 'AD_ORDER:VOID'            UNION ALL
    SELECT 'AD_ORDER:EXPORT'          UNION ALL
    -- 改单 AD_ORDER_CHANGE（独立模块，替代旧 AD_ORDER:CHANGE）
    SELECT 'AD_ORDER_CHANGE:READ'     UNION ALL
    SELECT 'AD_ORDER_CHANGE:CREATE'   UNION ALL
    SELECT 'AD_ORDER_CHANGE:APPROVE'  UNION ALL
    SELECT 'AD_ORDER_CHANGE:REJECT'   UNION ALL
    -- 付款/财务 AD_PAYMENT
    SELECT 'AD_PAYMENT:READ'              UNION ALL
    SELECT 'AD_PAYMENT:CONFIRM_PREPAY'    UNION ALL
    SELECT 'AD_PAYMENT:PAY_MEDIA_PREPAY' UNION ALL
    SELECT 'AD_PAYMENT:INVOICE'           UNION ALL
    SELECT 'AD_PAYMENT:RECEIVE'           UNION ALL
    SELECT 'AD_PAYMENT:PAY_MEDIA_POSTPAY' UNION ALL
    SELECT 'AD_PAYMENT:RED_INVOICE_CLEAR' UNION ALL
    -- 合同 AD_CONTRACT
    SELECT 'AD_CONTRACT:READ'         UNION ALL
    SELECT 'AD_CONTRACT:CREATE'       UNION ALL
    SELECT 'AD_CONTRACT:UPDATE'       UNION ALL
    SELECT 'AD_CONTRACT:DELETE'       UNION ALL
    -- 用印 AD_SEAL（独立模块，替代旧 AD_CONTRACT:SEAL_*）
    SELECT 'AD_SEAL:READ'             UNION ALL
    SELECT 'AD_SEAL:APPLY'            UNION ALL
    SELECT 'AD_SEAL:APPROVE'          UNION ALL
    SELECT 'AD_SEAL:REJECT'           UNION ALL
    SELECT 'AD_SEAL:UPLOAD'           UNION ALL
    -- 资源 AD_RESOURCE
    SELECT 'AD_RESOURCE:READ'         UNION ALL
    SELECT 'AD_RESOURCE:CREATE'       UNION ALL
    SELECT 'AD_RESOURCE:UPDATE'       UNION ALL
    SELECT 'AD_RESOURCE:DELETE'       UNION ALL
    -- 客户 AD_CUSTOMER
    SELECT 'AD_CUSTOMER:READ'         UNION ALL
    SELECT 'AD_CUSTOMER:CREATE'       UNION ALL
    SELECT 'AD_CUSTOMER:UPDATE'       UNION ALL
    SELECT 'AD_CUSTOMER:DELETE'       UNION ALL
    -- 业务主体 AD_BUSINESS_ENTITY
    SELECT 'AD_BUSINESS_ENTITY:READ'   UNION ALL
    SELECT 'AD_BUSINESS_ENTITY:CREATE' UNION ALL
    SELECT 'AD_BUSINESS_ENTITY:UPDATE' UNION ALL
    SELECT 'AD_BUSINESS_ENTITY:DELETE' UNION ALL
    -- 用户-业务主体 AD_USER_BUSINESS_ENTITY
    SELECT 'AD_USER_BUSINESS_ENTITY:READ'   UNION ALL
    SELECT 'AD_USER_BUSINESS_ENTITY:CREATE' UNION ALL
    SELECT 'AD_USER_BUSINESS_ENTITY:UPDATE' UNION ALL
    SELECT 'AD_USER_BUSINESS_ENTITY:DELETE' UNION ALL
    -- 字典 AD_DICT
    SELECT 'AD_DICT:READ'             UNION ALL
    SELECT 'AD_DICT:CREATE'           UNION ALL
    SELECT 'AD_DICT:UPDATE'           UNION ALL
    SELECT 'AD_DICT:DELETE'           UNION ALL
    -- 报表 AD_REPORT
    SELECT 'AD_REPORT:READ'           UNION ALL
    -- 工作台 AD_WORKBENCH
    SELECT 'AD_WORKBENCH:READ'        UNION ALL
    -- 审批中心 AD_APPROVAL
    SELECT 'AD_APPROVAL:READ'         UNION ALL
    SELECT 'AD_APPROVAL:APPROVE'      UNION ALL
    -- 系统配置 AD_SYSTEM
    SELECT 'AD_SYSTEM:READ'           UNION ALL
    SELECT 'AD_SYSTEM:CONFIG'
) c
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission p
    WHERE p.role_id = 'org_admin' AND p.permission_id = c.code
);

-- ============================================================
-- 2) ROLE_BOSS(老板) —— 按 §3.3 老板列授予全部 AD_* 权限
-- ============================================================
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), 'ROLE_BOSS', c.code FROM (
    SELECT 'AD_ORDER:READ'            AS code UNION ALL
    SELECT 'AD_ORDER:CREATE'          UNION ALL
    SELECT 'AD_ORDER:SUBMIT'          UNION ALL
    SELECT 'AD_ORDER:APPROVE'         UNION ALL
    SELECT 'AD_ORDER:REJECT'          UNION ALL
    SELECT 'AD_ORDER:ARCHIVE'         UNION ALL
    SELECT 'AD_ORDER:FORCE_ARCHIVE'   UNION ALL
    SELECT 'AD_ORDER:VOID'            UNION ALL
    SELECT 'AD_ORDER:EXPORT'          UNION ALL
    SELECT 'AD_ORDER_CHANGE:READ'     UNION ALL
    SELECT 'AD_ORDER_CHANGE:CREATE'   UNION ALL
    SELECT 'AD_ORDER_CHANGE:APPROVE'  UNION ALL
    SELECT 'AD_ORDER_CHANGE:REJECT'   UNION ALL
    SELECT 'AD_PAYMENT:READ'              UNION ALL
    SELECT 'AD_PAYMENT:CONFIRM_PREPAY'    UNION ALL
    SELECT 'AD_PAYMENT:PAY_MEDIA_PREPAY' UNION ALL
    SELECT 'AD_PAYMENT:INVOICE'           UNION ALL
    SELECT 'AD_PAYMENT:RECEIVE'           UNION ALL
    SELECT 'AD_PAYMENT:PAY_MEDIA_POSTPAY' UNION ALL
    SELECT 'AD_PAYMENT:RED_INVOICE_CLEAR' UNION ALL
    SELECT 'AD_CONTRACT:READ'         UNION ALL
    SELECT 'AD_CONTRACT:CREATE'       UNION ALL
    SELECT 'AD_CONTRACT:UPDATE'       UNION ALL
    SELECT 'AD_CONTRACT:DELETE'       UNION ALL
    SELECT 'AD_SEAL:READ'             UNION ALL
    SELECT 'AD_SEAL:APPLY'            UNION ALL
    SELECT 'AD_SEAL:APPROVE'          UNION ALL
    SELECT 'AD_SEAL:REJECT'           UNION ALL
    SELECT 'AD_SEAL:UPLOAD'           UNION ALL
    SELECT 'AD_RESOURCE:READ'         UNION ALL
    SELECT 'AD_RESOURCE:CREATE'       UNION ALL
    SELECT 'AD_RESOURCE:UPDATE'       UNION ALL
    SELECT 'AD_RESOURCE:DELETE'       UNION ALL
    SELECT 'AD_CUSTOMER:READ'         UNION ALL
    SELECT 'AD_CUSTOMER:CREATE'       UNION ALL
    SELECT 'AD_CUSTOMER:UPDATE'       UNION ALL
    SELECT 'AD_CUSTOMER:DELETE'       UNION ALL
    SELECT 'AD_BUSINESS_ENTITY:READ'   UNION ALL
    SELECT 'AD_BUSINESS_ENTITY:CREATE' UNION ALL
    SELECT 'AD_BUSINESS_ENTITY:UPDATE' UNION ALL
    SELECT 'AD_BUSINESS_ENTITY:DELETE' UNION ALL
    SELECT 'AD_USER_BUSINESS_ENTITY:READ'   UNION ALL
    SELECT 'AD_USER_BUSINESS_ENTITY:CREATE' UNION ALL
    SELECT 'AD_USER_BUSINESS_ENTITY:UPDATE' UNION ALL
    SELECT 'AD_USER_BUSINESS_ENTITY:DELETE' UNION ALL
    SELECT 'AD_DICT:READ'             UNION ALL
    SELECT 'AD_DICT:CREATE'           UNION ALL
    SELECT 'AD_DICT:UPDATE'           UNION ALL
    SELECT 'AD_DICT:DELETE'           UNION ALL
    SELECT 'AD_REPORT:READ'           UNION ALL
    SELECT 'AD_WORKBENCH:READ'        UNION ALL
    SELECT 'AD_APPROVAL:READ'         UNION ALL
    SELECT 'AD_APPROVAL:APPROVE'      UNION ALL
    SELECT 'AD_SYSTEM:READ'           UNION ALL
    SELECT 'AD_SYSTEM:CONFIG'
) c
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission p
    WHERE p.role_id = 'ROLE_BOSS' AND p.permission_id = c.code
);

-- ============================================================
-- 3) ROLE_FINANCE(财务) —— 按 §3.3 财务列授权
--    全模块查看 + 收付款相关动作(确认预收/支付媒体预付尾款/开票/收款/红冲) + 归档
-- ============================================================
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), 'ROLE_FINANCE', c.code FROM (
    SELECT 'AD_ORDER:READ'                 AS code UNION ALL
    SELECT 'AD_ORDER:ARCHIVE'              UNION ALL
    SELECT 'AD_ORDER_CHANGE:READ'          UNION ALL
    SELECT 'AD_PAYMENT:READ'               UNION ALL
    SELECT 'AD_PAYMENT:CONFIRM_PREPAY'     UNION ALL
    SELECT 'AD_PAYMENT:PAY_MEDIA_PREPAY'   UNION ALL
    SELECT 'AD_PAYMENT:INVOICE'            UNION ALL
    SELECT 'AD_PAYMENT:RECEIVE'            UNION ALL
    SELECT 'AD_PAYMENT:PAY_MEDIA_POSTPAY'  UNION ALL
    SELECT 'AD_PAYMENT:RED_INVOICE_CLEAR'  UNION ALL
    SELECT 'AD_CONTRACT:READ'              UNION ALL
    SELECT 'AD_SEAL:READ'                  UNION ALL
    SELECT 'AD_RESOURCE:READ'              UNION ALL
    SELECT 'AD_CUSTOMER:READ'              UNION ALL
    SELECT 'AD_BUSINESS_ENTITY:READ'       UNION ALL
    SELECT 'AD_USER_BUSINESS_ENTITY:READ'  UNION ALL
    SELECT 'AD_DICT:READ'                  UNION ALL
    SELECT 'AD_REPORT:READ'                UNION ALL
    SELECT 'AD_WORKBENCH:READ'             UNION ALL
    SELECT 'AD_APPROVAL:READ'              UNION ALL
    SELECT 'AD_SYSTEM:READ'
) c
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission p
    WHERE p.role_id = 'ROLE_FINANCE' AND p.permission_id = c.code
);

-- ============================================================
-- 4) ROLE_MEDIA(媒介) —— 按 §3.3 媒介列授权
--    新建/编辑订单 / 提交审核 / 改单申请 / 用印申请 / 合同·资源·客户CRUD / 全模块查看
-- ============================================================
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), 'ROLE_MEDIA', c.code FROM (
    SELECT 'AD_ORDER:READ'            AS code UNION ALL
    SELECT 'AD_ORDER:CREATE'          UNION ALL
    SELECT 'AD_ORDER:SUBMIT'          UNION ALL
    SELECT 'AD_ORDER:VOID'            UNION ALL
    SELECT 'AD_ORDER_CHANGE:READ'     UNION ALL
    SELECT 'AD_ORDER_CHANGE:CREATE'   UNION ALL
    SELECT 'AD_PAYMENT:READ'          UNION ALL
    SELECT 'AD_CONTRACT:READ'         UNION ALL
    SELECT 'AD_CONTRACT:CREATE'       UNION ALL
    SELECT 'AD_CONTRACT:UPDATE'       UNION ALL
    SELECT 'AD_SEAL:READ'             UNION ALL
    SELECT 'AD_SEAL:APPLY'            UNION ALL
    SELECT 'AD_RESOURCE:READ'         UNION ALL
    SELECT 'AD_RESOURCE:CREATE'       UNION ALL
    SELECT 'AD_RESOURCE:UPDATE'       UNION ALL
    SELECT 'AD_RESOURCE:DELETE'       UNION ALL
    SELECT 'AD_CUSTOMER:READ'         UNION ALL
    SELECT 'AD_CUSTOMER:CREATE'       UNION ALL
    SELECT 'AD_CUSTOMER:UPDATE'       UNION ALL
    SELECT 'AD_CUSTOMER:DELETE'       UNION ALL
    SELECT 'AD_BUSINESS_ENTITY:READ'  UNION ALL
    SELECT 'AD_USER_BUSINESS_ENTITY:READ' UNION ALL
    SELECT 'AD_DICT:READ'             UNION ALL
    SELECT 'AD_REPORT:READ'           UNION ALL
    SELECT 'AD_WORKBENCH:READ'        UNION ALL
    SELECT 'AD_APPROVAL:READ'         UNION ALL
    SELECT 'AD_SYSTEM:READ'
) c
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission p
    WHERE p.role_id = 'ROLE_MEDIA' AND p.permission_id = c.code
);

SET SESSION innodb_lock_wait_timeout = DEFAULT;
