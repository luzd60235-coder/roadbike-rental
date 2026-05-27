export const mockData = {
  users: [
    {
      role: 'admin',
      username: 'admin01',
      password: '123456',
      name: '系统管理员',
      avatar: '管',
      motto: '保持车辆周转与服务稳定'
    },
    {
      role: 'user',
      username: 'rider01',
      password: '123456',
      name: '周沐骑',
      avatar: '骑',
      motto: '这个周末继续刷城市环线'
    },
    {
      role: 'staff',
      username: 'staff01',
      password: '123456',
      name: '刘晨',
      avatar: '店',
      storeName: '虹桥旗舰店',
      motto: '今天还有 4 台车辆待交付'
    }
  ],
  stores: [
    {
      id: 1,
      name: '虹桥旗舰店',
      city: '上海',
      district: '闵行区',
      address: '申虹路 1588 号骑行驿站',
      phone: '021-6688-2001',
      hours: '08:00 - 21:30',
      capacity: 42,
      available: 16,
      utilization: 91,
      rating: 4.9,
      services: ['异店还车', '头盔护具', '快修保养']
    },
    {
      id: 2,
      name: '陆家嘴体验店',
      city: '上海',
      district: '浦东新区',
      address: '世纪大道 1888 号滨江骑行中心',
      phone: '021-6688-2002',
      hours: '09:00 - 22:00',
      capacity: 36,
      available: 11,
      utilization: 87,
      rating: 4.8,
      services: ['夜骑补给', '团骑集合', '路线咨询']
    },
    {
      id: 3,
      name: '金鸡湖骑行站',
      city: '苏州',
      district: '工业园区',
      address: '月廊街 66 号湖畔运动空间',
      phone: '0512-8800-3201',
      hours: '08:30 - 21:00',
      capacity: 28,
      available: 9,
      utilization: 83,
      rating: 4.7,
      services: ['湖区路线包', '摄影跟拍', '能量补给']
    },
    {
      id: 4,
      name: '西湖轻骑门店',
      city: '杭州',
      district: '西湖区',
      address: '曙光路 120 号运动生活馆',
      phone: '0571-6123-1108',
      hours: '07:30 - 20:30',
      capacity: 32,
      available: 14,
      utilization: 78,
      rating: 4.8,
      services: ['新手陪骑', '寄存服务', '车队活动']
    },
    {
      id: 5,
      name: '滨江骑行仓',
      city: '杭州',
      district: '滨江区',
      address: '闻涛路 218 号城市骑行补给仓',
      phone: '0571-6123-1120',
      hours: '09:00 - 21:30',
      capacity: 24,
      available: 7,
      utilization: 89,
      rating: 4.6,
      services: ['赛事支援', '码表租借', '夜骑灯具']
    }
  ],
  bikes: [
    {
      id: 101,
      code: 'RB-2026-001',
      brand: 'Specialized',
      model: 'Tarmac SL8 Comp',
      bikeName: 'Specialized Tarmac SL8 Comp',
      type: '竞速型',
      frame: '碳纤维',
      gear: 'Shimano 105 Di2 24 速',
      brake: '液压碟刹',
      wheel: '700C 碳刀轮组',
      weight: '8.1kg',
      size: 'M',
      height: '170 - 180cm',
      color: '晨雾灰',
      status: '可租赁',
      statusType: 'success',
      price: 268,
      deposit: 2000,
      mileage: 820,
      rating: 4.9,
      stock: 3,
      storeId: 1,
      storeName: '虹桥旗舰店',
      city: '上海',
      tags: ['电子变速', '轻量竞速', '热门推荐'],
      summary: '适合城市高频训练与周末长距离刷圈，兼顾爆发力与舒适性。'
    },
    {
      id: 102,
      code: 'RB-2026-014',
      brand: 'TREK',
      model: 'Madone SL 6',
      bikeName: 'TREK Madone SL 6',
      type: '空气动力型',
      frame: 'OCLV 碳纤维',
      gear: 'Shimano 105 24 速',
      brake: '液压碟刹',
      wheel: 'Bontrager Aeolus',
      weight: '8.5kg',
      size: 'L',
      height: '178 - 188cm',
      color: '深海蓝',
      status: '已预定',
      statusType: 'warning',
      price: 298,
      deposit: 2200,
      mileage: 1160,
      rating: 4.8,
      stock: 1,
      storeId: 2,
      storeName: '陆家嘴体验店',
      city: '上海',
      tags: ['长途稳定', '城市夜骑', '旗舰车型'],
      summary: '空气动力学车架带来更高巡航效率，适合强度训练和都市远骑。'
    },
    {
      id: 103,
      code: 'RB-2026-021',
      brand: 'GIANT',
      model: 'TCR Advanced 1',
      bikeName: 'GIANT TCR Advanced 1',
      type: '综合型',
      frame: 'Advanced 碳纤维',
      gear: 'Shimano 105 24 速',
      brake: '液压碟刹',
      wheel: '700C 铝合金轮组',
      weight: '8.7kg',
      size: 'S',
      height: '160 - 170cm',
      color: '曜石黑',
      status: '租赁中',
      statusType: 'info',
      price: 218,
      deposit: 1800,
      mileage: 1530,
      rating: 4.7,
      stock: 0,
      storeId: 3,
      storeName: '金鸡湖骑行站',
      city: '苏州',
      tags: ['入门竞速', '综合训练', '性价比高'],
      summary: '兼顾操控与舒适的综合型碳架公路车，适合新人进阶和轻训练。'
    },
    {
      id: 104,
      code: 'RB-2026-032',
      brand: 'Cannondale',
      model: 'SuperSix EVO 3',
      bikeName: 'Cannondale SuperSix EVO 3',
      type: '爬坡型',
      frame: 'BallisTec Carbon',
      gear: 'SRAM Rival AXS 24 速',
      brake: '液压碟刹',
      wheel: 'HollowGram R-S',
      weight: '7.9kg',
      size: 'M',
      height: '168 - 178cm',
      color: '月光白',
      status: '可租赁',
      statusType: 'success',
      price: 288,
      deposit: 2400,
      mileage: 620,
      rating: 4.9,
      stock: 2,
      storeId: 4,
      storeName: '西湖轻骑门店',
      city: '杭州',
      tags: ['轻量爬坡', '电变系统', '高颜值'],
      summary: '车重控制出色，爬坡段响应灵敏，适合环湖与起伏路线。'
    },
    {
      id: 105,
      code: 'RB-2026-045',
      brand: 'Merida',
      model: 'Scultura 6000',
      bikeName: 'Merida Scultura 6000',
      type: '耐力型',
      frame: 'CF3 碳纤维',
      gear: 'Shimano Ultegra 24 速',
      brake: '液压碟刹',
      wheel: 'Vision Team 35',
      weight: '8.4kg',
      size: 'M',
      height: '170 - 180cm',
      color: '橄榄绿',
      status: '维修中',
      statusType: 'danger',
      price: 238,
      deposit: 1800,
      mileage: 1930,
      rating: 4.6,
      stock: 0,
      storeId: 1,
      storeName: '虹桥旗舰店',
      city: '上海',
      tags: ['耐力舒适', '长骑友好', '异店热门'],
      summary: '更偏向耐力骑乘姿态，适合长距离通勤与周末城市环线体验。'
    },
    {
      id: 106,
      code: 'RB-2026-051',
      brand: 'BMC',
      model: 'Teammachine SLR',
      bikeName: 'BMC Teammachine SLR',
      type: '高性能型',
      frame: 'Premium Carbon',
      gear: 'Shimano Ultegra Di2',
      brake: '液压碟刹',
      wheel: 'CRD 351',
      weight: '8.0kg',
      size: 'L',
      height: '180 - 190cm',
      color: '熔岩红',
      status: '可租赁',
      statusType: 'success',
      price: 318,
      deposit: 2600,
      mileage: 710,
      rating: 5,
      stock: 2,
      storeId: 5,
      storeName: '滨江骑行仓',
      city: '杭州',
      tags: ['赛事同款', '旗舰性能', '稀缺车源'],
      summary: '适合追求高刚性与赛道反馈的进阶用户，周末活动中的高关注车型。'
    }
  ],
  orders: [
    {
      id: 1,
      orderNo: 'DD20260411001',
      bikeId: 101,
      bikeName: 'Specialized Tarmac SL8 Comp',
      userName: '周沐骑',
      pickupStore: '虹桥旗舰店',
      returnStore: '陆家嘴体验店',
      startTime: '2026-04-12 09:00',
      endTime: '2026-04-13 18:00',
      rentDays: 2,
      amount: 536,
      deposit: 2000,
      status: '待取车',
      statusType: 'warning',
      paymentStatus: '已支付',
      source: 'App 直租'
    },
    {
      id: 2,
      orderNo: 'DD20260410018',
      bikeId: 102,
      bikeName: 'TREK Madone SL 6',
      userName: '林野',
      pickupStore: '陆家嘴体验店',
      returnStore: '陆家嘴体验店',
      startTime: '2026-04-10 08:00',
      endTime: '2026-04-12 17:00',
      rentDays: 3,
      amount: 894,
      deposit: 2200,
      status: '租赁中',
      statusType: 'primary',
      paymentStatus: '已支付',
      source: '门店开单'
    },
    {
      id: 3,
      orderNo: 'DD20260409012',
      bikeId: 104,
      bikeName: 'Cannondale SuperSix EVO 3',
      userName: '周沐骑',
      pickupStore: '西湖轻骑门店',
      returnStore: '西湖轻骑门店',
      startTime: '2026-04-09 07:30',
      endTime: '2026-04-09 21:30',
      rentDays: 1,
      amount: 288,
      deposit: 2400,
      status: '已完成',
      statusType: 'success',
      paymentStatus: '已完成退款',
      source: '活动套餐'
    },
    {
      id: 4,
      orderNo: 'DD20260408021',
      bikeId: 103,
      bikeName: 'GIANT TCR Advanced 1',
      userName: '叶白',
      pickupStore: '金鸡湖骑行站',
      returnStore: '金鸡湖骑行站',
      startTime: '2026-04-08 09:00',
      endTime: '2026-04-09 15:00',
      rentDays: 2,
      amount: 436,
      deposit: 1800,
      status: '待结算',
      statusType: 'danger',
      paymentStatus: '部分退款',
      source: '小程序'
    },
    {
      id: 5,
      orderNo: 'DD20260407009',
      bikeId: 106,
      bikeName: 'BMC Teammachine SLR',
      userName: '周沐骑',
      pickupStore: '滨江骑行仓',
      returnStore: '滨江骑行仓',
      startTime: '2026-04-07 06:30',
      endTime: '2026-04-07 20:00',
      rentDays: 1,
      amount: 318,
      deposit: 2600,
      status: '已完成',
      statusType: 'success',
      paymentStatus: '已完成退款',
      source: '门店快租'
    },
    {
      id: 6,
      orderNo: 'DD20260406003',
      bikeId: 105,
      bikeName: 'Merida Scultura 6000',
      userName: '许光',
      pickupStore: '虹桥旗舰店',
      returnStore: '虹桥旗舰店',
      startTime: '2026-04-06 10:00',
      endTime: '2026-04-07 10:00',
      rentDays: 1,
      amount: 238,
      deposit: 1800,
      status: '已取消',
      statusType: 'info',
      paymentStatus: '已退款',
      source: 'App 直租'
    }
  ],
  pricing: [
    {
      id: 1,
      model: 'Specialized Tarmac SL8 Comp',
      packageName: '都市竞速档',
      dailyRent: 268,
      weekendRent: 298,
      deposit: 2000,
      overtime: 48,
      season: '春季热租期',
      updatedAt: '2026-04-08 10:00'
    },
    {
      id: 2,
      model: 'TREK Madone SL 6',
      packageName: '旗舰巡航档',
      dailyRent: 298,
      weekendRent: 328,
      deposit: 2200,
      overtime: 58,
      season: '长线体验包',
      updatedAt: '2026-04-08 10:00'
    },
    {
      id: 3,
      model: 'Cannondale SuperSix EVO 3',
      packageName: '山路轻量档',
      dailyRent: 288,
      weekendRent: 318,
      deposit: 2400,
      overtime: 56,
      season: '西湖环线专享',
      updatedAt: '2026-04-09 09:40'
    },
    {
      id: 4,
      model: 'BMC Teammachine SLR',
      packageName: '旗舰性能档',
      dailyRent: 318,
      weekendRent: 358,
      deposit: 2600,
      overtime: 68,
      season: '赛事训练周',
      updatedAt: '2026-04-10 15:30'
    }
  ],
  favorites: [
    {
      id: 101,
      addedAt: '2026-04-09 18:20'
    },
    {
      id: 104,
      addedAt: '2026-04-08 21:12'
    },
    {
      id: 106,
      addedAt: '2026-04-07 20:45'
    }
  ],
  settlements: [
    {
      id: 1,
      cycle: '2026-04-01 ~ 2026-04-07',
      storeName: '虹桥旗舰店',
      orderCount: 42,
      rentIncome: 18680,
      refundAmount: 1200,
      maintenanceCost: 860,
      finalIncome: 16620,
      status: '待复核',
      statusType: 'warning'
    },
    {
      id: 2,
      cycle: '2026-04-01 ~ 2026-04-07',
      storeName: '陆家嘴体验店',
      orderCount: 36,
      rentIncome: 17240,
      refundAmount: 980,
      maintenanceCost: 720,
      finalIncome: 15540,
      status: '已出账',
      statusType: 'success'
    },
    {
      id: 3,
      cycle: '2026-04-08 ~ 2026-04-10',
      storeName: '西湖轻骑门店',
      orderCount: 27,
      rentIncome: 12980,
      refundAmount: 600,
      maintenanceCost: 540,
      finalIncome: 11840,
      status: '结算中',
      statusType: 'primary'
    },
    {
      id: 4,
      cycle: '2026-04-08 ~ 2026-04-10',
      storeName: '滨江骑行仓',
      orderCount: 19,
      rentIncome: 10820,
      refundAmount: 0,
      maintenanceCost: 410,
      finalIncome: 10410,
      status: '已确认',
      statusType: 'success'
    }
  ],
  maintenance: [
    {
      id: 1,
      bikeCode: 'RB-2026-045',
      bikeName: 'Merida Scultura 6000',
      storeName: '虹桥旗舰店',
      issue: '后拨异响，链条磨损明显',
      level: '中等',
      engineer: '沈涛',
      cost: 180,
      reportTime: '2026-04-10 15:20',
      status: '维修中',
      statusType: 'danger'
    },
    {
      id: 2,
      bikeCode: 'RB-2026-021',
      bikeName: 'GIANT TCR Advanced 1',
      storeName: '金鸡湖骑行站',
      issue: '前轮偏摆，需要重新编轮',
      level: '紧急',
      engineer: '周恺',
      cost: 320,
      reportTime: '2026-04-09 18:30',
      status: '待派工',
      statusType: 'warning'
    },
    {
      id: 3,
      bikeCode: 'RB-2026-014',
      bikeName: 'TREK Madone SL 6',
      storeName: '陆家嘴体验店',
      issue: '刹车片磨耗接近阈值',
      level: '轻度',
      engineer: '林栩',
      cost: 90,
      reportTime: '2026-04-09 10:10',
      status: '已完成',
      statusType: 'success'
    },
    {
      id: 4,
      bikeCode: 'RB-2026-051',
      bikeName: 'BMC Teammachine SLR',
      storeName: '滨江骑行仓',
      issue: '变速调校偏差，需校正电变',
      level: '中等',
      engineer: '杨纪',
      cost: 140,
      reportTime: '2026-04-08 17:40',
      status: '复检中',
      statusType: 'primary'
    }
  ],
  statistics: {
    overview: {
      revenue: 248600,
      orders: 318,
      activeBikes: 126,
      stores: 8,
      satisfaction: '98.6%'
    },
    storePerformance: [
      {
        name: '虹桥旗舰店',
        revenue: 68300,
        utilization: 92,
        orders: 86
      },
      {
        name: '陆家嘴体验店',
        revenue: 57200,
        utilization: 88,
        orders: 71
      },
      {
        name: '西湖轻骑门店',
        revenue: 49800,
        utilization: 83,
        orders: 63
      },
      {
        name: '金鸡湖骑行站',
        revenue: 42100,
        utilization: 79,
        orders: 52
      }
    ],
    hotModels: [
      {
        model: 'Specialized Tarmac SL8 Comp',
        orders: 78,
        growth: '+18%'
      },
      {
        model: 'TREK Madone SL 6',
        orders: 62,
        growth: '+12%'
      },
      {
        model: 'Cannondale SuperSix EVO 3',
        orders: 58,
        growth: '+15%'
      },
      {
        model: 'BMC Teammachine SLR',
        orders: 47,
        growth: '+21%'
      }
    ],
    incomeTrend: [
      {
        week: '第1周',
        amount: 36
      },
      {
        week: '第2周',
        amount: 41
      },
      {
        week: '第3周',
        amount: 46
      },
      {
        week: '第4周',
        amount: 53
      },
      {
        week: '本周',
        amount: 62
      }
    ],
    channels: [
      {
        name: 'App 下单',
        percent: 56
      },
      {
        name: '门店快租',
        percent: 24
      },
      {
        name: '小程序',
        percent: 20
      }
    ]
  }
}

export function getBikeById(id) {
  return mockData.bikes.find((item) => String(item.id) === String(id))
}

export function getStoreById(id) {
  return mockData.stores.find((item) => String(item.id) === String(id))
}
