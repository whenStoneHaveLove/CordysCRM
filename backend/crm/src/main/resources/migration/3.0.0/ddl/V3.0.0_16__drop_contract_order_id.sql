-- V3.0.0_16: 移除 ad_contract.order_id 单字段关联，统一使用 ad_order_contract 中间表（一对多）
ALTER TABLE ad_contract
    DROP COLUMN `order_id`;
