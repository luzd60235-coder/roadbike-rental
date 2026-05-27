-- 都市圈公路车租赁服务系统数据库设计
-- 核心模块：管理员、用户、门店、门店员工、车辆配置、车辆实例、价格策略、租赁订单、结算、收藏、维修记录

CREATE DATABASE IF NOT EXISTS road_bike_rental
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE road_bike_rental;

SET FOREIGN_KEY_CHECKS = 0;

DROP VIEW IF EXISTS vw_financial_overview;

DROP TABLE IF EXISTS user_favorite;
DROP TABLE IF EXISTS maintenance_record;
DROP TABLE IF EXISTS rental_order_settlement;
DROP TABLE IF EXISTS rental_order;
DROP TABLE IF EXISTS bike_inventory;
DROP TABLE IF EXISTS bike_pricing;
DROP TABLE IF EXISTS bike_model;
DROP TABLE IF EXISTS store_staff;
DROP TABLE IF EXISTS rental_store;
DROP TABLE IF EXISTS app_user;
DROP TABLE IF EXISTS sys_admin;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sys_admin (
  admin_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
  username VARCHAR(50) NOT NULL COMMENT '登录账号',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值',
  real_name VARCHAR(50) NOT NULL COMMENT '管理员姓名',
  phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  email VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
  id_card_no VARCHAR(30) DEFAULT NULL COMMENT '身份证号',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
  last_login_at DATETIME DEFAULT NULL COMMENT '最后登录时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_sys_admin_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台管理员表';

CREATE TABLE app_user (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(50) NOT NULL COMMENT '登录账号',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值',
  nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  gender TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0未知，1男，2女',
  phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  id_card_no VARCHAR(30) DEFAULT NULL COMMENT '身份证号',
  member_level VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '会员等级',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
  last_login_at DATETIME DEFAULT NULL COMMENT '最后登录时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_app_user_username (username),
  UNIQUE KEY uk_app_user_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE rental_store (
  store_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '门店ID',
  store_name VARCHAR(100) NOT NULL COMMENT '门店名称',
  contact_name VARCHAR(50) DEFAULT NULL COMMENT '负责人姓名',
  contact_phone VARCHAR(20) NOT NULL COMMENT '门店联系电话',
  province VARCHAR(30) DEFAULT NULL COMMENT '省份',
  city VARCHAR(30) DEFAULT NULL COMMENT '城市',
  district VARCHAR(30) DEFAULT NULL COMMENT '区县',
  detail_address VARCHAR(255) NOT NULL COMMENT '详细地址',
  latitude DECIMAL(10, 6) DEFAULT NULL COMMENT '纬度',
  longitude DECIMAL(10, 6) DEFAULT NULL COMMENT '经度',
  store_image_url VARCHAR(500) DEFAULT NULL COMMENT '门店图片URL',
  business_hours VARCHAR(100) DEFAULT NULL COMMENT '营业时间',
  inventory_capacity INT NOT NULL DEFAULT 0 COMMENT '车辆容量',
  description VARCHAR(255) DEFAULT NULL COMMENT '门店描述',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1营业中，0停用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_rental_store_city_status (city, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店信息表';

CREATE TABLE store_staff (
  staff_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '员工ID',
  store_id BIGINT NOT NULL COMMENT '所属门店ID',
  username VARCHAR(50) NOT NULL COMMENT '登录账号',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值',
  staff_name VARCHAR(50) NOT NULL COMMENT '员工姓名',
  gender TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0未知，1男，2女',
  phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  id_card_no VARCHAR(30) DEFAULT NULL COMMENT '身份证号',
  job_title VARCHAR(30) NOT NULL DEFAULT 'clerk' COMMENT '岗位：manager店长、clerk店员、mechanic维修员',
  work_schedule VARCHAR(100) DEFAULT NULL COMMENT '员工工作时间',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1在职，0停用',
  last_login_at DATETIME DEFAULT NULL COMMENT '最后登录时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_store_staff_username (username),
  UNIQUE KEY uk_store_staff_phone (phone),
  KEY idx_store_staff_store_id (store_id),
  CONSTRAINT fk_store_staff_store
    FOREIGN KEY (store_id) REFERENCES rental_store (store_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店员工表';

CREATE TABLE bike_model (
  model_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '车辆配置ID',
  brand_name VARCHAR(50) NOT NULL COMMENT '品牌',
  series_name VARCHAR(50) DEFAULT NULL COMMENT '系列',
  model_name VARCHAR(100) NOT NULL COMMENT '车型名称',
  bike_type VARCHAR(30) NOT NULL DEFAULT 'road-bike' COMMENT '车辆类型',
  frame_material VARCHAR(50) DEFAULT NULL COMMENT '车架材质',
  gear_system VARCHAR(100) DEFAULT NULL COMMENT '变速套件',
  brake_type VARCHAR(50) DEFAULT NULL COMMENT '刹车类型',
  wheel_size VARCHAR(30) DEFAULT NULL COMMENT '轮组规格',
  bike_weight DECIMAL(5, 2) DEFAULT NULL COMMENT '整车重量(kg)',
  suggested_height_min SMALLINT DEFAULT NULL COMMENT '建议身高下限(cm)',
  suggested_height_max SMALLINT DEFAULT NULL COMMENT '建议身高上限(cm)',
  market_price DECIMAL(10, 2) DEFAULT NULL COMMENT '市场价格',
  cover_image_url VARCHAR(255) DEFAULT NULL COMMENT '封面图地址',
  description TEXT COMMENT '车辆配置描述',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_bike_model_brand_status (brand_name, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公路车配置表';

CREATE TABLE bike_pricing (
  pricing_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '价格策略ID',
  model_id BIGINT NOT NULL COMMENT '车辆配置ID',
  daily_rent DECIMAL(10, 2) NOT NULL COMMENT '日租金',
  deposit_amount DECIMAL(10, 2) NOT NULL COMMENT '押金',
  overtime_fee_per_hour DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '超时每小时费用',
  effective_from DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
  effective_to DATETIME DEFAULT NULL COMMENT '失效时间',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1生效，0停用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_bike_pricing_model_status (model_id, status, effective_from),
  CONSTRAINT fk_bike_pricing_model
    FOREIGN KEY (model_id) REFERENCES bike_model (model_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁价格策略表';

CREATE TABLE bike_inventory (
  bike_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '车辆实例ID',
  bike_code VARCHAR(50) NOT NULL COMMENT '车辆编码',
  model_id BIGINT NOT NULL COMMENT '车辆配置ID',
  frame_no VARCHAR(100) NOT NULL COMMENT '车架编号',
  frame_size VARCHAR(20) NOT NULL COMMENT '尺码',
  color VARCHAR(30) DEFAULT NULL COMMENT '颜色',
  purchase_date DATE DEFAULT NULL COMMENT '采购日期',
  purchase_price DECIMAL(10, 2) DEFAULT NULL COMMENT '采购价格',
  mileage_km DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '累计里程(km)',
  gps_device_no VARCHAR(50) DEFAULT NULL COMMENT 'GPS设备编号',
  current_store_id BIGINT NOT NULL COMMENT '当前所在门店ID',
  bike_status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1可租赁，2已预订，3租赁中，4维修中，5停用',
  last_maintenance_at DATETIME DEFAULT NULL COMMENT '最近维护时间',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_bike_inventory_code (bike_code),
  UNIQUE KEY uk_bike_inventory_frame_no (frame_no),
  KEY idx_bike_inventory_model_id (model_id),
  KEY idx_bike_inventory_store_status (current_store_id, bike_status),
  CONSTRAINT fk_bike_inventory_model
    FOREIGN KEY (model_id) REFERENCES bike_model (model_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_bike_inventory_store
    FOREIGN KEY (current_store_id) REFERENCES rental_store (store_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公路车库存表';

CREATE TABLE rental_order (
  order_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
  order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
  user_id BIGINT NOT NULL COMMENT '下单用户ID',
  bike_id BIGINT NOT NULL COMMENT '租赁车辆ID',
  pricing_id BIGINT DEFAULT NULL COMMENT '采用的价格策略ID',
  pickup_store_id BIGINT NOT NULL COMMENT '取车门店ID',
  return_store_id BIGINT DEFAULT NULL COMMENT '还车门店ID',
  created_staff_id BIGINT DEFAULT NULL COMMENT '代办订单员工ID',
  planned_start_time DATETIME NOT NULL COMMENT '计划取车时间',
  planned_end_time DATETIME NOT NULL COMMENT '计划还车时间',
  actual_pickup_time DATETIME DEFAULT NULL COMMENT '实际取车时间',
  actual_return_time DATETIME DEFAULT NULL COMMENT '实际还车时间',
  rent_days INT NOT NULL DEFAULT 1 COMMENT '租赁天数',
  daily_rent_amount DECIMAL(10, 2) NOT NULL COMMENT '下单时日租金',
  rent_amount DECIMAL(10, 2) NOT NULL COMMENT '租金金额',
  deposit_amount DECIMAL(10, 2) NOT NULL COMMENT '押金金额',
  overtime_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '超时费用',
  damage_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '损坏赔偿费用',
  other_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '其他费用',
  total_amount DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
  order_status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1待支付，2待取车，3租赁中，4待结算，5已完成，6已取消',
  payment_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0未支付，1已支付，2部分退款，3已完成退款',
  deposit_refund_status TINYINT NOT NULL DEFAULT 0 COMMENT '押金退款状态：0未退款，1部分退款，2已退款',
  cancel_reason VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_rental_order_no (order_no),
  KEY idx_rental_order_user_id (user_id),
  KEY idx_rental_order_bike_id (bike_id),
  KEY idx_rental_order_status (order_status),
  KEY idx_rental_order_pickup_store (pickup_store_id),
  KEY idx_rental_order_return_store (return_store_id),
  KEY idx_rental_order_created_staff (created_staff_id),
  CONSTRAINT fk_rental_order_user
    FOREIGN KEY (user_id) REFERENCES app_user (user_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_rental_order_bike
    FOREIGN KEY (bike_id) REFERENCES bike_inventory (bike_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_rental_order_pricing
    FOREIGN KEY (pricing_id) REFERENCES bike_pricing (pricing_id)
    ON UPDATE CASCADE
    ON DELETE SET NULL,
  CONSTRAINT fk_rental_order_pickup_store
    FOREIGN KEY (pickup_store_id) REFERENCES rental_store (store_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_rental_order_return_store
    FOREIGN KEY (return_store_id) REFERENCES rental_store (store_id)
    ON UPDATE CASCADE
    ON DELETE SET NULL,
  CONSTRAINT fk_rental_order_created_staff
    FOREIGN KEY (created_staff_id) REFERENCES store_staff (staff_id)
    ON UPDATE CASCADE
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁订单表';

CREATE TABLE rental_order_settlement (
  settlement_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '结算ID',
  order_id BIGINT NOT NULL COMMENT '订单ID',
  settle_store_id BIGINT NOT NULL COMMENT '结算门店ID',
  settle_staff_id BIGINT NOT NULL COMMENT '结算员工ID',
  base_rent_amount DECIMAL(10, 2) NOT NULL COMMENT '基础租金',
  overtime_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '超时费用',
  maintenance_compensation DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '维修赔偿',
  loss_compensation DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '车辆遗失赔偿',
  discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  payable_total DECIMAL(10, 2) NOT NULL COMMENT '最终应收金额',
  deposit_paid DECIMAL(10, 2) NOT NULL COMMENT '已收押金',
  refund_deposit_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '应退押金',
  settlement_note VARCHAR(255) DEFAULT NULL COMMENT '结算说明',
  settled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '结算时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_rental_order_settlement_order_id (order_id),
  KEY idx_rental_order_settlement_store (settle_store_id),
  KEY idx_rental_order_settlement_staff (settle_staff_id),
  CONSTRAINT fk_rental_order_settlement_order
    FOREIGN KEY (order_id) REFERENCES rental_order (order_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_rental_order_settlement_store
    FOREIGN KEY (settle_store_id) REFERENCES rental_store (store_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_rental_order_settlement_staff
    FOREIGN KEY (settle_staff_id) REFERENCES store_staff (staff_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁结算表';

CREATE TABLE maintenance_record (
  maintenance_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '维修记录ID',
  bike_id BIGINT NOT NULL COMMENT '车辆实例ID',
  store_id BIGINT NOT NULL COMMENT '处理门店ID',
  reported_by_staff_id BIGINT DEFAULT NULL COMMENT '报修员工ID',
  assigned_staff_id BIGINT DEFAULT NULL COMMENT '维修负责人ID',
  maintenance_type VARCHAR(30) NOT NULL DEFAULT 'repair' COMMENT '类型：repair维修，routine保养，inspection巡检',
  fault_description VARCHAR(255) NOT NULL COMMENT '故障描述',
  maintenance_content TEXT COMMENT '维修内容',
  maintenance_cost DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '维修费用',
  start_time DATETIME DEFAULT NULL COMMENT '开始时间',
  end_time DATETIME DEFAULT NULL COMMENT '完成时间',
  maintenance_status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1待处理，2维修中，3已完成',
  next_maintenance_date DATE DEFAULT NULL COMMENT '建议下次维护日期',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_maintenance_record_bike (bike_id),
  KEY idx_maintenance_record_store_status (store_id, maintenance_status),
  KEY idx_maintenance_record_reported_staff (reported_by_staff_id),
  KEY idx_maintenance_record_assigned_staff (assigned_staff_id),
  CONSTRAINT fk_maintenance_record_bike
    FOREIGN KEY (bike_id) REFERENCES bike_inventory (bike_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_maintenance_record_store
    FOREIGN KEY (store_id) REFERENCES rental_store (store_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_maintenance_record_reported_staff
    FOREIGN KEY (reported_by_staff_id) REFERENCES store_staff (staff_id)
    ON UPDATE CASCADE
    ON DELETE SET NULL,
  CONSTRAINT fk_maintenance_record_assigned_staff
    FOREIGN KEY (assigned_staff_id) REFERENCES store_staff (staff_id)
    ON UPDATE CASCADE
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆维修记录表';

CREATE TABLE user_favorite (
  favorite_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  model_id BIGINT NOT NULL COMMENT '收藏的车辆配置ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  UNIQUE KEY uk_user_favorite_user_model (user_id, model_id),
  KEY idx_user_favorite_model (model_id),
  CONSTRAINT fk_user_favorite_user
    FOREIGN KEY (user_id) REFERENCES app_user (user_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_user_favorite_model
    FOREIGN KEY (model_id) REFERENCES bike_model (model_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

DROP VIEW IF EXISTS vw_financial_overview;

CREATE VIEW vw_financial_overview AS
SELECT
  COUNT(DISTINCT o.order_id) AS total_orders,
  COALESCE(SUM(CASE WHEN o.order_status IN (3, 4, 5) THEN o.rent_amount ELSE 0 END), 0) AS total_rent_income,
  COALESCE(SUM(CASE WHEN o.payment_status IN (1, 2, 3) THEN o.deposit_amount ELSE 0 END), 0) AS total_deposit_collected,
  COALESCE(SUM(COALESCE(s.refund_deposit_amount, 0)), 0) AS total_deposit_refunded,
  COALESCE(SUM(COALESCE(s.overtime_amount, 0) + COALESCE(s.maintenance_compensation, 0) + COALESCE(s.loss_compensation, 0) - COALESCE(s.discount_amount, 0)), 0) AS total_extra_income,
  COALESCE((SELECT SUM(m.maintenance_cost) FROM maintenance_record m WHERE m.maintenance_status = 3), 0) AS total_maintenance_cost,
  COALESCE(SUM(
    CASE
      WHEN o.order_status IN (3, 4, 5) THEN
        o.rent_amount
        + COALESCE(s.overtime_amount, 0)
        + COALESCE(s.maintenance_compensation, 0)
        + COALESCE(s.loss_compensation, 0)
        - COALESCE(s.discount_amount, 0)
      ELSE 0
    END
  ), 0)
  - COALESCE((SELECT SUM(m.maintenance_cost) FROM maintenance_record m WHERE m.maintenance_status = 3), 0) AS estimated_net_income
FROM rental_order o
LEFT JOIN rental_order_settlement s ON o.order_id = s.order_id
WHERE o.order_status <> 6;
