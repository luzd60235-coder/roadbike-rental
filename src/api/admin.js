import request from '@/utils/request'
import {
  createLookupMap,
  groupTotals,
  mapBikeRecord,
  mapOrderRecord,
  mapPricingRecord,
  mapStoreEntity,
  pickRecords,
  toNumber,
  todayPrefix
} from '@/utils/backend'

const PAGE_PARAMS = {
  current: 1,
  size: 100
}

async function fetchAdminBikeModels(params = {}) {
  const page = await request({
    url: '/admin/bike-models/page',
    method: 'get',
    params: {
      ...PAGE_PARAMS,
      ...params
    }
  })

  return page
}

async function fetchAdminBikes(params = {}) {
  const page = await request({
    url: '/admin/bikes/page',
    method: 'get',
    params: {
      ...PAGE_PARAMS,
      ...params
    }
  })

  return page
}

async function fetchAdminPricing(params = {}) {
  const page = await request({
    url: '/admin/pricings/page',
    method: 'get',
    params: {
      ...PAGE_PARAMS,
      ...params
    }
  })

  return page
}

async function fetchAdminStores(params = {}) {
  const page = await request({
    url: '/admin/stores/page',
    method: 'get',
    params: {
      ...PAGE_PARAMS,
      ...params
    }
  })

  return page
}

function buildStoreServices(store) {
  const serviceMap = {
    上海: ['异店还车', '夜骑支援', '现场快修'],
    杭州: ['环湖骑行', '新手陪骑', '头盔租借'],
    苏州: ['园区通勤', '湖畔慢骑', '车辆调度']
  }

  return serviceMap[store?.city] || ['标准租赁', '车辆保养']
}

function buildAdminOverview(statistics) {
  const revenue =
    toNumber(statistics?.totalRentIncome, 0) + toNumber(statistics?.totalExtraIncome, 0)

  return {
    revenue,
    orders: toNumber(statistics?.totalOrders, 0),
    activeBikes: toNumber(statistics?.totalBikes, 0),
    stores: toNumber(statistics?.totalStores, 0),
    satisfaction: statistics?.totalOrders ? '100%' : '0%'
  }
}

export async function getAdminVehicles() {
  const [bikePage, modelPage, pricingPage, storePage] = await Promise.all([
    fetchAdminBikes(),
    fetchAdminBikeModels(),
    fetchAdminPricing(),
    fetchAdminStores()
  ])

  const modelMap = createLookupMap(pickRecords(modelPage), (item) => item.modelId)
  const pricingMap = createLookupMap(pickRecords(pricingPage), (item) => item.modelId)
  const storeMap = createLookupMap(
    pickRecords(storePage).map((item) => mapStoreEntity(item)),
    (item) => item.id
  )
  const list = pickRecords(bikePage).map((item) => {
    const model = modelMap.get(item.modelId) || {}
    const pricing = pricingMap.get(item.modelId)

    const bike = mapBikeRecord(
      {
        ...item,
        ...model
      },
      {
        storeMap,
        dailyRent: pricing?.dailyRent,
        deposit: pricing?.depositAmount
      }
    )

    return {
      ...bike,
      frameNo: item.frameNo,
      purchaseDate: item.purchaseDate,
      purchasePrice: item.purchasePrice,
      gpsDeviceNo: item.gpsDeviceNo,
      lastMaintenanceAt: item.lastMaintenanceAt,
      remark: item.remark
    }
  })

  return {
    list,
    summary: {
      totalBikes: list.length,
      available: list.filter((item) => item.statusCode === 1).length,
      renting: list.filter((item) => item.statusCode === 3).length,
      maintenance: list.filter((item) => item.statusCode === 4).length
    }
  }
}

export async function getAdminVehicleFormOptions() {
  const [modelPage, storePage] = await Promise.all([fetchAdminBikeModels(), fetchAdminStores()])

  return {
    models: pickRecords(modelPage).map((item) => ({
      label: [item.brandName, item.seriesName, item.modelName].filter(Boolean).join(' '),
      value: item.modelId,
      status: item.status,
      bikeType: item.bikeType,
      brandName: item.brandName,
      seriesName: item.seriesName,
      modelName: item.modelName
    })),
    stores: pickRecords(storePage).map((item) => ({
      label: item.storeName,
      value: item.storeId,
      city: item.city,
      status: item.status
    }))
  }
}

