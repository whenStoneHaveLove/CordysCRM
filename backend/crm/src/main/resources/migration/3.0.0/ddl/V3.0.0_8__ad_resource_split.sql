-- V3.0.0_8: 拆 ad_resource 为 ad_upstream_agent / ad_downstream_media
DROP TABLE IF EXISTS ad_resource;

CREATE TABLE ad_upstream_agent
(
    `id`              VARCHAR(32)  NOT NULL COMMENT 'id',
    `name`            VARCHAR(128) NOT NULL COMMENT '代理名称',
    `credit_code`     VARCHAR(64)  NULL COMMENT '社会信用代码',
    `signing_entity`  VARCHAR(128) NULL COMMENT '签约主体',
    `contact_person`  VARCHAR(64)  NULL COMMENT '联系人',
    `contact_phone`   VARCHAR(32)  NULL COMMENT '电话',
    `cooperation_status` TINYINT   NOT NULL DEFAULT 10 COMMENT '合作状态:10正常/20停用',
    `status`          TINYINT      NOT NULL DEFAULT 10 COMMENT '状态:10正常/20停用',
    `remark`          VARCHAR(512) NULL COMMENT '备注',
    `business_entity_id` VARCHAR(32) NOT NULL COMMENT '业务主体',
    `organization_id` VARCHAR(32)  NOT NULL COMMENT '组织(租户)id',
    `deleted`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `create_user`     VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_time`     BIGINT       NULL COMMENT '更新时间',
    `update_user`     VARCHAR(32)  NULL COMMENT '修改人',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '上游代理';

CREATE TABLE ad_downstream_media
(
    `id`              VARCHAR(32)  NOT NULL COMMENT 'id',
    `name`            VARCHAR(128) NOT NULL COMMENT '媒体名称',
    `media_type`      VARCHAR(64)  NULL COMMENT '媒体类型',
    `channel`         VARCHAR(128) NULL COMMENT '覆盖渠道',
    `rate_card`       VARCHAR(512) NULL COMMENT '刊例价',
    `discount_policy` VARCHAR(512) NULL COMMENT '折扣政策',
    `contact_person`  VARCHAR(64)  NULL COMMENT '联系人',
    `contact_phone`   VARCHAR(32)  NULL COMMENT '电话',
    `cooperation_status` TINYINT   NOT NULL DEFAULT 10 COMMENT '合作状态:10正常/20停用',
    `status`          TINYINT      NOT NULL DEFAULT 10 COMMENT '状态:10正常/20停用',
    `business_entity_id` VARCHAR(32) NOT NULL COMMENT '业务主体',
    `organization_id` VARCHAR(32)  NOT NULL COMMENT '组织(租户)id',
    `deleted`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除:0-否/1-是',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `create_user`     VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_time`     BIGINT       NULL COMMENT '更新时间',
    `update_user`     VARCHAR(32)  NULL COMMENT '修改人',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '下游媒体';
