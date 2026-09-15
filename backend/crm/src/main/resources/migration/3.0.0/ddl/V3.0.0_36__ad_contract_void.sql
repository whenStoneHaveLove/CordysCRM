ALTER TABLE ad_contract
    ADD COLUMN void_reason          VARCHAR(512) NULL COMMENT '作废原因',
    ADD COLUMN void_applicant_id    VARCHAR(64)  NULL COMMENT '作废申请人',
    ADD COLUMN void_applied_at      BIGINT       NULL COMMENT '作废申请时间',
    ADD COLUMN void_approve_remark  VARCHAR(512) NULL COMMENT '作废审批意见',
    ADD COLUMN void_approve_user    VARCHAR(64)  NULL COMMENT '作废审批人',
    ADD COLUMN void_approve_time    BIGINT       NULL COMMENT '作废审批时间';
