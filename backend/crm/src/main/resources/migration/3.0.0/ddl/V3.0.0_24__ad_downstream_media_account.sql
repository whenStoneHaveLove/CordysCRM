-- V3.0.0_24 下游客户银行账户 + 付款单明细关联账户

-- 下游客户银行账户（一个下游客户可挂多个账户，逻辑删除/可停用，无独立权限）
DROP TABLE IF EXISTS ad_downstream_media_account;
CREATE TABLE ad_downstream_media_account
(
    `id`                 VARCHAR(32)  NOT NULL COMMENT 'id',
    `downstream_media_id` VARCHAR(32) NOT NULL COMMENT '下游客户id(ad_downstream_media.id)',
    `payee_name`         VARCHAR(128) NOT NULL COMMENT '收款人全称',
    `bank_name`          VARCHAR(128) NOT NULL COMMENT '开户行',
    `bank_account`       VARCHAR(64)  NOT NULL COMMENT '银行账号',
    `disabled`           TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否停用:0-启用/1-停用',
    `organization_id`    VARCHAR(32)  NOT NULL COMMENT '组织(租户)id',
    `create_time`        BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`        BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`        VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`        VARCHAR(32)  NOT NULL COMMENT '修改人',
    `deleted`            TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告下游客户银行账户';

CREATE INDEX idx_ad_dma_media ON ad_downstream_media_account (downstream_media_id);
CREATE INDEX idx_ad_dma_org ON ad_downstream_media_account (organization_id);

-- 付款单明细增加账户信息
ALTER TABLE ad_payment_media ADD COLUMN `account_id` VARCHAR(32) DEFAULT NULL COMMENT '下游客户银行账户id(ad_downstream_media_account.id)';
CREATE INDEX idx_ad_payment_media_account ON ad_payment_media (account_id);
