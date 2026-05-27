const TOKEN_KEY = 'road-bike-token'
const USER_KEY = 'road-bike-user'
const USER_CHANGE_EVENT = 'road-bike-user-changed'
const ROLE_MAP = {
  admin: 'admin',
  user: 'user',
  staff: 'staff',
  ADMIN: 'admin',
  USER: 'user',
  STAFF: 'staff'
}

export function normalizeRole(role) {
  return ROLE_MAP[role] || 'user'
}

function decodeJwtPayload(token) {
  const parts = token.split('.')
  if (parts.length !== 3) {
    return null
  }

  try {
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const normalized = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')
    const payload = atob(normalized)
    return JSON.parse(payload)
  } catch {
    return null
  }
}

function isTokenExpired(token) {
  if (!token) {
    return true
  }

  const payload = decodeJwtPayload(token)
  if (!payload || typeof payload.exp !== 'number') {
    return true
  }

  return payload.exp <= Math.floor(Date.now() / 1000)
}

export function getToken() {
  const token = localStorage.getItem(TOKEN_KEY) || ''
  if (!token) {
    return ''
  }

  if (isTokenExpired(token)) {
    clearAuthSession()
    return ''
  }

  return token
}

export function getUser() {
  if (!getToken()) {
    return null
  }

  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }

  const user = JSON.parse(raw)
  return {
    ...user,
    role: normalizeRole(user?.role)
  }
}

export function setAuthSession(session) {
  const payload = {
    token: session?.token || '',
    user: {
      ...(session?.user || {}),
      role: normalizeRole(session?.user?.role)
    }
  }

  localStorage.setItem(TOKEN_KEY, payload.token)
  localStorage.setItem(USER_KEY, JSON.stringify(payload.user))
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent(USER_CHANGE_EVENT, { detail: payload.user }))
  }
}

export function clearAuthSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent(USER_CHANGE_EVENT, { detail: null }))
  }
}

export function updateCurrentUser(patch = {}) {
  const currentUser = getUser()
  if (!currentUser) {
    return null
  }

  const nextUser = {
    ...currentUser,
    ...patch,
    role: normalizeRole(patch?.role || currentUser.role)
  }

  localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent(USER_CHANGE_EVENT, { detail: nextUser }))
  }
  return nextUser
}

export function getUserChangeEventName() {
  return USER_CHANGE_EVENT
}

export function getHomePathByRole(role) {
  const normalizedRole = normalizeRole(role)

  if (normalizedRole === 'admin') {
    return '/admin/vehicles'
  }
  if (normalizedRole === 'staff') {
    return '/staff/trades'
  }
  return '/user/home'
}
