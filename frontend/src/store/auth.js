import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { authAPI } from '@/api'
import { getSafeUserFromStorage, setSafeUserToStorage, debugId } from '@/utils/bigint-helper'

// 本地存储键名
const AUTH_USER_KEY = 'hive_auth_user'

export const useAuthStore = defineStore('auth', () => {
  // 尝试从本地存储中恢复用户信息，使用安全的JSON解析
  const storedUser = getSafeUserFromStorage(AUTH_USER_KEY)
  
  // 状态
  const user = ref(storedUser)
  const isLoading = ref(false)
  const error = ref(null)
  
  // 计算属性
  const isAuthenticated = computed(() => !!user.value)
  const userName = computed(() => user.value?.name || '')
  const userAvatar = computed(() => user.value?.avatarUrl || '')
  
  // Token自动刷新定时器
  let tokenRefreshTimer = null

  // 启动token自动刷新
  const startTokenRefresh = () => {
    if (tokenRefreshTimer) {
      clearInterval(tokenRefreshTimer)
    }
    
    // 每6小时检查一次token是否需要刷新
    tokenRefreshTimer = setInterval(async () => {
      if (user.value?.token) {
        try {
          await refreshToken()
        } catch (error) {
          console.error('自动刷新token失败:', error)
        }
      }
    }, 6 * 60 * 60 * 1000) // 6小时
  }

  // 停止token自动刷新
  const stopTokenRefresh = () => {
    if (tokenRefreshTimer) {
      clearInterval(tokenRefreshTimer)
      tokenRefreshTimer = null
    }
  }

  // 方法
  const setUser = (userData) => {
    // 调试用户ID
    if (userData && userData.id) {
      debugId(userData.id, '用户ID');
    }
    
    user.value = userData
    error.value = null
    
    // 将用户信息保存到本地存储，实现页面刷新后的登录状态保持
    if (userData) {
      setSafeUserToStorage(userData, AUTH_USER_KEY)
      // 启动token自动刷新
      startTokenRefresh()
    } else {
      localStorage.removeItem(AUTH_USER_KEY)
      // 停止token自动刷新
      stopTokenRefresh()
    }
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
  
  // 检查token是否需要刷新
  const shouldRefreshToken = async () => {
    if (!user.value || !user.value.token) {
      return false
    }
    
    try {
      // 解析JWT token获取过期时间
      const token = user.value.token
      const payload = JSON.parse(atob(token.split('.')[1]))
      const expirationTime = payload.exp * 1000 // 转换为毫秒
      const currentTime = Date.now()
      const timeUntilExpiry = expirationTime - currentTime
      
      // 如果剩余时间少于1天（86400000毫秒），则需要刷新
      const refreshThreshold = 24 * 60 * 60 * 1000 // 1天
      return timeUntilExpiry < refreshThreshold
    } catch (error) {
      console.error('检查token过期时间失败:', error)
      return false
    }
  }

  // 刷新token
  const refreshToken = async () => {
    if (!user.value || !user.value.token) {
      return false
    }
    
    try {
      const response = await authAPI.refreshToken()
      
      if (response && response.success) {
        // 更新用户信息和token
        const updatedUser = { ...user.value, ...response.user, token: response.token }
        setUser(updatedUser)
        return true
      } else {
        // 检查是否是"不需要刷新"的情况
        if (response?.remainingTimeMinutes !== undefined && response?.remainingTimeMinutes > 0) {
          // 这种情况不算失败，返回true
          return true
        }
        
        console.warn('Token刷新失败:', response?.message || '响应格式不正确')
        return false
      }
    } catch (err) {
      console.error('Token刷新失败:', err)
      
      // 如果是401或403错误，说明token已过期，清除用户状态
      if (err.status === 401 || err.status === 403) {
        console.log('Token已过期，清除用户状态')
        setUser(null)
      }
      
      return false
    }
  }

  // 检查认证状态
  const checkAuthStatus = async () => {
    // 如果已经在检查中，避免重复调用
    if (isLoading.value) {
      return user.value !== null
    }
    
    try {
      setLoading(true)
      clearError()
      
      // 检查是否有用户信息在内存中
      if (user.value && user.value.token) {
        // 验证token是否有效
        try {
          const response = await authAPI.getCurrentUser()
          if (response) {
            // 更新用户信息，确保token是最新的
            const updatedUser = {
              ...user.value,
              ...response,
              token: response.token || user.value.token // 保持现有token或使用新的
            }
            setUser(updatedUser)
            return true
          }
        } catch (tokenErr) {
          console.error('Token验证失败:', tokenErr)
          // 如果token验证失败，尝试刷新token
          if (tokenErr.status === 401) {
            const refreshSuccess = await refreshToken()
            if (refreshSuccess) {
              return true
            }
          }
        }
      }
      
      // 尝试从后端获取当前用户信息
      const response = await authAPI.getCurrentUser()
      if (response) {
        // 确保响应中包含token
        const userData = {
          ...response,
          token: response.token || (user.value ? user.value.token : null)
        }
        setUser(userData)
        return true
      } else {
        // 如果响应为空，清除用户信息
        user.value = null
        return false
      }
    } catch (err) {
      console.error('检查认证状态失败:', err)
      
      // 如果是401错误，说明用户未登录，这是正常的
      if (err.status === 401) {
        user.value = null
        localStorage.removeItem(AUTH_USER_KEY)
        return false
      }
      
      // 其他错误，清除用户信息
      user.value = null
      return false
    } finally {
      setLoading(false)
    }
  }
  
  // 登录
  const login = async (credentials) => {
    try {
      setLoading(true)
      clearError()
      
      // 创建登录数据对象，使用与后端匹配的参数名称
      const loginData = {
        account: credentials.account, // 这里使用account，与SecurityConfig中配置的usernameParameter一致
        password: credentials.password,
        'remember-me': credentials.rememberMe ? 'on' : ''
      }
      
      try {
        // 尝试登录
        const response = await authAPI.login(loginData)
        
        // 检查响应
        if (response && response.success) {
          
          // 清除认证检查标记，确保下次可以正常检查
          sessionStorage.removeItem('authChecked')
          
          // 直接使用响应中的用户信息，不存储临时token
          if (response.user) {
            // 确保用户信息包含token
            const userData = {
              ...response.user,
              token: response.user.token || response.token // 兼容不同的响应格式
            }
            
            // 保存用户信息到store和localStorage
            setUser(userData)
            
            // 如果不是记住我，则设置会话结束时清除标志
            if (!credentials.rememberMe) {
              // 设置sessionStorage标记，表示这是一个临时会话登录
              sessionStorage.setItem('temp_session', 'true')
              console.log('临时会话登录，页面关闭后将清除登录状态')
            } else {
              // 如果是记住我，则移除临时会话标记
              sessionStorage.removeItem('temp_session')
              console.log('记住我登录，登录状态将被保留')
            }
            
            return true
          } else {
            console.error('登录成功但未返回用户信息')
            setError('登录成功但未返回用户信息')
            return false
          }
        } else {
          console.error('登录请求失败:', response)
          setError(response?.message || '登录失败，请稍后重试')
          return false
        }
      } catch (loginErr) {
        console.error('登录请求失败:', loginErr)
        
        if (loginErr.status === 401) {
          setError('用户名或密码错误')
        } else {
          setError(loginErr.message || '登录失败，请稍后重试')
        }
        return false
      }
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
      
      // 使用API模块中的register方法
      const response = await authAPI.register({
        username: signupData.username,
        email: signupData.email,
        password: signupData.password,
        confirmPassword: signupData.confirmPassword
      })

      
      if (response && response.success) {
        return true
      } else {
        // 处理后端返回的特定错误
        if (response && response.errors) {
          // 将所有错误信息保存，以便前端可以显示在对应字段下方
          const fieldErrors = {}
          
          if (response.errors.username) {
            fieldErrors.username = response.errors.username
            setError('用户名: ' + response.errors.username)
          } 
          if (response.errors.email) {
            fieldErrors.email = response.errors.email
            setError('邮箱: ' + response.errors.email)
          } 
          if (response.errors.password) {
            fieldErrors.password = response.errors.password
            setError('密码: ' + response.errors.password)
          } 
          if (response.errors.confirmPassword) {
            fieldErrors.confirmPassword = response.errors.confirmPassword
            setError('确认密码: ' + response.errors.confirmPassword)
          }
          
          // 如果有任何字段错误，返回这些错误
          if (Object.keys(fieldErrors).length > 0) {
            return { success: false, fieldErrors }
          } else {
            setError(response.message || '注册失败，请稍后重试')
          }
        } else {
          setError(response?.message || '注册失败，请稍后重试')
        }
        return false
      }
    } catch (err) {
      console.error('注册错误:', err)
      
      // 检查是否有响应数据
      if (err.data && err.data.errors) {
        const fieldErrors = {}
        
        if (err.data.errors.username) {
          fieldErrors.username = err.data.errors.username
        }
        if (err.data.errors.email) {
          fieldErrors.email = err.data.errors.email
        }
        
        if (Object.keys(fieldErrors).length > 0) {
          return { success: false, fieldErrors }
        }
      }
      
      const errorMsg = err.message || '注册失败，请稍后重试'
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
      
      const response = await authAPI.logout()

    } catch (err) {
      console.error('登出失败:', err)
    } finally {
      // 无论API调用是否成功，都要清除本地状态
      // 使用setUser(null)来清除用户状态和本地存储
      setUser(null)
      // 清除认证检查标记
      sessionStorage.removeItem('authChecked')
      setLoading(false)
    }
  }
  
  // 更新用户信息
  const updateUserProfile = async (profileData) => {
    try {
      setLoading(true)
      clearError()
      
      // 直接更新本地用户信息，不调用API
      if (user.value) {
        const updatedUser = { ...user.value, ...profileData }
        setUser(updatedUser)
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
    shouldRefreshToken,
    refreshToken,
    startTokenRefresh,
    stopTokenRefresh,
    login,
    register,
    logout,
    updateUserProfile
  }
})
