-- ============================================================
-- CordysCRM 广告下单系统 V3.1 演进脚本（基于 dev3.0 开源基线）
-- 版本: 3.0.0  描述: ad_system_v31
-- 说明: ad_* 模块在基线中文件数=0，全部为新建；不改动任何已有脚本
-- 注意: 每张 ad 表前加 DROP TABLE IF EXISTS 以保证开发循环幂等重建；
--       sys_user 仅追加一列 is_cross_entity（带存在性判断，安全重跑）。
-- ============================================================
SET SESSION innodb_lock_wait_timeout = 7200;

-- ---------- 业务主体 ----------
DROP TABLE IF EXISTS ad_business_entity;
CREATE TABLE ad_business_entity
(
    `id`              VARCHAR(32)  NOT NULL COMMENT 'id',
    `name`            VARCHAR(128) NOT NULL COMMENT '主体名称',
    `code`            VARCHAR(32)  NOT NULL COMMENT '主体代码(JS/TH)',
    `status`          TINYINT      NOT NULL DEFAULT 10 COMMENT '状态:10启用/20停用',
    `organization_id` VARCHAR(32)  NOT NULL COMMENT '组织(租户)id',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`     VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`     VARCHAR(32)  NOT NULL COMMENT '修改人',
    `deleted`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ad_be_code (code, organization_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '业务主体';

CREATE INDEX idx_ad_be_org ON ad_business_entity (organization_id);

-- ---------- 用户-主体 N:N (L-06) ----------
DROP TABLE IF EXISTS ad_user_business_entity;
CREATE TABLE ad_user_business_entity
(
    `id`                 VARCHAR(32) NOT NULL COMMENT 'id',
    `user_id`            VARCHAR(32) NOT NULL COMMENT '用户id',
    `business_entity_id` VARCHAR(32) NOT NULL COMMENT '业务主体id',
    `is_primary`         TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否主要主体:0-否/1-是',
    `organization_id`    VARCHAR(32) NOT NULL COMMENT '组织(租户)id',
    `create_time`        BIGINT      NOT NULL COMMENT '创建时间',
    `update_user`        VARCHAR(32) NOT NULL COMMENT '修改人',
    `deleted`            TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ad_ube (user_id, business_entity_id, organization_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户-业务主体关联';

CREATE INDEX idx_ad_ube_user ON ad_user_business_entity (user_id);
CREATE INDEX idx_ad_ube_entity ON ad_user_business_entity (business_entity_id);

-- ---------- 客户库(全局唯一, L-16) ----------
DROP TABLE IF EXISTS ad_customer;
CREATE TABLE ad_customer
(
    `id`              VARCHAR(32)  NOT NULL COMMENT 'id',
    `name`            VARCHAR(255) NOT NULL COMMENT '客户名称(全局唯一)',
    `brand`           VARCHAR(255) COMMENT '品牌',
    `industry_code`   VARCHAR(32)  COMMENT '行业类别(字典)',
    `signing_entity`  VARCHAR(128) COMMENT '签约主体',
    `organization_id` VARCHAR(32)  NOT NULL COMMENT '组织(租户)id',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`     VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`     VARCHAR(32)  NOT NULL COMMENT '修改人',
    `deleted`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ad_cu_name_org (name, organization_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告客户库';

CREATE INDEX idx_ad_cu_org ON ad_customer (organization_id);

-- ---------- 上下游资源 ----------
DROP TABLE IF EXISTS ad_resource;
CREATE TABLE ad_resource
(
    `id`                 VARCHAR(32)  NOT NULL COMMENT 'id',
    `resource_type`      TINYINT      NOT NULL COMMENT '资源类型:10代理/20媒体',
    `name`               VARCHAR(255) NOT NULL COMMENT '名称',
    `media_type`         VARCHAR(32)  COMMENT '媒体类型(字典)',
    `channel`            VARCHAR(128) COMMENT '渠道',
    `rate_card`          VARCHAR(512) COMMENT '刊例价',
    `discount_policy`    VARCHAR(512) COMMENT '折扣政策',
    `credit_code`        VARCHAR(64)  COMMENT '信用代码',
    `signing_entity`     VARCHAR(128) COMMENT '签约主体',
    `business_entity_id` VARCHAR(32)  COMMENT '归属业务主体',
    `status`             TINYINT      NOT NULL DEFAULT 10 COMMENT '状态:10正常/20停用',
    `organization_id`    VARCHAR(32)  NOT NULL COMMENT '组织(租户)id',
    `create_time`        BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`        BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`        VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`        VARCHAR(32)  NOT NULL COMMENT '修改人',
    `deleted`            TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '上下游资源';

CREATE INDEX idx_ad_re_org ON ad_resource (organization_id);
CREATE INDEX idx_ad_re_entity ON ad_resource (business_entity_id);

-- ---------- 订单主表 ----------
DROP TABLE IF EXISTS ad_order;
CREATE TABLE ad_order
(
    `id`                        VARCHAR(32)  NOT NULL COMMENT 'id',
    `order_no`                  VARCHAR(32)  NOT NULL COMMENT '订单编号 主体代码-YYYYMMDD-3位流水(L-20)',
    `order_name`                VARCHAR(128) NOT NULL COMMENT '订单名称',
    `business_entity_id`        VARCHAR(32)  NOT NULL COMMENT '业务主体(下单必选,L-29)',
    `customer_id`               VARCHAR(32)  NOT NULL COMMENT '客户id',
    `industry_code`             VARCHAR(32)  COMMENT '行业类别(字典)',
    `signing_entity`            VARCHAR(128) COMMENT '签约主体',
    `order_type`                TINYINT      NOT NULL COMMENT '订单类型:10框架合同/20单笔合同',
    `upstream_agent_id`         VARCHAR(32)  COMMENT '上游代理(可空)',
    `agent_order_no`            VARCHAR(64)  COMMENT '代理订单号',
    `creator_id`                VARCHAR(32)  NOT NULL COMMENT '下单人',
    `status`                    TINYINT      NOT NULL DEFAULT 0 COMMENT '主状态(L-01,10倍数)',
    `total_amount`              DECIMAL(15,2) NOT NULL COMMENT '订单总金额',
    `rebate_mode`               TINYINT      NOT NULL COMMENT '返点方式:10比例/20固定金额',
    `rebate_value`              DECIMAL(10,2) NOT NULL COMMENT '返点值',
    `no_rebate_amount`          DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '不记返金额',
    `rebate_amount`             DECIMAL(15,2) NOT NULL COMMENT '返点金额(自动)',
    `receivable_amount`         DECIMAL(15,2) NOT NULL COMMENT '应收金额=总额-返点',
    `media_payable_amount`      DECIMAL(15,2) NOT NULL COMMENT '媒体应付总额(下游口径,L-28)',
    `delivery_start_date`       DATE         NOT NULL COMMENT '投放起始日',
    `delivery_end_date`         DATE         NOT NULL COMMENT '投放结束日',
    `delivery_volume`           VARCHAR(64)  COMMENT '投放量+单位',
    `remark`                    TEXT         COMMENT '备注',
    `receipt_method`            TINYINT      NOT NULL COMMENT '收款方式:10预付款/20账期',
    `receipt_prepay_mode`       TINYINT      COMMENT '预付模式:10比例/20固定(L-25)',
    `receipt_prepay_ratio`      DECIMAL(5,2) COMMENT '预收比例%',
    `receipt_prepay_amount`     DECIMAL(15,2) COMMENT '预收金额(基数=应收,L-11)',
    `receipt_prepay_deadline`   DATE         COMMENT '预收截止日',
    `receipt_account_period_days` INT        COMMENT '账期天数(账期时必填)',
    `payment_method`            TINYINT      NOT NULL COMMENT '付款方式:10预付媒体/20后付媒体',
    `payment_prepay_mode`       TINYINT      COMMENT '媒体预付模式:10比例/20固定',
    `payment_prepay_ratio`      DECIMAL(5,2) COMMENT '媒体预付比例%',
    `payment_prepay_amount`     DECIMAL(15,2) COMMENT '媒体预付金额(基数=media_payable,L-28)',
    `payment_prepay_deadline`   DATE         COMMENT '媒体预付截止日',
    `payment_postpay_trigger`   TINYINT      COMMENT '后付触发:10收到上游全款/20执行完成X天(L-05)',
    `payment_postpay_days`      INT          COMMENT '后付X天(trigger=20必填)',
    `invoice_status`            TINYINT      NOT NULL DEFAULT 0 COMMENT '开票进度:0未开/10部分/20全额(L-01)',
    `receipt_status`            TINYINT      NOT NULL DEFAULT 0 COMMENT '收款进度:0未收/10部分/20全额',
    `media_payment_status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '媒体付款进度:0未付/10部分/20全额',
    `invoiced_amount`           DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '已开票累计',
    `received_amount`           DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '已收款累计(含预收)',
    `media_paid_amount`         DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '已付媒体款累计',
    `bad_debt_amount`           DECIMAL(15,2) COMMENT '坏账金额(强制归档,L-13)',
    `needs_red_invoice`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '需红冲标记:0否/1是(L-27)',
    `account_period_start_date` DATE         COMMENT '账期起算日(L-09/L-26)',
    `account_period_end_date`   DATE         COMMENT '账期到期日(自动)',
    `execution_completed_at`    DATETIME     COMMENT '执行完成时间',
    `archived_at`               DATETIME     COMMENT '归档时间',
    `voided_at`                 DATETIME     COMMENT '作废时间',
    `void_reason`               TEXT         COMMENT '作废原因',
    `ext_json`                  JSON         COMMENT '扩展字段(V3.1 §4.4)',
    `currency`                  VARCHAR(8)   NOT NULL DEFAULT 'CNY' COMMENT '币种',
    `tenant_id`                 VARCHAR(32)  COMMENT '预留多租户',
    `organization_id`           VARCHAR(32)  NOT NULL COMMENT '组织(租户)id',
    `create_time`               BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`               BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`               VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`               VARCHAR(32)  NOT NULL COMMENT '修改人',
    `deleted`                   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告订单';

CREATE INDEX idx_ad_o_org ON ad_order (organization_id);
CREATE INDEX idx_ad_o_entity ON ad_order (business_entity_id);
CREATE INDEX idx_ad_o_customer ON ad_order (customer_id);
CREATE INDEX idx_ad_o_status ON ad_order (status);
CREATE INDEX idx_ad_o_no ON ad_order (order_no);

-- ---------- 改单记录 ----------
DROP TABLE IF EXISTS ad_order_change;
CREATE TABLE ad_order_change
(
    `id`              VARCHAR(32) NOT NULL COMMENT 'id',
    `order_id`        VARCHAR(32) NOT NULL COMMENT '订单id',
    `change_fields`   JSON        NOT NULL COMMENT '变更字段清单',
    `reason`          TEXT        NOT NULL COMMENT '变更原因',
    `snapshot_before` JSON        NOT NULL COMMENT '变更前快照(L-30)',
    `snapshot_after`  JSON        NOT NULL COMMENT '变更后快照',
    `status`          TINYINT     NOT NULL DEFAULT 0 COMMENT '0待审批/10通过/20驳回',
    `approver_id`     VARCHAR(32) COMMENT '审批人',
    `approved_at`     DATETIME    COMMENT '审批时间',
    `approve_remark`  TEXT        COMMENT '审批备注',
    `organization_id` VARCHAR(32) NOT NULL COMMENT '组织(租户)id',
    `create_time`     BIGINT      NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT      NOT NULL COMMENT '更新时间',
    `create_user`     VARCHAR(32) NOT NULL COMMENT '创建人',
    `update_user`     VARCHAR(32) NOT NULL COMMENT '修改人',
    `deleted`         TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告订单改单';

CREATE INDEX idx_ad_oc_order ON ad_order_change (order_id);

-- ---------- 订单附件 ----------
DROP TABLE IF EXISTS ad_order_attachment;
CREATE TABLE ad_order_attachment
(
    `id`          VARCHAR(32) NOT NULL COMMENT 'id',
    `order_id`    VARCHAR(32) NOT NULL COMMENT '订单id',
    `type`        TINYINT     NOT NULL COMMENT '10排期/20邮件截图/30合同/40过程附件/50改单附件',
    `file_url`    VARCHAR(512) NOT NULL COMMENT '文件地址',
    `file_name`   VARCHAR(128) NOT NULL COMMENT '文件名',
    `organization_id` VARCHAR(32) NOT NULL COMMENT '组织(租户)id',
    `create_time` BIGINT      NOT NULL COMMENT '创建时间',
    `create_user` VARCHAR(32) NOT NULL COMMENT '创建人',
    `deleted`     TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告订单附件';

CREATE INDEX idx_ad_oa_order ON ad_order_attachment (order_id);

-- ---------- 订单操作记录(详情Tab) ----------
DROP TABLE IF EXISTS ad_order_log;
CREATE TABLE ad_order_log
(
    `id`           VARCHAR(32) NOT NULL COMMENT 'id',
    `order_id`     VARCHAR(32) NOT NULL COMMENT '订单id',
    `action`       VARCHAR(64) NOT NULL COMMENT '动作(SUBMIT/APPROVE/REJECT/CHANGE/VOID/ARCHIVE...)',
    `operator_id`  VARCHAR(32) NOT NULL COMMENT '操作人',
    `before_value` JSON        COMMENT '变更前(L-30 仅变更字段)',
    `after_value`  JSON        COMMENT '变更后',
    `ip`           VARCHAR(64) COMMENT 'IP',
    `organization_id` VARCHAR(32) NOT NULL COMMENT '组织(租户)id',
    `create_time`  BIGINT      NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告订单操作记录';

CREATE INDEX idx_ad_ol_order ON ad_order_log (order_id);

-- ---------- 跨模块操作日志(L-18) ----------
DROP TABLE IF EXISTS ad_operation_log;
CREATE TABLE ad_operation_log
(
    `id`           VARCHAR(32) NOT NULL COMMENT 'id',
    `module`       VARCHAR(32) NOT NULL COMMENT '模块(ORDER/CONTRACT/PAYMENT/SYSTEM...)',
    `biz_type`     VARCHAR(32) COMMENT '业务类型',
    `action`       VARCHAR(64) NOT NULL COMMENT '动作',
    `target_id`    VARCHAR(32) NOT NULL COMMENT '目标id',
    `operator_id`  VARCHAR(32) NOT NULL COMMENT '操作人',
    `before_value` JSON        COMMENT '变更前',
    `after_value`  JSON        COMMENT '变更后',
    `ip`           VARCHAR(64) COMMENT 'IP',
    `organization_id` VARCHAR(32) NOT NULL COMMENT '组织(租户)id',
    `create_time`  BIGINT      NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告系统操作日志';

CREATE INDEX idx_ad_op_module_target ON ad_operation_log (module, target_id);
CREATE INDEX idx_ad_op_org ON ad_operation_log (organization_id);

-- ---------- 合同统一表(L-10) ----------
DROP TABLE IF EXISTS ad_contract;
CREATE TABLE ad_contract
(
    `id`                 VARCHAR(32)  NOT NULL COMMENT 'id',
    `contract_no`        VARCHAR(32)  NOT NULL COMMENT '合同编号',
    `contract_name`      VARCHAR(128) NOT NULL COMMENT '合同名称',
    `business_entity_id` VARCHAR(32)  NOT NULL COMMENT '业务主体',
    `contract_direction` TINYINT      NOT NULL COMMENT '10上游/20下游',
    `contract_type`      TINYINT      NOT NULL COMMENT '10框架/20单笔',
    `related_party_id`   VARCHAR(32)  NOT NULL COMMENT '关联方id',
    `related_party_type` TINYINT      NOT NULL COMMENT '10客户/20上游代理/30下游媒体',
    `order_id`           VARCHAR(32)  COMMENT '关联订单(单笔合同)',
    `signing_entity`     VARCHAR(128) COMMENT '签约主体',
    `valid_from`         DATE         COMMENT '有效期起',
    `valid_to`           DATE         COMMENT '有效期止',
    `amount`             DECIMAL(15,2) COMMENT '合同金额',
    `rebate_terms`       VARCHAR(256) COMMENT '返点条款',
    `file_url`           VARCHAR(512) COMMENT '合同文件(单笔用印前可空,L-07)',
    `seal_status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0未申请/10审批中/20已用印/30已驳回',
    `status`             TINYINT      NOT NULL DEFAULT 10 COMMENT '10生效/20失效/30已作废',
    `organization_id`    VARCHAR(32)  NOT NULL COMMENT '组织(租户)id',
    `create_time`        BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`        BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`        VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`        VARCHAR(32)  NOT NULL COMMENT '修改人',
    `deleted`            TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告合同';

CREATE INDEX idx_ad_ct_org ON ad_contract (organization_id);
CREATE INDEX idx_ad_ct_entity ON ad_contract (business_entity_id);
CREATE INDEX idx_ad_ct_order ON ad_contract (order_id);

-- ---------- 用印记录(L-07/L-19) ----------
DROP TABLE IF EXISTS ad_seal_record;
CREATE TABLE ad_seal_record
(
    `id`             VARCHAR(32) NOT NULL COMMENT 'id',
    `contract_id`    VARCHAR(32) NOT NULL COMMENT '合同id',
    `seal_type`      TINYINT     NOT NULL COMMENT '10公章/20合同章',
    `applied_copies` INT         NOT NULL COMMENT '申请份数',
    `actual_copies`  INT         COMMENT '实际盖章份数(审批时填)',
    `applicant_id`   VARCHAR(32) NOT NULL COMMENT '申请人',
    `apply_remark`   TEXT        COMMENT '申请备注',
    `status`         TINYINT     NOT NULL DEFAULT 0 COMMENT '0审批中/10通过/20驳回',
    `approver_id`    VARCHAR(32) COMMENT '审批人',
    `approved_at`    DATETIME    COMMENT '审批时间',
    `approve_remark` TEXT        COMMENT '审批备注',
    `organization_id` VARCHAR(32) NOT NULL COMMENT '组织(租户)id',
    `create_time`    BIGINT      NOT NULL COMMENT '创建时间',
    `update_time`    BIGINT      NOT NULL COMMENT '更新时间',
    `create_user`    VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`    VARCHAR(32)  NOT NULL COMMENT '修改人',
    `deleted`        TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告用印记录';

CREATE INDEX idx_ad_sr_contract ON ad_seal_record (contract_id);

-- ---------- 订单-框架合同中间表 ----------
DROP TABLE IF EXISTS ad_order_contract;
CREATE TABLE ad_order_contract
(
    `id`           VARCHAR(32) NOT NULL COMMENT 'id',
    `order_id`     VARCHAR(32) NOT NULL COMMENT '订单id',
    `contract_id`  VARCHAR(32) NOT NULL COMMENT '合同id',
    `organization_id` VARCHAR(32) NOT NULL COMMENT '组织(租户)id',
    `create_time`  BIGINT      NOT NULL COMMENT '创建时间',
    `deleted`      TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ad_oc (order_id, contract_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单-框架合同关联';

CREATE INDEX idx_ad_oc_contract ON ad_order_contract (contract_id);

-- ---------- 收付款记录(L-28扩展) ----------
DROP TABLE IF EXISTS ad_payment_record;
CREATE TABLE ad_payment_record
(
    `id`                 VARCHAR(32) NOT NULL COMMENT 'id',
    `order_id`           VARCHAR(32) NOT NULL COMMENT '关联订单',
    `business_entity_id` VARCHAR(32) NOT NULL COMMENT '业务主体',
    `resource_id`        VARCHAR(32) COMMENT '关联媒体/代理(L-23)',
    `direction`          TINYINT     NOT NULL COMMENT '10上游收款/20下游付款',
    `type`               TINYINT     NOT NULL COMMENT '10预收/20预付/30开票收款/40媒体尾款/50退款/60坏账',
    `amount`             DECIMAL(15,2) NOT NULL COMMENT '金额',
    `occur_date`         DATE        NOT NULL COMMENT '发生日期',
    `voucher_url`        VARCHAR(512) COMMENT '凭证',
    `invoice_no`         VARCHAR(64)  COMMENT '发票号(type=30)',
    `remark`             TEXT        COMMENT '备注',
    `operator_id`        VARCHAR(32) NOT NULL COMMENT '操作人',
    `organization_id`    VARCHAR(32) NOT NULL COMMENT '组织(租户)id',
    `create_time`        BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`        BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`        VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`        VARCHAR(32)  NOT NULL COMMENT '修改人',
    `deleted`            TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告收付款记录';

CREATE INDEX idx_ad_pr_order ON ad_payment_record (order_id);
CREATE INDEX idx_ad_pr_entity ON ad_payment_record (business_entity_id);
CREATE INDEX idx_ad_pr_resource ON ad_payment_record (resource_id);

-- ---------- 字典表(§5.3/§4.3) ----------
DROP TABLE IF EXISTS ad_dict;
CREATE TABLE ad_dict
(
    `id`             VARCHAR(32) NOT NULL COMMENT 'id',
    `dict_code`      VARCHAR(32)  NOT NULL COMMENT '字典编码(industry/media_type/seal_type/receipt_method/payment_method)',
    `dict_value`     VARCHAR(64)  NOT NULL COMMENT '字典值(枚举码)',
    `dict_label`     VARCHAR(128) NOT NULL COMMENT '显示名',
    `parent_value`   VARCHAR(64)  COMMENT '父级值',
    `sort`           INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `status`         TINYINT      NOT NULL DEFAULT 10 COMMENT '10启用/20停用',
    `organization_id` VARCHAR(32) NOT NULL COMMENT '组织(租户)id',
    `create_time`    BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`    BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`    VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`    VARCHAR(32)  NOT NULL COMMENT '修改人',
    `deleted`        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告字典';

CREATE INDEX idx_ad_dict_code_org ON ad_dict (dict_code, organization_id);

-- ---------- 报表配置(§4.6) ----------
DROP TABLE IF EXISTS ad_report_config;
CREATE TABLE ad_report_config
(
    `id`              VARCHAR(32) NOT NULL COMMENT 'id',
    `report_code`     VARCHAR(64)  NOT NULL COMMENT '报表编码(order-execution/receivable-payable/...)',
    `report_name`     VARCHAR(128) NOT NULL COMMENT '报表名称',
    `filter_fields`   JSON         COMMENT '筛选字段配置',
    `sql_template_key` VARCHAR(128) COMMENT 'SQL模板标识(映射后端固定SQL)',
    `status`          TINYINT      NOT NULL DEFAULT 10 COMMENT '10启用/20停用',
    `organization_id` VARCHAR(32)  NOT NULL COMMENT '组织(租户)id',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`     VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`     VARCHAR(32)  NOT NULL COMMENT '修改人',
    `deleted`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ad_rc_code_org (report_code, organization_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告报表配置';

-- ---------- sys_user 增加 is_cross_entity 标记（财务/老板全主体可见, L-06） ----------
-- 带存在性判断，保证开发循环重跑安全（列已存在则跳过）
SET @ad_db = DATABASE();
SET @ad_col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'is_cross_entity'
);
SET @ad_alter_sql = IF(
    @ad_col_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN `is_cross_entity` TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否跨主体可见:0-否/1-是''',
    'SELECT 1'
);
PREPARE ad_stmt FROM @ad_alter_sql;
EXECUTE ad_stmt;
DEALLOCATE PREPARE ad_stmt;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
