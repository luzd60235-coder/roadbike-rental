import request from '@/utils/request'
import {
  createLookupMap,
  groupTotals,
  mapBikeRecord,
  mapMaintenanceRecord,
  mapOrderRecord,
  mapStoreEntity,
  pickRecords,
  toNumber
} from '@/utils/backend'

const PAGE_PARAMS = {
  current: 1,
  size: 100
}

function buildStoreServices(store) {
  const serviceMap = {
    上海: ['异店还车', '夜骑集合', '头盔租借'],
    杭州: ['环湖骑行', '新手陪骑', '寄存服务'],
    苏州: ['园区通勤', '湖畔慢骑', '车辆调度']
  }

  return serviceMap[store?.city] || ['标准租赁', '车辆保养']
}

async function fetchUserBikes(params = {}) {
  const page = await request({
    url: '/user/bikes/page',
    method: 'get',
    params: {
      ...PAGE_PARAMS,
      ...params
    }
  })

  return pickRecords(page)
}

async function fetchUserStores(params = {}) {
  const page = await request({
    url: '/user/stores/page',
    method: 'get',
    params: {
      ...PAGE_PARAMS,
      ...params
    }
  })

  return pickRecords(page)
}

async function fetchFavoritePage(params = {}) {
  const page = await request({
    url: '/user/favorites/page',
    method: 'get',
    params: {
      ...PAGE_PARAMS,
      ...params
    }
  })

  return page
}

function mapUserStores(rawStores, rawBikes) {
  const availableByStore = groupTotals(
    rawBikes.filter((item) => Number(item.bikeStatus) === 1),
    (item) => item.storeId ?? item.currentStoreId
  )

  return rawStores.map((item) =>
    mapStoreEntity(item, {
      availableCount: availableByStore.get(item.storeId) || 0,
      services: buildStoreServices(item)
    })
  )
}

export async function getUserHome() {
  const [rawBikes, rawStores] = await Promise.all([fetchUserBikes(), fetchUserStores()])
  const stores = mapUserStores(rawStores, rawBikes)
  const storeMap = createLookupMap(stores, (item) => item.id)
  const bikes = rawBikes.map((item) => mapBikeRecord(item, { storeMap }))
  const rentableCount = bikes.filter((item) => item.statusCode === 1).length
  const recommended = bikes.filter((item) => item.statusCode === 1).slice(0, 3)

  return {
    hero: {
      title: '都市圈公路车租赁平台',
      subtitle: '支持车辆浏览、在线预约、收藏管理和门店服务查询。'
    },
    quickStats: [
      { label: '可租车辆', value: `${rentableCount}` },
      { label: '覆盖门店', value: `${stores.length} 家` },
      { label: '异店还车', value: '已支持' },
      { label: '会员服务', value: '已开启' }
    ],
    recommended: recommended.length > 0 ? recommended : bikes.slice(0, 3),
    stores: stores.slice(0, 4)
  }
}

export async function getUserBikes(params) {
  const [rawBikes, rawStores] = await Promise.all([fetchUserBikes(params), fetchUserStores()])
  const stores = mapUserStores(rawStores, rawBikes)
  const storeMap = createLookupMap(stores, (item) => item.id)

  return {
    list: rawBikes.map((item) => mapBikeRecord(item, { storeMap }))
  }
}

export async function getBikeDetail(id) {
  const [detail, rawBikes, rawStores] = await Promise.all([
    request({
      url: `/user/bikes/${id}`,
      method: 'get'
    }),
    fetchUserBikes(),
    fetchUserStores()
  ])

  const stores = mapUserStores(rawStores, rawBikes)
  const storeMap = createLookupMap(stores, (item) => item.id)
  const bikes = rawBikes.map((item) => mapBikeRecord(item, { storeMap }))
  const bike = mapBikeRecord(detail?.bike, { storeMap })

  if (!bike?.id) {
    throw new Error('未找到对应车辆')
  }

  return {
    bike,
    maintenanceHistory: Array.isArray(detail?.maintenanceHistory)
      ? detail.maintenanceHistory.map((item) => mapMaintenanceRecord(item))
      : [],
    nearbyStores: stores.filter((item) => item.city === bike.city).slice(0, 3),
    similar: bikes.filter((item) => item.id !== bike.id).slice(0, 3)
  }
}

