-- V3.1 订单状态机简化：ad_order 新增收款/付款布尔状态
-- 收款状态 0待收/1已收、付款状态 0待付/1已付（区别于现有 receipt_status/media_payment_status 的进度语义）
ALTER TABLE ad_order
    ADD COLUMN `receipt_done` TINYINT NOT NULL DEFAULT 0 COMMENT '收款状态:0待收/1已收' AFTER `media_payment_status`,
    ADD COLUMN `payment_done` TINYINT NOT NULL DEFAULT 0 COMMENT '付款状态:0待付/1已付' AFTER `receipt_done`;

-- ---------- 收款单（一个订单对应一个收款单） ----------
DROP TABLE IF EXISTS ad_receipt;
CREATE TABLE ad_receipt
(
    `id`              VARCHAR(32) NOT NULL COMMENT 'id',
    `receipt_no`      VARCHAR(32) NOT NULL COMMENT '收款单号',
    `order_id`        VARCHAR(32) NOT NULL COMMENT '关联订单',
    `amount`          DECIMAL(15,2) NOT NULL COMMENT '收款金额',
    `receipt_time`    DATE        COMMENT '收款时间',
    `type`            TINYINT     NOT NULL DEFAULT 10 COMMENT '10普通收款/20退款',
    `status`          TINYINT     NOT NULL DEFAULT 0 COMMENT '0草稿/10待审核/20审核通过/30驳回',
    `voucher_url`     VARCHAR(512) COMMENT '凭证',
    `approve_user`    VARCHAR(32) COMMENT '审批人',
    `approve_time`    BIGINT      COMMENT '审批时间',
    `approve_remark`  VARCHAR(512) COMMENT '审批备注',
    `remark`          TEXT        COMMENT '备注',
    `organization_id` VARCHAR(32) NOT NULL COMMENT '组织(租户)id',
    `create_time`     BIGINT      NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT      NOT NULL COMMENT '更新时间',
    `create_user`     VARCHAR(32) NOT NULL COMMENT '创建人',
    `update_user`     VARCHAR(32) NOT NULL COMMENT '修改人',
    `deleted`         TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告收款单';

CREATE INDEX idx_ad_receipt_order ON ad_receipt (order_id);

-- ---------- 付款单（一个订单对应一个付款单，可勾选多个） ----------
DROP TABLE IF EXISTS ad_payment;
CREATE TABLE ad_payment
(
    `id`              VARCHAR(32) NOT NULL COMMENT 'id',
    `payment_no`      VARCHAR(32) NOT NULL COMMENT '付款单号',
    `order_id`        VARCHAR(32) NOT NULL COMMENT '关联订单',
    `amount`          DECIMAL(15,2) NOT NULL COMMENT '付款金额',
    `payment_time`    DATE        COMMENT '付款时间',
    `type`            TINYINT     NOT NULL DEFAULT 10 COMMENT '10普通付款/20坏账',
    `status`          TINYINT     NOT NULL DEFAULT 0 COMMENT '0草稿/10待审核/20审核通过/30驳回',
    `media_ids`       JSON        COMMENT '勾选的id列表(默认全部)',
    `voucher_url`     VARCHAR(512) COMMENT '凭证',
    `approve_user`    VARCHAR(32) COMMENT '审批人',
    `approve_time`    BIGINT      COMMENT '审批时间',
    `approve_remark`  VARCHAR(512) COMMENT '审批备注',
    `remark`          TEXT        COMMENT '备注',
    `organization_id` VARCHAR(32) NOT NULL COMMENT '组织(租户)id',
    `create_time`     BIGINT      NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT      NOT NULL COMMENT '更新时间',
    `create_user`     VARCHAR(32) NOT NULL COMMENT '创建人',
    `update_user`     VARCHAR(32) NOT NULL COMMENT '修改人',
    `deleted`         TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告付款单';

CREATE INDEX idx_ad_payment_order ON ad_payment (order_id);
