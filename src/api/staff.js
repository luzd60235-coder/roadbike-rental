import request from '@/utils/request'
import {
  mapMaintenanceRecord,
  mapOrderRecord,
  mapSettlementRecord,
  pickRecords,
  toNumber,
  todayPrefix
} from '@/utils/backend'

const PAGE_PARAMS = {
  current: 1,
  size: 100
}

export async function getStaffTrades(filters = {}) {
  const page = await request({
    url: '/staff/operations/orders/page',
    method: 'get',
    params: {
      ...PAGE_PARAMS,
      status: filters?.status || undefined,
      keyword: filters?.keyword?.trim() || undefined
    }
  })

  const rawList = pickRecords(page)
  const list = rawList.map((item) => mapOrderRecord(item))
  const today = todayPrefix()
  const todayTurnover = rawList
    .filter((item) =>
      [item.createdAt, item.actualPickupTime, item.actualReturnTime, item.plannedStartTime]
        .filter(Boolean)
        .some((value) => value.startsWith(today))
    )
    .reduce((total, item) => {
      // 只统计租金，不包含押金
      const orderAmount = toNumber(item.rentAmount, 0)
      return total + orderAmount
    }, 0)

  return {
    list,
    summary: {
      pendingPickup: rawList.filter((item) => item.orderStatus === 2).length,
      waitingReturn: rawList.filter((item) => item.orderStatus === 3).length,
      todayTurnover,
      currentShift: '白班值守'
    }
  }
}

export function getStaffTradeFormOptions() {
  return request({
    url: '/staff/operations/orders/form-options',
    method: 'get'
  })
}

export function createStaffOfflineOrder(data) {
  return request({
    url: '/staff/operations/orders',
    method: 'post',
    data
  })
}

export function pickupStaffOrder(orderId) {
  return request({
    url: '/staff/operations/pickup',
    method: 'post',
    data: { orderId }
  })
}

export function returnStaffOrder(orderId) {
  return request({
    url: '/staff/operations/return',
    method: 'post',
    data: { orderId }
  })
}

export async function getStaffSettlements() {
  const page = await request({
    url: '/staff/settlements/page',
    method: 'get',
    params: PAGE_PARAMS
  })

  const list = pickRecords(page).map((item) => mapSettlementRecord(item))

  return {
    list,
    overview: {
      currentIncome: list.reduce((total, item) => total + item.finalIncome, 0),
      refundAmount: list.reduce((total, item) => total + item.refundAmount, 0),
      pendingReview: list.filter((item) => item.status !== '已结算').length,
      maintenanceCost: list.reduce((total, item) => total + item.maintenanceCost, 0)
    }
  }
}

export function createStaffSettlement(data) {
  return request({
    url: '/staff/settlements',
    method: 'post',
    data
  })
}

export async function getStaffMaintenance() {
  const page = await request({
    url: '/staff/maintenance/page',
    method: 'get',
    params: PAGE_PARAMS
  })

  const rawList = pickRecords(page)
  const list = rawList.map((item) => mapMaintenanceRecord(item))
  const prefix = todayPrefix()

  return {
    list,
    overview: {
      pendingJobs: rawList.filter((item) => item.maintenanceStatus === 1).length,
      inProgress: rawList.filter((item) => item.maintenanceStatus === 2).length,
      completedToday: rawList.filter((item) => (item.endTime || '').startsWith(prefix)).length,
      sparePartsStock: list.length > 0 ? '正常' : '-'
    }
  }
}

export function getStaffMaintenanceFormOptions() {
  return request({
    url: '/staff/maintenance/form-options',
    method: 'get'
  })
}

export function createStaffMaintenance(data) {
  return request({
    url: '/staff/maintenance',
    method: 'post',
    data
  })
}

export function updateStaffMaintenance(maintenanceId, data) {
  return request({
    url: `/staff/maintenance/${maintenanceId}`,
    method: 'put',
    data
  })
}

export function getStaffProfile() {
  return request({
    url: '/staff/profile/me',
    method: 'get'
  })
}

export function updateStaffProfile(data) {
  return request({
    url: '/staff/profile/me',
    method: 'put',
    data
  })
}
