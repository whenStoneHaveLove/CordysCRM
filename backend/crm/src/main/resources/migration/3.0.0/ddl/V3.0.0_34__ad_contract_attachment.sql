-- 合同附件子表（用印附件 / 双盖附件，支持多文件）
CREATE TABLE `ad_contract_attachment` (
  `id` varchar(32) NOT NULL,
  `contract_id` varchar(32) DEFAULT NULL,
  `type` tinyint DEFAULT NULL COMMENT '附件类型:10用印附件/20双盖附件',
  `file_url` varchar(512) DEFAULT NULL,
  `file_name` varchar(128) DEFAULT NULL,
  `organization_id` varchar(32) DEFAULT NULL,
  `create_time` bigint DEFAULT NULL,
  `create_user` varchar(32) DEFAULT NULL,
  `update_time` bigint DEFAULT NULL,
  `update_user` varchar(32) DEFAULT NULL,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_ad_contract_attachment_contract_id` (`contract_id`),
  KEY `idx_ad_contract_attachment_type` (`contract_id`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同附件(用印附件/双盖附件,支持多文件)';
