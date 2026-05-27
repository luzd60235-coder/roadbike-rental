-- 添加缺失的列到 rental_store 表
ALTER TABLE rental_store ADD COLUMN store_image_url VARCHAR(500) DEFAULT NULL COMMENT '门店图片URL' AFTER longitude;
