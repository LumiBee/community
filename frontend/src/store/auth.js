import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const user = ref(null)
  const isLoading = ref(false)
  const error = ref(null)
  
  // 计算属性
  const isAuthenticated = computed(() => !!user.value)
  const userName = computed(() => user.value?.name || '')
  const userAvatar = computed(() => user.value?.avatarUrl || '')
  
  // 方法
  const setUser = (userData) => {
    user.value = userData
    error.value = null
  }
  
  const setError = (errorMessage) => {
    error.value = errorMessage
  }
  
  const clearError = () => {
    error.value = null
  }
  
  const setLoading = (loading) => {
    isLoading.value = loading
  }
  
  // 检查认证状态
  const checkAuthStatus = async () => {
    try {
      setLoading(true)
      const response = await api.get('/api/user/profile')
      if (response.data) {
        setUser(response.data)
      }
    } catch (err) {
      // 如果获取用户信息失败，说明未登录
      user.value = null
    } finally {
      setLoading(false)
    }
  }
  
  // 登录
  const login = async (credentials) => {
    try {
      setLoading(true)
      clearError()
      
      // 创建表单数据
      const formData = new FormData()
      formData.append('account', credentials.account)
      formData.append('password', credentials.password)
      if (credentials.rememberMe) {
        formData.append('remember-me', 'on')
      }
      
      const response = await api.post('/login-process', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      
      // 登录成功后获取用户信息
      await checkAuthStatus()
      
      return true
    } catch (err) {
      const errorMsg = err.response?.data?.message || '登录失败，请检查用户名和密码'
      setError(errorMsg)
      return false
    } finally {
      setLoading(false)
    }
  }
  
  // 注册
  const register = async (signupData) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await api.post('/signup', signupData)
      return true
    } catch (err) {
      const errorMsg = err.response?.data?.message || '注册失败，请稍后重试'
      setError(errorMsg)
      return false
    } finally {
      setLoading(false)
    }
  }
  
  // 登出
  const logout = async () => {
    try {
      setLoading(true)
      await api.post('/logout')
    } catch (err) {
      console.error('Logout error:', err)
    } finally {
      user.value = null
      setLoading(false)
    }
  }
  
  // 更新用户信息
  const updateUserProfile = async (profileData) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await api.put('/api/user/profile', profileData)
      if (response.data) {
        setUser(response.data)
      }
      return true
    } catch (err) {
      const errorMsg = err.response?.data?.message || '更新用户信息失败'
      setError(errorMsg)
      return false
    } finally {
      setLoading(false)
    }
  }
  
  return {
    // 状态
    user,
    isLoading,
    error,
    
    // 计算属性
    isAuthenticated,
    userName,
    userAvatar,
    
    // 方法
    setUser,
    setError,
    clearError,
    setLoading,
    checkAuthStatus,
    login,
    register,
    logout,
    updateUserProfile
  }
})
