# Road Bike Rental

一个面向课程设计、毕设演示和全栈练习的公路车租赁管理系统，包含：

- 管理员后台
- 用户端租车流程
- 门店员工操作台
- Spring Boot 后端接口
- Vue 3 前端管理与展示界面

## 项目亮点

- 三端角色完整：管理员、普通用户、门店员工
- 业务链路完整：登录、门店管理、车辆管理、价格管理、订单管理、维修管理、结算管理
- 前后端分离：前端使用 Vite，后端使用 Spring Boot
- 支持图片上传：门店封面、车型图片
- 附带数据库建表与种子数据脚本，方便快速演示

## 技术栈

### Frontend

- Vue 3
- Vite
- Element Plus
- Vue Router
- Axios

### Backend

- Spring Boot 2.7
- MyBatis-Plus
- MySQL 8
- JWT

## 功能模块

### 管理员端

- 车辆管理
- 车型管理
- 门店管理
- 门店员工管理
- 价格管理
- 订单管理
- 统计分析
- 管理员个人中心

### 用户端

- 首页推荐
- 门店查询
- 车辆列表
- 车辆详情
- 在线下单
- 我的订单
- 我的收藏
- 个人中心

### 门店员工端

- 交易管理
- 维修管理
- 结算管理
- 员工个人中心

## 目录结构

```text
roadbike-rental/
├─ src/                                   # 前端源码
│  ├─ api/
│  ├─ views/
│  ├─ router/
│  └─ utils/
├─ src/main/java/com/example/roadbikerental/
│  ├─ controller/                         # 后端接口
│  ├─ service/
│  ├─ mapper/
│  ├─ entity/
│  └─ common/
├─ src/main/resources/
│  └─ application.yml                     # 后端配置
├─ road_bike_rental_schema.sql            # 建表脚本
├─ road_bike_rental_seed.sql              # 初始化数据
├─ fix_store.sql                          # 门店图片字段修复脚本
├─ fix_bike.sql                           # 车辆/车型相关修复脚本
└─ fix_database.sql                       # 数据库补丁脚本
```

## 快速开始

### 1. 准备环境

- Node.js 18+
- JDK 8 或 17
- Maven 3.8+
- MySQL 8.x

### 2. 初始化数据库

先创建数据库，例如：

```sql
CREATE DATABASE road_bike_rental DEFAULT CHARACTER SET utf8mb4;
```

然后依次执行：

```text
road_bike_rental_schema.sql
road_bike_rental_seed.sql
```

如果你的本地数据库结构比当前代码旧，再按需要执行：

- `fix_store.sql`
- `fix_bike.sql`
- `fix_database.sql`

### 3. 启动后端

修改 [`src/main/resources/application.yml`](C:\Users\luzdengdong\Documents\roadbike-rental\src\main\resources\application.yml) 中的数据库连接：

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

然后启动：

```bash
mvn spring-boot:run
```

默认后端地址：

```text
http://127.0.0.1:8080
```

### 4. 启动前端

安装依赖：

```bash
npm install
```

复制环境变量模板并按需调整：

```bash
cp .env.example .env.development
```

启动前端：

```bash
npm run dev
```

默认前端地址：

```text
http://127.0.0.1:5173
```

## 默认演示账号

- 管理员：`admin01 / 123456`
- 用户：`rider01 / 123456`
- 门店员工：`staff01 / 123456`

## 接口角色划分

### 管理员接口

- `/api/admin/auth/*`
- `/api/admin/stores/*`
- `/api/admin/bikes/*`
- `/api/admin/bike-models/*`
- `/api/admin/pricings/*`
- `/api/admin/orders/*`
- `/api/admin/statistics/*`
- `/api/admin/uploads/*`

### 用户接口

- `/api/user/auth/*`
- `/api/user/stores/*`
- `/api/user/bikes/*`
- `/api/user/orders/*`
- `/api/user/favorites/*`
- `/api/user/profile/*`

### 员工接口

- `/api/staff/auth/*`
- `/api/staff/trades/*`
- `/api/staff/maintenance/*`
- `/api/staff/settlements/*`
- `/api/staff/profile/*`

## 当前适合展示的内容

- 多角色后台系统设计
- Vue 3 + Spring Boot 前后端分离实践
- 基于 JWT 的登录鉴权
- 车辆租赁业务建模
- 图片上传与静态资源映射

## 发布到 GitHub 前建议

- 补一组页面截图放进 `README`
- 在仓库 About 区填写中英文描述
- 添加 Topics，例如：
  - `vue3`
  - `spring-boot`
  - `mybatis-plus`
  - `mysql`
  - `jwt`
  - `rental-system`
  - `graduation-project`

## License

当前仓库未附带开源许可证。如果你准备公开发布，建议补一个常见许可证，例如 MIT。
