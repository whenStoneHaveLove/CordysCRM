-- ---------- ad_contract 增加 change_order_id（关联变更单，M5 T-40） ----------
-- 带存在性判断，保证开发循环重跑安全（列已存在则跳过）。
-- 模式参照 M1 的 sys_user.is_cross_entity 追加列（INFORMATION_SCHEMA 检查）。
SET @ad_db = DATABASE();
SET @ad_col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @ad_db
      AND TABLE_NAME = 'ad_contract'
      AND COLUMN_NAME = 'change_order_id'
);
SET @ad_alter_sql = IF(
    @ad_col_exists = 0,
    'ALTER TABLE ad_contract ADD COLUMN `change_order_id` VARCHAR(32) NULL COMMENT ''关联变更单id（M5，可空）''',
    'SELECT 1'
);
PREPARE ad_stmt FROM @ad_alter_sql;
EXECUTE ad_stmt;
DEALLOCATE PREPARE ad_stmt;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