export function createAdminVehicle(data) {
  return request({
    url: '/admin/bikes',
    method: 'post',
    data
  })
}

export function updateAdminVehicle(bikeId, data) {
  return request({
    url: `/admin/bikes/${bikeId}`,
    method: 'put',
    data
  })
}

export function deleteAdminVehicle(bikeId) {
  return request({
    url: `/admin/bikes/${bikeId}`,
    method: 'delete'
  })
}

export async function getAdminVehicleModels() {
  const [modelPage, pricingPage] = await Promise.all([
    fetchAdminBikeModels(),
    fetchAdminPricing()
  ])

  const pricingMap = createLookupMap(pickRecords(pricingPage), (item) => item.modelId)
  const list = pickRecords(modelPage).map((item) => {
    const pricing = pricingMap.get(item.modelId)
    const bike = mapBikeRecord(
      {
        ...item,
        bikeCode: `MODEL-${String(item.modelId).padStart(4, '0')}`,
        frameSize: item.wheelSize,
        bikeStatus: item.status === 1 ? 1 : 5,
        storeName: '车型库'
      },
      {
        idOverride: item.modelId,
        dailyRent: pricing?.dailyRent,
        deposit: pricing?.depositAmount
      }
    )

    return {
      ...bike,
      modelId: item.modelId,
      brandName: item.brandName,
      seriesName: item.seriesName,
      modelName: item.modelName,
      bikeType: item.bikeType,
      frameMaterial: item.frameMaterial,
      gearSystem: item.gearSystem,
      brakeType: item.brakeType,
      wheelSize: item.wheelSize,
      bikeWeight: item.bikeWeight,
      marketPrice: item.marketPrice,
      coverImageUrl: item.coverImageUrl || bike.coverImageUrl,
      description: item.description,
      modelStatus: item.status
    }
  })

  return list
}

export async function getAdminBikeModelFormOptions() {
  return {
    pricingModelIds: pickRecords(await fetchAdminPricing()).map((item) => item.modelId)
  }
}

export function createAdminBikeModel(data) {
  return request({
    url: '/admin/bike-models',
    method: 'post',
    data
  })
}

export function uploadAdminBikeCover(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/admin/uploads/bike-cover',
    method: 'post',
    data: formData
  })
}

export function updateAdminBikeModel(modelId, data) {
  return request({
    url: `/admin/bike-models/${modelId}`,
    method: 'put',
    data
  })
}

export function deleteAdminBikeModel(modelId) {
  return request({
    url: `/admin/bike-models/${modelId}`,
    method: 'delete'
  })
}

export async function getAdminOrders() {
  const orderPage = await request({
    url: '/admin/orders/page',
    method: 'get',
    params: PAGE_PARAMS
  })

  const rawList = pickRecords(orderPage)
  const list = rawList.map((item) => mapOrderRecord(item))
  const prefix = todayPrefix()

  return {
    list,
    summary: {
      todayOrders: rawList.filter((item) => (item.createdAt || '').startsWith(prefix)).length,
      pendingPickup: rawList.filter((item) => item.orderStatus === 2).length,
      runningOrders: rawList.filter((item) => [2, 3, 4].includes(item.orderStatus)).length,
      completedOrders: rawList.filter((item) => item.orderStatus === 5).length
    }
  }
}

export function updateAdminOrder(orderId, data) {
  return request({
    url: `/admin/orders/${orderId}`,
    method: 'put',
    data
  })
}

