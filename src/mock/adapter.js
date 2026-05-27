import { getBikeById, mockData } from './data'

function wait(duration = 180) {
  return new Promise((resolve) => {
    window.setTimeout(resolve, duration)
  })
}

function parseBody(raw) {
  if (!raw) {
    return {}
  }
  if (typeof raw === 'string') {
    return JSON.parse(raw)
  }
  return raw
}

function parseQuery(url = '') {
  if (!url.includes('?')) {
    return {}
  }
  const searchParams = new URLSearchParams(url.split('?')[1])
  return Object.fromEntries(searchParams.entries())
}

function normalizePath(url = '') {
  return url.split('?')[0].replace(/^\/api/, '')
}

function buildResponse(config, data, code = 0, message = 'success', status = 200) {
  return {
    data: {
      code,
      message,
      data
    },
    status,
    statusText: status === 200 ? 'OK' : 'Error',
    headers: {},
    config,
    request: {
      mocked: true
    }
  }
}

function requireUser(payload) {
  const user = mockData.users.find(
    (item) =>
      item.role === payload.role &&
      item.username === payload.username &&
      item.password === payload.password
  )

  if (!user) {
    throw new Error('账号、密码或登录角色不正确')
  }

  return {
    token: `mock-token-${user.role}-${Date.now()}`,
    user: {
      role: user.role,
      name: user.name,
      avatar: user.avatar,
      storeName: user.storeName || '',
      motto: user.motto
    }
  }
}

const tradeList = mockData.orders.map((item, index) => ({
  id: item.id,
  orderNo: item.orderNo,
  bikeName: item.bikeName,
  bikeCode: mockData.bikes.find((bike) => bike.id === item.bikeId)?.code || '-',
  customer: item.userName,
  pickupStore: item.pickupStore,
  returnStore: item.returnStore,
  pickupTime: item.startTime,
  returnTime: item.endTime,
  amount: item.amount + item.deposit,
  operator: ['刘晨', '沈涛', '方鸣', '陶雅'][index % 4],
  status: item.status,
  statusType: item.statusType
}))

export async function mockAdapter(config) {
  await wait()

  const method = (config.method || 'get').toLowerCase()
  const path = normalizePath(config.url)
  const payload = parseBody(config.data)
  const query = {
    ...parseQuery(config.url),
    ...(config.params || {})
  }

  try {
    if (method === 'post' && path === '/auth/login') {
      return buildResponse(config, requireUser(payload))
    }

    if (method === 'get' && path === '/admin/vehicles') {
      return buildResponse(config, {
        list: mockData.bikes,
        summary: {
          totalBikes: mockData.bikes.length,
          available: mockData.bikes.filter((item) => item.status === '可租赁').length,
          renting: mockData.bikes.filter((item) => item.status === '租赁中').length,
          maintenance: mockData.bikes.filter((item) => item.status === '维修中').length
        }
      })
    }

    if (method === 'get' && path === '/admin/orders') {
      return buildResponse(config, {
        list: mockData.orders,
        summary: {
          todayOrders: 26,
          pendingPickup: 8,
          runningOrders: 19,
          completedOrders: 214
        }
      })
    }

    if (method === 'get' && path === '/admin/pricing') {
      return buildResponse(config, {
        list: mockData.pricing,
        highlights: {
          averageDailyRent: 293,
          highestModel: 'BMC Teammachine SLR',
          depositRange: '1800 - 2600'
        }
      })
    }

    if (method === 'get' && path === '/admin/statistics') {
      return buildResponse(config, mockData.statistics)
    }

    if (method === 'get' && path === '/admin/stores') {
      return buildResponse(config, {
        list: mockData.stores,
        summary: {
          totalStores: mockData.stores.length,
          averageUtilization: '85.6%',
          activeCities: 3,
          serviceCoverage: '98%'
        }
      })
    }

    if (method === 'get' && path === '/user/home') {
      return buildResponse(config, {
        hero: {
          title: '都市圈轻骑计划',
          subtitle: '联动上海、苏州、杭州热门门店，支持异店还车与活动线路推荐。'
        },
        quickStats: [
          { label: '可租车型', value: '26+' },
          { label: '覆盖门店', value: '8 家' },
          { label: '异店还车', value: '支持' },
          { label: '本月骑行里程', value: '348 km' }
        ],
        recommended: mockData.bikes.filter((item) => ['可租赁', '已预定'].includes(item.status)).slice(0, 3),
        stores: mockData.stores.slice(0, 3)
      })
    }

    if (method === 'get' && path === '/user/bikes') {
      const city = query.city || ''
      const keyword = query.keyword || ''
      const list = mockData.bikes.filter((item) => {
        const matchCity = !city || item.city === city
        const loweredKeyword = keyword.toLowerCase()
        const matchKeyword =
          !keyword ||
          item.bikeName.toLowerCase().includes(loweredKeyword) ||
          item.brand.toLowerCase().includes(loweredKeyword)
        return matchCity && matchKeyword
      })
      return buildResponse(config, { list })
    }

    if (method === 'get' && /^\/user\/bikes\/\d+$/.test(path)) {
      const bikeId = path.split('/').pop()
      const bike = getBikeById(bikeId)
      if (!bike) {
        throw new Error('未找到对应车辆')
      }
      return buildResponse(config, {
        bike,
        nearbyStores: mockData.stores.filter((item) => item.city === bike.city).slice(0, 3),
        similar: mockData.bikes.filter((item) => item.id !== bike.id).slice(0, 3)
      })
    }

    if (method === 'get' && path === '/user/orders') {
      return buildResponse(config, {
        list: mockData.orders.filter((item) => item.userName === '周沐骑')
      })
    }

    if (method === 'get' && path === '/user/favorites') {
      return buildResponse(config, {
        list: mockData.favorites.map((favorite) => ({
          ...getBikeById(favorite.id),
          addedAt: favorite.addedAt
        }))
      })
    }

    if (method === 'get' && path === '/user/stores') {
      return buildResponse(config, {
        list: mockData.stores
      })
    }

    if (method === 'get' && path === '/staff/trades') {
      return buildResponse(config, {
        list: tradeList,
        summary: {
          pendingPickup: 4,
          waitingReturn: 6,
          todayTurnover: 12860,
          currentShift: '早班 08:00 - 16:00'
        }
      })
    }

    if (method === 'get' && path === '/staff/settlements') {
      return buildResponse(config, {
        list: mockData.settlements,
        overview: {
          currentIncome: 54410,
          refundAmount: 2780,
          pendingReview: 2,
          maintenanceCost: 2530
        }
      })
    }

    if (method === 'get' && path === '/staff/maintenance') {
      return buildResponse(config, {
        list: mockData.maintenance,
        overview: {
          pendingJobs: 3,
          inProgress: 2,
          completedToday: 5,
          sparePartsStock: '充足'
        }
      })
    }

    return buildResponse(config, null, 1, `未匹配模拟接口：${method.toUpperCase()} ${path}`, 404)
  } catch (error) {
    return buildResponse(config, null, 1, error.message || '模拟请求异常', 500)
  }
}
