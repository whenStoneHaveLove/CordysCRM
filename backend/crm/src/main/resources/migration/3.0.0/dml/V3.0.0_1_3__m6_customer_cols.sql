-- ---------- ad_customer 增加M6字段（广告客户管理）----------
-- 带存在性判断，保证开发循环重跑安全（列已存在则跳过）。
SET @ad_db = DATABASE();

-- 1) contact_person
SET @ad_col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db AND TABLE_NAME = 'ad_customer' AND COLUMN_NAME = 'contact_person'
);
SET @ad_alter_sql = IF(@ad_col_exists = 0,
    'ALTER TABLE ad_customer ADD COLUMN `contact_person` VARCHAR(64) NULL COMMENT ''联系人(M6)''',
    'SELECT 1');
PREPARE ad_stmt FROM @ad_alter_sql; EXECUTE ad_stmt; DEALLOCATE PREPARE ad_stmt;

-- 2) contact_phone
SET @ad_col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db AND TABLE_NAME = 'ad_customer' AND COLUMN_NAME = 'contact_phone'
);
SET @ad_alter_sql = IF(@ad_col_exists = 0,
    'ALTER TABLE ad_customer ADD COLUMN `contact_phone` VARCHAR(32) NULL COMMENT ''联系电话(M6)''',
    'SELECT 1');
PREPARE ad_stmt FROM @ad_alter_sql; EXECUTE ad_stmt; DEALLOCATE PREPARE ad_stmt;

-- 3) email
SET @ad_col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db AND TABLE_NAME = 'ad_customer' AND COLUMN_NAME = 'email'
);
SET @ad_alter_sql = IF(@ad_col_exists = 0,
    'ALTER TABLE ad_customer ADD COLUMN `email` VARCHAR(128) NULL COMMENT ''邮箱(M6)''',
    'SELECT 1');
PREPARE ad_stmt FROM @ad_alter_sql; EXECUTE ad_stmt; DEALLOCATE PREPARE ad_stmt;

-- 4) address
SET @ad_col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db AND TABLE_NAME = 'ad_customer' AND COLUMN_NAME = 'address'
);
SET @ad_alter_sql = IF(@ad_col_exists = 0,
    'ALTER TABLE ad_customer ADD COLUMN `address` VARCHAR(255) NULL COMMENT ''地址(M6)''',
    'SELECT 1');
PREPARE ad_stmt FROM @ad_alter_sql; EXECUTE ad_stmt; DEALLOCATE PREPARE ad_stmt;

-- 5) industry
SET @ad_col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db AND TABLE_NAME = 'ad_customer' AND COLUMN_NAME = 'industry'
);
SET @ad_alter_sql = IF(@ad_col_exists = 0,
    'ALTER TABLE ad_customer ADD COLUMN `industry` VARCHAR(64) NULL COMMENT ''行业(M6)''',
    'SELECT 1');
PREPARE ad_stmt FROM @ad_alter_sql; EXECUTE ad_stmt; DEALLOCATE PREPARE ad_stmt;

-- 6) customer_level
SET @ad_col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db AND TABLE_NAME = 'ad_customer' AND COLUMN_NAME = 'customer_level'
);
SET @ad_alter_sql = IF(@ad_col_exists = 0,
    'ALTER TABLE ad_customer ADD COLUMN `customer_level` TINYINT NULL COMMENT ''客户等级:10VIP/20普通/30潜力(M6)''',
    'SELECT 1');
PREPARE ad_stmt FROM @ad_alter_sql; EXECUTE ad_stmt; DEALLOCATE PREPARE ad_stmt;

-- 7) status (M6客户状态)
SET @ad_col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db AND TABLE_NAME = 'ad_customer' AND COLUMN_NAME = 'status'
);
SET @ad_alter_sql = IF(@ad_col_exists = 0,
    'ALTER TABLE ad_customer ADD COLUMN `status` TINYINT NOT NULL DEFAULT 0 COMMENT ''客户状态:0活跃/10非活跃/20黑名单(M6)''',
    'SELECT 1');
PREPARE ad_stmt FROM @ad_alter_sql; EXECUTE ad_stmt; DEALLOCATE PREPARE ad_stmt;

-- 8) remark
SET @ad_col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db AND TABLE_NAME = 'ad_customer' AND COLUMN_NAME = 'remark'
);
SET @ad_alter_sql = IF(@ad_col_exists = 0,
    'ALTER TABLE ad_customer ADD COLUMN `remark` VARCHAR(512) NULL COMMENT ''备注(M6)''',
    'SELECT 1');
PREPARE ad_stmt FROM @ad_alter_sql; EXECUTE ad_stmt; DEALLOCATE PREPARE ad_stmt;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
