-- ---------- ad_resource 增加M6字段（广告资源管理）----------
-- 带存在性判断，保证开发循环重跑安全（列已存在则跳过）。
SET @ad_db = DATABASE();

-- 1) position
SET @ad_col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db AND TABLE_NAME = 'ad_resource' AND COLUMN_NAME = 'position'
);
SET @ad_alter_sql = IF(@ad_col_exists = 0,
    'ALTER TABLE ad_resource ADD COLUMN `position` VARCHAR(255) NULL COMMENT ''广告位(M6)''',
    'SELECT 1');
PREPARE ad_stmt FROM @ad_alter_sql; EXECUTE ad_stmt; DEALLOCATE PREPARE ad_stmt;

-- 2) daily_impressions
SET @ad_col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db AND TABLE_NAME = 'ad_resource' AND COLUMN_NAME = 'daily_impressions'
);
SET @ad_alter_sql = IF(@ad_col_exists = 0,
    'ALTER TABLE ad_resource ADD COLUMN `daily_impressions` BIGINT NULL COMMENT ''日均曝光量(M6)''',
    'SELECT 1');
PREPARE ad_stmt FROM @ad_alter_sql; EXECUTE ad_stmt; DEALLOCATE PREPARE ad_stmt;

-- 3) unit_price
SET @ad_col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db AND TABLE_NAME = 'ad_resource' AND COLUMN_NAME = 'unit_price'
);
SET @ad_alter_sql = IF(@ad_col_exists = 0,
    'ALTER TABLE ad_resource ADD COLUMN `unit_price` DECIMAL(12,2) NULL COMMENT ''单价(M6)''',
    'SELECT 1');
PREPARE ad_stmt FROM @ad_alter_sql; EXECUTE ad_stmt; DEALLOCATE PREPARE ad_stmt;

-- 4) remark
SET @ad_col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db AND TABLE_NAME = 'ad_resource' AND COLUMN_NAME = 'remark'
);
SET @ad_alter_sql = IF(@ad_col_exists = 0,
    'ALTER TABLE ad_resource ADD COLUMN `remark` VARCHAR(512) NULL COMMENT ''备注(M6)''',
    'SELECT 1');
PREPARE ad_stmt FROM @ad_alter_sql; EXECUTE ad_stmt; DEALLOCATE PREPARE ad_stmt;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
