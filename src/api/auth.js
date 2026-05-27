import request from '@/utils/request'
import { normalizeRole } from '@/utils/auth'
import { mapLoginPayload } from '@/utils/backend'

const AUTH_PATH_BY_ROLE = {
  admin: {
    login: '/admin/auth/login',
    register: '/admin/auth/register'
  },
  user: {
    login: '/user/auth/login',
    register: '/user/auth/register'
  },
  staff: {
    login: '/staff/auth/login',
    register: '/staff/auth/register'
  }
}

function buildRegisterPayload(role, data) {
  const commonPayload = {
    username: data?.username || '',
    password: data?.password || '',
    confirmPassword: data?.confirmPassword || '',
    phone: data?.phone || '',
    idCardNo: data?.idCardNo || ''
  }

  if (role === 'admin') {
    return {
      ...commonPayload,
      realName: data?.realName || '',
      email: data?.email || ''
    }
  }

  if (role === 'staff') {
    return {
      ...commonPayload,
      gender: data?.gender ?? 0,
      storeId: data?.storeId || null,
      staffName: data?.staffName || '',
      jobTitle: data?.jobTitle || 'clerk'
    }
  }

  return {
    ...commonPayload,
    gender: data?.gender ?? 0,
    nickname: data?.nickname || '',
    email: data?.email || ''
  }
}

export async function login(data) {
  const role = normalizeRole(data?.role)
  const url = AUTH_PATH_BY_ROLE[role]?.login || AUTH_PATH_BY_ROLE.admin.login
  const result = await request({
    url,
    method: 'post',
    data: {
      username: data?.username || '',
      password: data?.password || ''
    }
  })

  return mapLoginPayload(result)
}

export async function register(data) {
  const role = normalizeRole(data?.role)
  const url = AUTH_PATH_BY_ROLE[role]?.register || AUTH_PATH_BY_ROLE.user.register
  const result = await request({
    url,
    method: 'post',
    data: buildRegisterPayload(role, data)
  })

  return mapLoginPayload(result)
}

export async function getStaffRegisterStores() {
  const stores = await request({
    url: '/staff/auth/register/options',
    method: 'get'
  })

  return Array.isArray(stores)
    ? stores.map((item) => ({
        label: [item.storeName, item.city, item.district].filter(Boolean).join(' / '),
        value: item.storeId
      }))
    : []
}
