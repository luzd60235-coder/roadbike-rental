import { resolveBikeImage, resolveStoreImage } from '@/utils/media'

const ROLE_TYPE_MAP = {
  ADMIN: 'admin',
  USER: 'user',
  STAFF: 'staff'
}
// 后端数据到前端展示模型的映射工具。
const BIKE_STATUS_META = {
  1: { label: '可租赁', type: 'success' },
  2: { label: '已预订', type: 'warning' },
  3: { label: '租赁中', type: 'primary' },
  4: { label: '维修中', type: 'danger' },
  5: { label: '已停用', type: 'info' }
}

const ORDER_STATUS_META = {
  1: { label: '待支付', type: 'warning' },
  2: { label: '待取车', type: 'warning' },
  3: { label: '租赁中', type: 'primary' },
  4: { label: '待结算', type: 'danger' },
  5: { label: '已完成', type: 'success' },
  6: { label: '已取消', type: 'info' }
}

const MAINTENANCE_STATUS_META = {
  1: { label: '待处理', type: 'warning' },
  2: { label: '维修中', type: 'danger' },
  3: { label: '已完成', type: 'success' }
}

const PAYMENT_STATUS_LABELS = {
  0: '未支付',
  1: '已支付',
  2: '部分退款',
  3: '已退款'
}

function fallbackMeta(value) {
  return {
    label: value == null ? '-' : String(value),
    type: 'info'
  }
}

function resolveMeta(value, numericMap, stringMap = {}) {
  if (typeof value === 'number') {
    return numericMap[value] || fallbackMeta(value)
  }

  if (typeof value === 'string') {
    if (stringMap[value]) {
      return stringMap[value]
    }

    const lowered = value.toLowerCase()
    if (stringMap[lowered]) {
      return stringMap[lowered]
    }

    const numericValue = Number(value)
    if (!Number.isNaN(numericValue) && numericMap[numericValue]) {
      return numericMap[numericValue]
    }
  }

  return fallbackMeta(value)
}

function safeText(value, fallback = '') {
  return typeof value === 'string' ? value : fallback
}

function buildBikeName(brand, model, fallback = '-') {
  const name = [brand, model].filter(Boolean).join(' ').trim()
  return name || fallback
}

function buildBikeSummary(bike, store) {
  const parts = [
    bike.bikeType || bike.type,
    bike.frameMaterial || bike.frame,
    store?.city || bike.city
  ].filter(Boolean)

  return parts.length > 0 ? parts.join(' / ') : '适合日常骑行与门店租赁。'
}

function buildBikeTags(bike, store) {
  return [
    bike.bikeType || bike.type,
    bike.frameSize ? `尺码 ${bike.frameSize}` : bike.size,
    store?.city || bike.city
  ].filter(Boolean)
}

function formatDateLabel(value, fallback = '-') {
  return value || fallback
}

export function todayPrefix() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function toNumber(value, fallback = 0) {
  const number = Number(value)
  return Number.isFinite(number) ? number : fallback
}

export function pickRecords(pageResult) {
  return Array.isArray(pageResult?.records) ? pageResult.records : []
}

export function createLookupMap(list, keyGetter) {
  const map = new Map()
  list.forEach((item) => {
    const key = keyGetter(item)
    if (key != null) {
      map.set(key, item)
    }
  })
  return map
}

export function groupTotals(list, keyGetter, valueGetter = () => 1) {
  const map = new Map()
  list.forEach((item) => {
    const key = keyGetter(item)
    if (key == null || key === '') {
      return
    }
    map.set(key, (map.get(key) || 0) + toNumber(valueGetter(item), 0))
  })
  return map
}

export function roleTypeToRole(roleType) {
  return ROLE_TYPE_MAP[roleType] || 'user'
}

export function mapLoginPayload(payload) {
  const role = roleTypeToRole(payload?.roleType)
  const displayName = payload?.displayName || payload?.username || '用户'
  const fallbackMotto =
    role === 'admin'
      ? '当前已连接后台管理数据。'
      : role === 'staff'
        ? '当前已连接门店作业数据。'
        : '当前已连接车辆租赁服务。'

  return {
    token: payload?.token || '',
    user: {
      id: payload?.userId || null,
      username: payload?.username || '',
      role,
      name: displayName,
      avatar: displayName.slice(0, 1).toUpperCase(),
      storeId: payload?.storeId || null,
      storeName: payload?.storeName || (payload?.storeId ? `门店 ${payload.storeId}` : ''),
      motto: payload?.motto || fallbackMotto
    }
  }
}

export function getBikeStatusMeta(value) {
  return resolveMeta(value, BIKE_STATUS_META, {
    available: BIKE_STATUS_META[1],
    reserved: BIKE_STATUS_META[2],
    renting: BIKE_STATUS_META[3],
    maintenance: BIKE_STATUS_META[4],
    disabled: BIKE_STATUS_META[5]
  })
}

export function getOrderStatusMeta(value) {
  return resolveMeta(value, ORDER_STATUS_META)
}

export function getMaintenanceStatusMeta(value) {
  return resolveMeta(value, MAINTENANCE_STATUS_META)
}

