-- V3.0.0_23 付款单各下游客户付款返点明细

DROP TABLE IF EXISTS ad_payment_media;
CREATE TABLE ad_payment_media
(
    `id`                         VARCHAR(32)  NOT NULL COMMENT 'id',
    `payment_id`                 VARCHAR(32)  NOT NULL COMMENT '付款单id',
    `order_id`                   VARCHAR(32)  NOT NULL COMMENT '关联订单id(冗余)',
    `order_downstream_media_id`  VARCHAR(32)  NOT NULL COMMENT '订单-下游客户中间表id',
    `media_id`                   VARCHAR(32)  NOT NULL COMMENT '下游客户id',
    `media_name`                 VARCHAR(128) COMMENT '下游客户名称(冗余)',
    `payable_amount`             DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '应付金额',
    `no_rebate_amount`           DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '不记返金额',
    `rebate_mode`                TINYINT      NOT NULL DEFAULT 10 COMMENT '返点方式:10-比例/20-固定金额',
    `rebate_value`               DECIMAL(15,4) NOT NULL DEFAULT 0 COMMENT '返点值',
    `rebate_amount`              DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '返点金额',
    `actual_payable`             DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '实际应付',
    `paid_amount`                DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '本次付款金额',
    `organization_id`            VARCHAR(32)  NOT NULL COMMENT '组织(租户)id',
    `create_time`                BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`                BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`                VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`                VARCHAR(32)  NOT NULL COMMENT '修改人',
    `deleted`                    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告付款单-各下游客户付款明细';

CREATE INDEX idx_ad_payment_media_payment ON ad_payment_media (payment_id);
CREATE INDEX idx_ad_payment_media_order ON ad_payment_media (order_id);