export async function getAdminPricing() {
  const [pricingPage, modelPage] = await Promise.all([fetchAdminPricing(), fetchAdminBikeModels()])

  const pricingMap = createLookupMap(pickRecords(pricingPage), (item) => item.modelId)
  const modelList = pickRecords(modelPage)

  // 显示所有车型，包括已配置和未配置价格的
  const list = modelList.map((model) => {
    const pricing = pricingMap.get(model.modelId)
    if (pricing) {
      const pricingId = pricing.pricingId ?? pricing.id ?? null
      return {
        ...mapPricingRecord(pricing, createLookupMap(modelList, (item) => item.modelId)),
        id: pricingId,
        pricingId,
        modelId: model.modelId,
        effectiveFrom: pricing.effectiveFrom,
        effectiveTo: pricing.effectiveTo,
        status: pricing.status,
        hasPricing: true
      }
    } else {
      // 未配置价格的车型
      return {
        id: null,
        model: [model.brandName, model.modelName].filter(Boolean).join(' ') || '未命名车型',
        packageName: '未配置',
        dailyRent: 0,
        deposit: 0,
        overtime: 0,
        updatedAt: '-',
        modelId: model.modelId,
        effectiveFrom: null,
        effectiveTo: null,
        status: 0,
        hasPricing: false
      }
    }
  })

  // 只统计有价格策略的数据
  const pricedList = list.filter((item) => item.hasPricing)
  const rents = pricedList.map((item) => item.dailyRent)
  const deposits = pricedList.map((item) => item.deposit)
  const highest = pricedList.reduce(
    (current, item) => (item.dailyRent > current.dailyRent ? item : current),
    { model: '-', dailyRent: 0 }
  )

  return {
    list,
    highlights: {
      averageDailyRent:
        rents.length > 0
          ? Math.round(rents.reduce((total, item) => total + item, 0) / rents.length)
          : 0,
      highestModel: highest.model || '-',
      depositRange:
        deposits.length > 0
          ? `${Math.min(...deposits)} - ${Math.max(...deposits)}`
          : '-',
      totalModels: modelList.length,
      pricedModels: pricedList.length
    }
  }
}

export async function getAdminPricingFormOptions() {
  return {
    models: pickRecords(await fetchAdminBikeModels()).map((item) => ({
      label: [item.brandName, item.modelName].filter(Boolean).join(' '),
      value: item.modelId,
      status: item.status
    }))
  }
}

export function createAdminPricing(data) {
  return request({
    url: '/admin/pricings',
    method: 'post',
    data
  })
}

export function updateAdminPricing(pricingId, data) {
  return request({
    url: `/admin/pricings/${pricingId}`,
    method: 'put',
    data
  })
}

export function deleteAdminPricing(pricingId) {
  return request({
    url: `/admin/pricings/${pricingId}`,
    method: 'delete'
  })
}

export async function getAdminStatistics() {
  const [statistics, orderPage, storePage, bikePage] = await Promise.all([
    request({
      url: '/admin/statistics/overview',
      method: 'get'
    }),
    request({ url: '/admin/orders/page', method: 'get', params: PAGE_PARAMS }),
    request({ url: '/admin/stores/page', method: 'get', params: PAGE_PARAMS }),
    fetchAdminBikes()
  ])

  const orders = pickRecords(orderPage)
  const bikes = pickRecords(bikePage)
  const inventoryCountByStoreId = groupTotals(bikes, (item) => item.currentStoreId ?? item.storeId)
  const availableCountByStoreId = groupTotals(
    bikes.filter((item) => Number(item.bikeStatus) === 1),
    (item) => item.currentStoreId ?? item.storeId
  )
  const stores = pickRecords(storePage).map((item) => {
    const inventoryCount = inventoryCountByStoreId.get(item.storeId) || 0
    const availableCount = availableCountByStoreId.get(item.storeId) || 0
    const base = mapStoreEntity(item, {
      availableCount
    })

    return {
      ...base,
      inventoryCount,
      utilization:
        base.capacity > 0
          ? Math.min(100, Math.max(0, Math.round((inventoryCount / base.capacity) * 100)))
          : 0
    }
  })
  const revenueByStore = groupTotals(
    orders,
    (item) => item.pickupStoreName,
    (item) => item.totalAmount ?? item.rentAmount
  )
  const orderCountByStore = groupTotals(orders, (item) => item.pickupStoreName)
  const totalOrders = Math.max(orders.length, 1)
  const modelOrderCounts = groupTotals(
    orders,
    (item) => [item.brandName, item.modelName].filter(Boolean).join(' ').trim() || item.bikeCode
  )
  const timeline = groupTotals(
    orders,
    (item) => (item.createdAt || item.plannedStartTime || '').slice(0, 10),
    (item) => item.totalAmount ?? item.rentAmount
  )
  const channelCounts = groupTotals(orders, (item) =>
    item.createdStaffName ? '门店协助下单' : '用户自主下单'
  )

  const storePerformance = stores.map((item) => ({
    name: item.name,
    revenue: Math.round(revenueByStore.get(item.name) || 0),
    orders: Math.round(orderCountByStore.get(item.name) || 0),
    utilization: item.utilization,
    inventoryCount: item.inventoryCount,
    capacity: item.capacity,
    available: item.available
  }))

  const hotModels = Array.from(modelOrderCounts.entries())
    .sort((left, right) => right[1] - left[1])
    .slice(0, 5)
    .map(([model, count]) => ({
      model,
      orders: count,
      growth: `${Math.round((count / totalOrders) * 100)}%`
    }))

  const incomeTrend = Array.from(timeline.entries())
    .sort(([left], [right]) => left.localeCompare(right))
    .slice(-5)
    .map(([date, amount]) => ({
      week: date || '-',
      amount: Math.max(1, Math.round(amount / 1000))
    }))

  const channelTotal = Math.max(
    1,
    Array.from(channelCounts.values()).reduce((total, item) => total + item, 0)
  )
  const channels = Array.from(channelCounts.entries()).map(([name, count]) => ({
    name,
    percent: Math.round((count / channelTotal) * 100)
  }))

  return {
    overview: buildAdminOverview(statistics),
    storePerformance,
    hotModels,
    incomeTrend,
    channels
  }
}

