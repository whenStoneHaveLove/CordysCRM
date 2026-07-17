-- ---------- ad_support_ticket（M6 广告支持工单）----------
-- CREATE TABLE IF NOT EXISTS，允许开发循环重跑安全。
CREATE TABLE IF NOT EXISTS ad_support_ticket
(
    `id`               VARCHAR(32)  NOT NULL COMMENT 'id',
    `ticket_no`        VARCHAR(64)  NOT NULL COMMENT '工单编号(自动生成)',
    `title`            VARCHAR(255) NOT NULL COMMENT '工单标题',
    `description`      TEXT         COMMENT '工单描述',
    `ticket_type`      TINYINT      NOT NULL COMMENT '工单类型:10订单/20付款/30合同/40素材/50其他',
    `priority`         TINYINT      NOT NULL DEFAULT 20 COMMENT '优先级:10低/20中/30高/40紧急',
    `status`           TINYINT      NOT NULL DEFAULT 0 COMMENT '状态:0待处理/10处理中/20已解决/30已关闭',
    `related_order_id` VARCHAR(32)  NULL COMMENT '关联订单id(可空)',
    `assigned_to`      VARCHAR(32)  NULL COMMENT '指派处理人',
    `resolution`       TEXT         COMMENT '解决方案',
    `close_time`       BIGINT       NULL COMMENT '关闭时间',
    `organization_id`  VARCHAR(32)  NOT NULL COMMENT '组织(租户)id',
    `create_time`      BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`      BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`      VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`      VARCHAR(32)  NOT NULL COMMENT '修改人',
    `deleted`          TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告支持工单(M6)';

-- 索引以 INFORMATION_SCHEMA.STATISTICS 存在性判断，保证开发循环重跑安全（MySQL 不支持 CREATE INDEX IF NOT EXISTS）。
SET @ad_db = DATABASE();

-- idx_ad_st_org
SET @ad_idx_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @ad_db AND TABLE_NAME = 'ad_support_ticket' AND INDEX_NAME = 'idx_ad_st_org'
);
SET @ad_idx_sql = IF(@ad_idx_exists = 0,
    'CREATE INDEX idx_ad_st_org ON ad_support_ticket (organization_id)',
    'SELECT 1');
PREPARE ad_stmt FROM @ad_idx_sql; EXECUTE ad_stmt; DEALLOCATE PREPARE ad_stmt;

-- idx_ad_st_type
SET @ad_idx_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @ad_db AND TABLE_NAME = 'ad_support_ticket' AND INDEX_NAME = 'idx_ad_st_type'
);
SET @ad_idx_sql = IF(@ad_idx_exists = 0,
    'CREATE INDEX idx_ad_st_type ON ad_support_ticket (ticket_type)',
    'SELECT 1');
PREPARE ad_stmt FROM @ad_idx_sql; EXECUTE ad_stmt; DEALLOCATE PREPARE ad_stmt;

-- idx_ad_st_status
SET @ad_idx_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @ad_db AND TABLE_NAME = 'ad_support_ticket' AND INDEX_NAME = 'idx_ad_st_status'
);
SET @ad_idx_sql = IF(@ad_idx_exists = 0,
    'CREATE INDEX idx_ad_st_status ON ad_support_ticket (status)',
    'SELECT 1');
PREPARE ad_stmt FROM @ad_idx_sql; EXECUTE ad_stmt; DEALLOCATE PREPARE ad_stmt;