export async function getUserOrders() {
  const page = await request({
    url: '/user/orders/page',
    method: 'get',
    params: PAGE_PARAMS
  })

  return {
    list: pickRecords(page).map((item) => mapOrderRecord(item))
  }
}

export async function getUserFavorites() {
  const [favoritePage, rawBikes, rawStores] = await Promise.all([
    fetchFavoritePage(),
    fetchUserBikes(),
    fetchUserStores()
  ])

  const stores = mapUserStores(rawStores, rawBikes)
  const storeMap = createLookupMap(stores, (item) => item.id)
  const bikes = rawBikes.map((item) => mapBikeRecord(item, { storeMap }))
  const bikeByModelId = createLookupMap(bikes, (item) => item.modelId)

  return {
    list: pickRecords(favoritePage).map((item) => {
      const bike = bikeByModelId.get(item.modelId)
      const fallbackName = [item.brandName, item.modelName].filter(Boolean).join(' ') || '收藏车型'

      return {
        id: bike?.id ?? item.modelId,
        favoriteId: item.favoriteId,
        modelId: item.modelId,
        bikeName: bike?.bikeName ?? fallbackName,
        brand: bike?.brand ?? item.brandName ?? '',
        model: bike?.model ?? item.modelName ?? '',
        summary: bike?.summary ?? item.bikeType ?? '已收藏的骑行车型',
        price: bike?.price ?? toNumber(item.marketPrice, 0),
        originalPrice: bike?.originalPrice ?? bike?.price ?? toNumber(item.marketPrice, 0),
        maintenanceHistoryCount: bike?.maintenanceHistoryCount ?? 0,
        maintenanceDiscountPercent: bike?.maintenanceDiscountPercent ?? 0,
        hasMaintenanceDiscount: bike?.hasMaintenanceDiscount ?? false,
        addedAt: item.createdAt || '-',
        coverImageUrl: bike?.coverImageUrl ?? item.coverImageUrl ?? ''
      }
    })
  }
}

export async function getUserStores() {
  const [rawStores, rawBikes] = await Promise.all([fetchUserStores(), fetchUserBikes()])

  return {
    list: mapUserStores(rawStores, rawBikes)
  }
}

export async function getUserStoreDetail(storeId) {
  const [rawStore, rawBikes] = await Promise.all([
    request({
      url: `/user/stores/${storeId}`,
      method: 'get'
    }),
    fetchUserBikes({ storeId })
  ])

  if (!rawStore?.storeId) {
    throw new Error('未找到对应门店')
  }

  const store = mapStoreEntity(rawStore, {
    availableCount: rawBikes.filter((item) => Number(item.bikeStatus) === 1).length,
    services: buildStoreServices(rawStore)
  })
  const storeMap = createLookupMap([store], (item) => item.id)
  const bikes = rawBikes.map((item) => mapBikeRecord(item, { storeMap }))

  return {
    store,
    bikes,
    summary: {
      totalBikes: bikes.length,
      availableBikes: bikes.filter((item) => item.statusCode === 1).length,
      rentingBikes: bikes.filter((item) => item.statusCode === 3).length,
      maintenanceBikes: bikes.filter((item) => item.statusCode === 4).length
    }
  }
}

export async function getUserFavoriteModelIds() {
  const page = await fetchFavoritePage()
  return pickRecords(page)
    .map((item) => item.modelId)
    .filter((item) => item != null)
}

export function addUserFavorite(modelId) {
  return request({
    url: '/user/favorites',
    method: 'post',
    params: { modelId }
  })
}

export function removeUserFavorite(modelId) {
  return request({
    url: '/user/favorites',
    method: 'delete',
    params: { modelId }
  })
}

export async function createUserOrder(payload) {
  const order = await request({
    url: '/user/orders',
    method: 'post',
    data: payload
  })

  return mapOrderRecord(order)
}

export function cancelUserOrder(orderId, reason = '用户主动取消') {
  return request({
    url: `/user/orders/${orderId}/cancel`,
    method: 'put',
    params: { reason }
  })
}

export function getUserProfile() {
  return request({
    url: '/user/profile/me',
    method: 'get'
  })
}

export function updateUserProfile(data) {
  return request({
    url: '/user/profile/me',
    method: 'put',
    data
  })
}
