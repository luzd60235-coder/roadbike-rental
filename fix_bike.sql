-- 添加缺失的列
ALTER TABLE bike_inventory ADD COLUMN last_maintenance_at DATETIME DEFAULT NULL COMMENT '最后保养时间' AFTER bike_status;
