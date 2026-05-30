export const TOKEN_KEY = 'token'
export const USER_ID_KEY = 'userId'
export const USERNAME_KEY = 'username'
export const ROLE_CODE_KEY = 'roleCode'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setAuth({ token, userId, username, roleCode, role_code, roleId, role_id }) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  if (userId !== undefined && userId !== null) localStorage.setItem(USER_ID_KEY, String(userId))
  if (username) localStorage.setItem(USERNAME_KEY, username)
  const normalizedRoleCode = roleCode ?? role_code ?? roleId ?? role_id
  if (normalizedRoleCode !== undefined && normalizedRoleCode !== null && normalizedRoleCode !== '') {
    localStorage.setItem(ROLE_CODE_KEY, String(normalizedRoleCode))
  }
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_ID_KEY)
  localStorage.removeItem(USERNAME_KEY)
  localStorage.removeItem(ROLE_CODE_KEY)
}

export function getAuthSnapshot() {
  return {
    token: getToken(),
    userId: localStorage.getItem(USER_ID_KEY) || '',
    username: localStorage.getItem(USERNAME_KEY) || '',
    roleCode: localStorage.getItem(ROLE_CODE_KEY) || ''
  }
}

export function isAdminByRole(role) {
  if (!role) return false
  return String(role).trim().toUpperCase() === 'ROLE_ADMIN'
}

export function isAdminFromAuth() {
  const { roleCode } = getAuthSnapshot()
  return isAdminByRole(roleCode)
}
