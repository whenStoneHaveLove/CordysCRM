-- ---------- 订单-下游媒体中间表 ----------
DROP TABLE IF EXISTS ad_order_downstream_media;
CREATE TABLE ad_order_downstream_media
(
    `id`              VARCHAR(32) NOT NULL COMMENT 'id',
    `order_id`        VARCHAR(32) NOT NULL COMMENT '订单id',
    `downstream_media_id` VARCHAR(32) NOT NULL COMMENT '下游媒体id',
    `organization_id` VARCHAR(32) NOT NULL COMMENT '组织(租户)id',
    `create_time`     BIGINT      NOT NULL COMMENT '创建时间',
    `deleted`         TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ad_odm (order_id, downstream_media_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单-下游媒体关联';

CREATE INDEX idx_ad_odm_order ON ad_order_downstream_media (order_id);
CREATE INDEX idx_ad_odm_media ON ad_order_downstream_media (downstream_media_id);