export function getPaymentStatusLabel(value) {
  if (typeof value === 'string' && value) {
    return value
  }
  return PAYMENT_STATUS_LABELS[value] || '未知'
}

export function mapStoreEntity(store, options = {}) {
  const capacity = toNumber(store?.inventoryCapacity ?? store?.capacity, 0)
  const availableCount = options.availableCount
  const available =
    availableCount != null
      ? availableCount
      : store?.available != null
        ? toNumber(store.available, 0)
        : '--'
  const utilization =
    typeof available === 'number' && capacity > 0
      ? Math.min(100, Math.max(0, Math.round(((capacity - available) / capacity) * 100)))
      : toNumber(store?.utilization, 0)

  return {
    id: store?.storeId ?? store?.id ?? null,
    name: store?.storeName ?? store?.name ?? '-',
    city: safeText(store?.city),
    district: safeText(store?.district),
    address: store?.detailAddress ?? store?.address ?? '-',
    phone: store?.contactPhone ?? store?.phone ?? '-',
    hours: store?.businessHours ?? store?.hours ?? '-',
    capacity,
    available,
    utilization,
    services: Array.isArray(store?.services) ? store.services : options.services || [],
    rating: store?.rating ?? 4.8,
    description: safeText(store?.description),
    storeImageUrl: safeText(store?.storeImageUrl),
    imageUrl: resolveStoreImage(store)
  }
}

export function mapBikeRecord(bike, options = {}) {
  const storeMap = options.storeMap || new Map()
  const storeId = bike?.storeId ?? bike?.currentStoreId ?? null
  const store = storeMap.get(storeId)
  const rawStatus = options.statusOverride ?? bike?.bikeStatus ?? bike?.status
  const status = getBikeStatusMeta(rawStatus)
  const statusCode = Number(rawStatus)
  const brand = bike?.brandName ?? bike?.brand ?? ''
  const model = bike?.modelName ?? bike?.model ?? ''
  const bikeName = bike?.bikeName ?? buildBikeName(brand, model, bike?.bikeCode || '车辆')
  const originalPrice = toNumber(
    options.originalDailyRent ?? bike?.originalDailyRent ?? bike?.dailyRent ?? bike?.price,
    toNumber(bike?.marketPrice, 0)
  )
  const price = toNumber(options.dailyRent ?? bike?.dailyRent ?? bike?.price, originalPrice)
  const deposit = toNumber(options.deposit ?? bike?.depositAmount ?? bike?.deposit, 0)
  const maintenanceHistoryCount = toNumber(bike?.maintenanceHistoryCount, 0)
  const maintenanceDiscountRate = toNumber(bike?.maintenanceDiscountRate, 0)
  const maintenanceDiscountPercent = Math.round(maintenanceDiscountRate * 100)

  return {
    id: options.idOverride ?? bike?.bikeId ?? bike?.id ?? bike?.modelId ?? null,
    bikeId: bike?.bikeId ?? bike?.id ?? null,
    modelId: bike?.modelId ?? null,
    code: bike?.bikeCode ?? bike?.code ?? (bike?.modelId ? `MODEL-${bike.modelId}` : '-'),
    brand,
    model,
    bikeName,
    type: bike?.bikeType ?? bike?.type ?? '公路车',
    frame: bike?.frameMaterial ?? bike?.frame ?? '-',
    gear: bike?.gearSystem ?? bike?.gear ?? '-',
    brake: bike?.brakeType ?? bike?.brake ?? '-',
    wheel: bike?.wheelSize ?? bike?.wheel ?? '-',
    weight:
      bike?.bikeWeight != null
        ? `${bike.bikeWeight}kg`
        : bike?.weight || '-',
    size: bike?.frameSize ?? bike?.size ?? '-',
    color: bike?.color ?? '-',
    price,
    originalPrice,
    deposit,
    mileage: toNumber(bike?.mileageKm ?? bike?.mileage, 0),
    rating: toNumber(bike?.rating, 4.8),
    storeId,
    storeName: bike?.storeName ?? store?.name ?? '-',
    city: bike?.city ?? store?.city ?? '',
    district: bike?.district ?? store?.district ?? '',
    statusCode: Number.isFinite(statusCode) ? statusCode : null,
    status: status.label,
    statusType: status.type,
    maintenanceHistoryCount,
    maintenanceDiscountRate,
    maintenanceDiscountPercent,
    hasMaintenanceDiscount: maintenanceHistoryCount > 0 && maintenanceDiscountRate > 0,
    tags: buildBikeTags(bike, store),
    summary: bike?.description ?? buildBikeSummary(bike, store),
    coverImageUrl: bike?.coverImageUrl || resolveBikeImage({ ...bike, brand, model })
  }
}

