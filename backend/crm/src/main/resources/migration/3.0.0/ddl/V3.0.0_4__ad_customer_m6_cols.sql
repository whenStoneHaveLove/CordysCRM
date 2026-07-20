-- ============================================================
-- CordysCRM 广告客户表 M6 扩展列（幂等迁移，对齐 AdCustomer.java 领域模型）
-- 版本: 3.0.0.4  描述: ad_customer_m6_cols
-- 说明:
--   ad_customer 基础四列(name/brand/industry_code/signing_entity)已在
--   V3.0.0_1__ad_system_v31.sql 建立；此处补齐 M6 扩展列。
--   采用「先判断列是否存在再 ADD」的幂等写法，开发循环重跑安全。
-- 注意:
--   与 dml/V3.0.0_1_3__m6_customer_cols.sql 列定义保持一致（该脚本已存在且幂等）；
--   本脚本将列定义固化进 DDL 层，作为 ad_customer 的权威建表补充，二者不冲突。
--   版本号 V3.0.0.4 大于现有最大迁移版本，Flyway 顺序执行且不重名。
-- ============================================================
SET SESSION innodb_lock_wait_timeout = 7200;

SET @ad_db = DATABASE();

-- 1) contact_person 联系人
SET @ad_col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db
      AND TABLE_NAME = 'ad_customer'
      AND COLUMN_NAME = 'contact_person'
);
SET @ad_alter_sql = IF(
    @ad_col_exists = 0,
    'ALTER TABLE ad_customer ADD COLUMN `contact_person` VARCHAR(64) NULL COMMENT ''联系人(M6)''',
    'SELECT 1'
);
PREPARE ad_stmt FROM @ad_alter_sql;
EXECUTE ad_stmt;
DEALLOCATE PREPARE ad_stmt;

-- 2) contact_phone 联系电话
SET @ad_col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db
      AND TABLE_NAME = 'ad_customer'
      AND COLUMN_NAME = 'contact_phone'
);
SET @ad_alter_sql = IF(
    @ad_col_exists = 0,
    'ALTER TABLE ad_customer ADD COLUMN `contact_phone` VARCHAR(32) NULL COMMENT ''联系电话(M6)''',
    'SELECT 1'
);
PREPARE ad_stmt FROM @ad_alter_sql;
EXECUTE ad_stmt;
DEALLOCATE PREPARE ad_stmt;

-- 3) email 邮箱
SET @ad_col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db
      AND TABLE_NAME = 'ad_customer'
      AND COLUMN_NAME = 'email'
);
SET @ad_alter_sql = IF(
    @ad_col_exists = 0,
    'ALTER TABLE ad_customer ADD COLUMN `email` VARCHAR(128) NULL COMMENT ''邮箱(M6)''',
    'SELECT 1'
);
PREPARE ad_stmt FROM @ad_alter_sql;
EXECUTE ad_stmt;
DEALLOCATE PREPARE ad_stmt;

-- 4) address 地址
SET @ad_col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db
      AND TABLE_NAME = 'ad_customer'
      AND COLUMN_NAME = 'address'
);
SET @ad_alter_sql = IF(
    @ad_col_exists = 0,
    'ALTER TABLE ad_customer ADD COLUMN `address` VARCHAR(255) NULL COMMENT ''地址(M6)''',
    'SELECT 1'
);
PREPARE ad_stmt FROM @ad_alter_sql;
EXECUTE ad_stmt;
DEALLOCATE PREPARE ad_stmt;

-- 5) industry 行业(M6 自由文本，与 industry_code 字典并存)
SET @ad_col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db
      AND TABLE_NAME = 'ad_customer'
      AND COLUMN_NAME = 'industry'
);
SET @ad_alter_sql = IF(
    @ad_col_exists = 0,
    'ALTER TABLE ad_customer ADD COLUMN `industry` VARCHAR(64) NULL COMMENT ''行业(M6)''',
    'SELECT 1'
);
PREPARE ad_stmt FROM @ad_alter_sql;
EXECUTE ad_stmt;
DEALLOCATE PREPARE ad_stmt;

-- 6) customer_level 客户等级:10VIP/20普通/30潜力(M6)
SET @ad_col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db
      AND TABLE_NAME = 'ad_customer'
      AND COLUMN_NAME = 'customer_level'
);
SET @ad_alter_sql = IF(
    @ad_col_exists = 0,
    'ALTER TABLE ad_customer ADD COLUMN `customer_level` TINYINT NULL COMMENT ''客户等级:10VIP/20普通/30潜力(M6)''',
    'SELECT 1'
);
PREPARE ad_stmt FROM @ad_alter_sql;
EXECUTE ad_stmt;
DEALLOCATE PREPARE ad_stmt;

-- 7) status 客户状态:0活跃/10非活跃/20黑名单(M6)
SET @ad_col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db
      AND TABLE_NAME = 'ad_customer'
      AND COLUMN_NAME = 'status'
);
SET @ad_alter_sql = IF(
    @ad_col_exists = 0,
    'ALTER TABLE ad_customer ADD COLUMN `status` TINYINT NOT NULL DEFAULT 0 COMMENT ''客户状态:0活跃/10非活跃/20黑名单(M6)''',
    'SELECT 1'
);
PREPARE ad_stmt FROM @ad_alter_sql;
EXECUTE ad_stmt;
DEALLOCATE PREPARE ad_stmt;

-- 8) remark 备注(M6)
SET @ad_col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db
      AND TABLE_NAME = 'ad_customer'
      AND COLUMN_NAME = 'remark'
);
SET @ad_alter_sql = IF(
    @ad_col_exists = 0,
    'ALTER TABLE ad_customer ADD COLUMN `remark` VARCHAR(512) NULL COMMENT ''备注(M6)''',
    'SELECT 1'
);
PREPARE ad_stmt FROM @ad_alter_sql;
EXECUTE ad_stmt;
DEALLOCATE PREPARE ad_stmt;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
