import axios from 'axios'
import { ElMessage } from 'element-plus'
import { mockAdapter } from '@/mock/adapter'
import { clearAuthSession, getToken } from './auth'
// Axios 请求封装，负责 token 注入、统一报错、登录过期处理等。
const useMock = import.meta.env.VITE_USE_MOCK === 'true'
const successCodes = new Set([0, 200])
const authErrorPatterns = [/jwt expired/i, /token.*expired/i, /unauthorized/i, /invalid token/i]
let authRedirecting = false

function shouldSkipToken(url = '') {
  return /\/auth\/(login|register(?:\/options)?)$/i.test(url)
}

function isAuthExpiredMessage(message = '') {
  return authErrorPatterns.some((pattern) => pattern.test(message))
}

function handleAuthExpired() {
  clearAuthSession()
  if (authRedirecting) {
    return
  }

  authRedirecting = true
  ElMessage.closeAll()
  ElMessage.error('登录状态已过期，请重新登录')

  window.setTimeout(() => {
    if (window.location.pathname !== '/login') {
      window.location.replace('/login')
    } else {
      authRedirecting = false
    }
  }, 150)
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 8000,
  ...(useMock ? { adapter: mockAdapter } : {})
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token && !shouldSkipToken(config.url)) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (successCodes.has(payload?.code)) {
      return payload.data
    }
    if (payload?.code === 401 || isAuthExpiredMessage(payload?.message || '')) {
      handleAuthExpired()
      return Promise.reject(new Error('登录状态已过期，请重新登录'))
    }
    ElMessage.error(payload?.message || 'Request failed')
    return Promise.reject(new Error(payload?.message || 'Request Error'))
  },
  (error) => {
    const message = error?.response?.data?.message || error.message || 'Network Error'
    if (error?.response?.status === 401 || isAuthExpiredMessage(message)) {
      handleAuthExpired()
      return Promise.reject(new Error('登录状态已过期，请重新登录'))
    }
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  }
)

export default request
