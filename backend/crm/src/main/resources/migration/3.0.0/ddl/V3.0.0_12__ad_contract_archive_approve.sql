-- V3.1 合同归档审批字段：审批意见、审批人、审批时间
ALTER TABLE ad_contract
    ADD COLUMN `archive_approve_remark` VARCHAR(512) COMMENT '归档审批意见' AFTER `double_seal_file_url`,
    ADD COLUMN `archive_approve_user` VARCHAR(32) COMMENT '归档审批人' AFTER `archive_approve_remark`,
    ADD COLUMN `archive_approve_time` BIGINT COMMENT '归档审批时间' AFTER `archive_approve_user`;