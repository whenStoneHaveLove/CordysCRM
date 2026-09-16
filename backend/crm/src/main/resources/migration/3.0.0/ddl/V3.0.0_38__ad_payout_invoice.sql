-- ============================================================
-- CordysCRM 广告下单系统 —— 付款单发票（DDL）
-- 版本: 3.0.0  描述: ad_payout_invoice
-- 说明: 付款单支持上传发票（单文件），审批中心与付款详情可预览/下载。
--       采用独立子表，便于后续扩展为多文件而无需改主表结构。
-- 注意: 列类型必须与 cn.cordys.common.domain.BaseModel 一致
--       （id/createUser/updateUser = String → VARCHAR(32)，createTime/updateTime = Long → BIGINT）
-- ============================================================
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS `ad_payout_invoice`
(
    `id`              VARCHAR(32)  NOT NULL COMMENT '主键',
    `payout_id`       VARCHAR(32)  NOT NULL COMMENT '付款单ID(ad_payout.id)',
    `file_name`       VARCHAR(255) NOT NULL COMMENT '发票文件名',
    `file_url`        VARCHAR(512) NOT NULL COMMENT '发票文件访问路径/附件ID',
    `invoice_no`      VARCHAR(128)          DEFAULT NULL COMMENT '发票号码',
    `remark`          VARCHAR(512)          DEFAULT NULL COMMENT '备注',
    `organization_id` VARCHAR(32)  NOT NULL COMMENT '组织(租户)id',
    `create_user`     VARCHAR(32)           DEFAULT NULL COMMENT '创建人',
    `update_user`     VARCHAR(32)           DEFAULT NULL COMMENT '修改人',
    `create_time`     BIGINT                DEFAULT NULL COMMENT '创建时间',
    `update_time`     BIGINT                DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payout_invoice_payout` (`payout_id`),
    KEY `idx_payout_invoice_org` (`organization_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='付款单发票';

SET SESSION innodb_lock_wait_timeout = DEFAULT;
