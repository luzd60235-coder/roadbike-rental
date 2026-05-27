import {
  Bicycle,
  DataAnalysis,
  Document,
  Grid,
  House,
  LocationInformation,
  OfficeBuilding,
  PriceTag,
  Star,
  Tickets,
  Tools,
  UserFilled,
  WalletFilled
} from '@element-plus/icons-vue'

export const roleMetaMap = {
  admin: {
    short: '管',
    title: '后台管理中心',
    description: '统一查看车辆、车型、订单、价格和门店运营数据。'
  },
  user: {
    short: '骑',
    title: '用户骑行中心',
    description: '浏览车辆、筛选车型、查看订单与收藏。'
  },
  staff: {
    short: '店',
    title: '门店工作台',
    description: '处理取还车、门店结算、维修工单与个人值班信息。'
  }
}

export const roleMenus = {
  admin: [
    { path: '/admin/vehicles', title: '车辆管理', icon: Bicycle },
    { path: '/admin/models', title: '车型管理', icon: Grid },
    { path: '/admin/orders', title: '订单管理', icon: Tickets },
    { path: '/admin/pricing', title: '价格管理', icon: PriceTag },
    { path: '/admin/statistics', title: '数据统计', icon: DataAnalysis },
    { path: '/admin/stores', title: '门店管理', icon: OfficeBuilding },
    { path: '/admin/staff-members', title: '门店员工管理', icon: UserFilled },
    { path: '/admin/profile', title: '个人中心', icon: Document }
  ],
  user: [
    { path: '/user/home', title: '首页', icon: House },
    { path: '/user/bikes', title: '车辆列表', icon: Grid },
    { path: '/user/orders', title: '我的订单', icon: Document },
    { path: '/user/favorites', title: '我的收藏', icon: Star },
    { path: '/user/stores', title: '门店查询', icon: LocationInformation },
    { path: '/user/profile', title: '个人中心', icon: UserFilled }
  ],
  staff: [
    { path: '/staff/trades', title: '交易管理', icon: Tickets },
    { path: '/staff/settlements', title: '结算管理', icon: WalletFilled },
    { path: '/staff/maintenance', title: '维修管理', icon: Tools },
    { path: '/staff/profile', title: '个人中心', icon: UserFilled }
  ]
}
// 导航栏中的固定选项
