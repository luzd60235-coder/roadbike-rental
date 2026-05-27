-- 全面检查并修复数据库表结构

-- 1. 检查 rental_store 表
-- 如果 store_image_url 不存在则添加
SET @dbname = 'road_bike_rental';
SET @tablename = 'rental_store';
SET @columnname = 'store_image_url';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE rental_store ADD COLUMN store_image_url VARCHAR(500) DEFAULT NULL COMMENT ''门店图片URL'' AFTER longitude'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2. 检查 bike_inventory 表
SET @columnname = 'last_maintenance_at';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'bike_inventory' AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE bike_inventory ADD COLUMN last_maintenance_at DATETIME DEFAULT NULL COMMENT ''最后保养时间'' AFTER bike_status'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SELECT 'Database structure check completed' AS status;
