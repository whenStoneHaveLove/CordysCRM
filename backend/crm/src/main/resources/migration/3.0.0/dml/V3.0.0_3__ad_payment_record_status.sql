-- ---------- ad_payment_record 增加 status（报表中心付款汇总 M6，V3.1）----------
-- 带存在性判断，保证开发循环重跑安全（列已存在则跳过）。
-- 模式参照同目录 M6 追加列脚本（INFORMATION_SCHEMA + PREPARE/EXECUTE）。
--
-- 背景：ExtAdReportMapper.paymentSummary 按 p.status 分组统计已付/未付/逾期金额，
--       但原 DDL(V3.0.0_1__ad_system_v31.sql) 建表时漏加该列，导致报表中心 500
--       (Unknown column 'p.status' in 'field list')。此处补齐该列。
--
-- status 取值语义（依据 paymentSummary 中 CASE 用法推断，供业务侧回填参考）：
--   0 = 未付(unpaid) / 10 = 已付(paid) / 20 = 逾期(overdue)
--   默认 0（未付），类型 TINYINT，与 ad_order / ad_customer 等其它 status 列保持一致。
SET @ad_db = DATABASE();

SET @ad_col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db
      AND TABLE_NAME = 'ad_payment_record'
      AND COLUMN_NAME = 'status'
);
SET @ad_alter_sql = IF(
    @ad_col_exists = 0,
    'ALTER TABLE ad_payment_record ADD COLUMN `status` TINYINT NOT NULL DEFAULT 0 COMMENT ''付款状态:0未付/10已付/20逾期(M6 报表)''',
    'SELECT 1'
);
PREPARE ad_stmt FROM @ad_alter_sql;
EXECUTE ad_stmt;
DEALLOCATE PREPARE ad_stmt;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