export async function getAdminStores() {
  const [storePage, bikePage] = await Promise.all([fetchAdminStores(), fetchAdminBikes()])

  const bikes = pickRecords(bikePage)
  const inventoryCountByStoreId = groupTotals(bikes, (item) => item.currentStoreId ?? item.storeId)
  const availableCountByStoreId = groupTotals(
    bikes.filter((item) => Number(item.bikeStatus) === 1),
    (item) => item.currentStoreId ?? item.storeId
  )
  const list = pickRecords(storePage).map((item) => {
    const inventoryCount = inventoryCountByStoreId.get(item.storeId) || 0
    const availableCount = availableCountByStoreId.get(item.storeId) || 0
    const store = mapStoreEntity(item, {
      availableCount
    })
    const utilization =
      store.capacity > 0
        ? Math.min(100, Math.max(0, Math.round((inventoryCount / store.capacity) * 100)))
        : 0

    return {
      ...store,
      contactName: item.contactName,
      province: item.province,
      latitude: item.latitude,
      longitude: item.longitude,
      storeImageUrl: item.storeImageUrl,
      statusCode: item.status,
      inventoryCount,
      utilization,
      services: buildStoreServices(store)
    }
  })

  const averageUtilization =
    list.length > 0
      ? Math.round(list.reduce((total, item) => total + item.utilization, 0) / list.length)
      : 0

  return {
    list,
    summary: {
      totalStores: toNumber(storePage?.total, list.length),
      averageUtilization: `${averageUtilization}%`,
      activeCities: new Set(list.map((item) => item.city).filter(Boolean)).size,
      serviceCoverage: list.length > 0 ? '100%' : '0%'
    }
  }
}

export function createAdminStore(data) {
  return request({
    url: '/admin/stores',
    method: 'post',
    data
  })
}

export function updateAdminStore(storeId, data) {
  return request({
    url: `/admin/stores/${storeId}`,
    method: 'put',
    data
  })
}

export function deleteAdminStore(storeId) {
  return request({
    url: `/admin/stores/${storeId}`,
    method: 'delete'
  })
}

export function uploadAdminStoreCover(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/admin/uploads/store-cover',
    method: 'post',
    data: formData
  })
}

export function getAdminProfile() {
  return request({
    url: '/admin/profile/me',
    method: 'get'
  })
}

export function updateAdminProfile(data) {
  return request({
    url: '/admin/profile/me',
    method: 'put',
    data
  })
}

export async function getAdminStaffMembers(params = {}) {
  const page = await request({
    url: '/admin/store-staff/page',
    method: 'get',
    params: {
      ...PAGE_PARAMS,
      ...params
    }
  })

  return {
    list: pickRecords(page),
    total: toNumber(page?.total, 0)
  }
}

export function updateAdminStaffMember(staffId, data) {
  return request({
    url: `/admin/store-staff/${staffId}`,
    method: 'put',
    data
  })
}
