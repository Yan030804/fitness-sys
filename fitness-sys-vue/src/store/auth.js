import { defineStore } from 'pinia'
import * as authApi from '../api/auth'
import { clearAuth, getAuthSnapshot, setAuth } from '../utils/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getAuthSnapshot().token,
    userId: getAuthSnapshot().userId,
    username: getAuthSnapshot().username,
    roleCode: getAuthSnapshot().roleCode,
    currentUser: null
  }),
  actions: {
    async login(payload) {
      const data = await authApi.login(payload)
      setAuth(data)
      const normalizedRoleCode = data.roleCode ?? data.role_code ?? data.roleId ?? data.role_id ?? ''
      this.token = data.token || ''
      this.userId = data.userId !== undefined && data.userId !== null ? String(data.userId) : ''
      this.username = data.username || ''
      this.roleCode = normalizedRoleCode ? String(normalizedRoleCode) : ''
      return data
    },
    async register(payload) {
      return authApi.register(payload)
    },
    async logout() {
      try {
        await authApi.logout()
      } finally {
        clearAuth()
        this.token = ''
        this.userId = ''
        this.username = ''
        this.roleCode = ''
        this.currentUser = null
      }
    },
    async fetchCurrentUser() {
      const data = await authApi.getCurrentUser()
      this.currentUser = data
      
      // Sync auth info from /me endpoint to resolve refresh issues
      if (data) {
        const normalizedRoleCode = data.roleCode ?? data.role_code ?? data.roleId ?? data.role_id ?? ''
        this.roleCode = normalizedRoleCode ? String(normalizedRoleCode) : ''
        this.username = data.username || this.username
        this.userId = data.id !== undefined ? String(data.id) : (data.userId !== undefined ? String(data.userId) : this.userId)
        
        // Update local storage via setAuth (but don't overwrite token since /me doesn't return it)
        setAuth({
          token: this.token,
          userId: this.userId,
          username: this.username,
          roleCode: this.roleCode
        })
      }
      
      return data
    }
  }
})
