-- V3.1 合同双盖附件 & 用印状态扩展
ALTER TABLE ad_contract
    ADD COLUMN `double_seal_file_url` VARCHAR(512) COMMENT '双盖附件URL' AFTER `file_url`,
    MODIFY COLUMN `seal_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0未申请/10审批中/20已用印/30已驳回/40归档审批中/50归档审批驳回/60已归档',
    MODIFY COLUMN `contract_type` TINYINT NOT NULL COMMENT '10框架/20单笔/30服务/40其他';