export function mapOrderRecord(order) {
  const rawStatus = order?.orderStatus ?? order?.status
  const status = getOrderStatusMeta(order?.orderStatus ?? order?.status)
  const bikeName =
    buildBikeName(order?.brandName, order?.modelName, order?.bikeName || order?.bikeCode || '车辆')

  return {
    id: order?.orderId ?? order?.id ?? null,
    orderId: order?.orderId ?? order?.id ?? null,
    orderNo: order?.orderNo ?? '-',
    userName: order?.nickname ?? order?.username ?? order?.userName ?? '-',
    customer: order?.nickname ?? order?.username ?? order?.userName ?? '-',
    bikeName,
    bikeCode: order?.bikeCode ?? '-',
    pickupStore: order?.pickupStoreName ?? order?.pickupStore ?? '-',
    returnStore: order?.returnStoreName ?? order?.returnStore ?? '-',
    startTime: formatDateLabel(order?.plannedStartTime ?? order?.startTime),
    endTime: formatDateLabel(order?.plannedEndTime ?? order?.endTime),
    pickupTime: formatDateLabel(order?.actualPickupTime ?? order?.plannedStartTime ?? order?.pickupTime),
    returnTime: formatDateLabel(order?.actualReturnTime ?? order?.plannedEndTime ?? order?.returnTime),
    amount: toNumber(order?.totalAmount ?? order?.rentAmount ?? order?.amount, 0),
    rentAmount: toNumber(order?.rentAmount ?? order?.amount, 0),
    deposit: toNumber(order?.depositAmount ?? order?.deposit, 0),
    overtimeFeePerHour: toNumber(order?.overtimeFeePerHour, 0),
    paymentStatus: getPaymentStatusLabel(order?.paymentStatus),
    statusCode: Number.isFinite(Number(rawStatus)) ? Number(rawStatus) : null,
    status: status.label,
    statusType: status.type,
    operator: order?.createdStaffName ?? order?.operator ?? '-',
    cancelReason: order?.cancelReason ?? '',
    remark: order?.remark ?? '',
    createdAt: formatDateLabel(order?.createdAt)
  }
}

export function mapPricingRecord(pricing, modelMap = new Map()) {
  const model = modelMap.get(pricing?.modelId)
  const label = model
    ? buildBikeName(model.brandName, model.modelName, `车型 ${pricing?.modelId ?? '-'}`)
    : `车型 ${pricing?.modelId ?? '-'}`
  const dailyRent = toNumber(pricing?.dailyRent, 0)

  return {
    id: pricing?.pricingId ?? pricing?.id ?? null,
    model: label,
    packageName: pricing?.packageName ?? `方案 ${pricing?.pricingId ?? pricing?.id ?? '-'}`,
    dailyRent,
    weekendRent: Math.round(dailyRent * 1.12),
    deposit: toNumber(pricing?.depositAmount ?? pricing?.deposit, 0),
    overtime: toNumber(pricing?.overtimeFeePerHour ?? pricing?.overtime, 0),
    updatedAt: pricing?.updatedAt ?? pricing?.effectiveFrom ?? pricing?.createdAt ?? '-'
  }
}

export function mapSettlementRecord(item) {
  // 退款金额为押金退款（扣除维修/赔偿后的实际退款）
  const refundAmount = toNumber(item?.refundDepositAmount ?? item?.refundAmount, 0)
  const maintenanceCost = toNumber(item?.maintenanceCompensation ?? item?.maintenanceCost, 0)
  const rentIncome = toNumber(item?.baseRentAmount ?? item?.rentIncome, 0)
  const finalIncome = toNumber(item?.payableTotal ?? item?.finalIncome, rentIncome)

  return {
    id: item?.settlementId ?? item?.id ?? null,
    cycle: item?.cycle ?? (item?.settledAt ? item.settledAt.slice(0, 10) : '-'),
    orderNo: item?.orderNo ?? '-',
    storeName: item?.settleStoreName ?? item?.storeName ?? '-',
    orderCount: toNumber(item?.orderCount, 1),
    rentIncome,
    refundAmount,
    maintenanceCost,
    finalIncome,
    status: item?.status ?? '已结算',
    statusType: item?.statusType ?? 'success'
  }
}

export function mapMaintenanceRecord(item) {
  const rawStatus = item?.maintenanceStatus ?? item?.status
  const status = getMaintenanceStatusMeta(rawStatus)
  const cost = toNumber(item?.maintenanceCost ?? item?.cost, 0)
  let level = '轻度'

  if (cost >= 300) {
    level = '紧急'
  } else if (cost >= 100) {
    level = '中等'
  }

  return {
    id: item?.maintenanceId ?? item?.id ?? null,
    maintenanceId: item?.maintenanceId ?? item?.id ?? null,
    bikeCode: item?.bikeCode ?? '-',
    bikeName: item?.bikeName ?? `车辆 ${item?.bikeId ?? ''}`.trim(),
    storeName: item?.storeName ?? '-',
    issue: item?.faultDescription ?? item?.issue ?? '-',
    level,
    engineer: item?.assignedStaffName ?? item?.engineer ?? '-',
    cost,
    maintenanceType: item?.maintenanceType ?? '-',
    startTime: item?.startTime ?? '',
    endTime: item?.endTime ?? '',
    remark: item?.remark ?? '',
    reportTime: item?.startTime ?? item?.createdAt ?? item?.reportTime ?? '-',
    statusCode: Number.isFinite(Number(rawStatus)) ? Number(rawStatus) : null,
    status: status.label,
    statusType: status.type
  }
}
