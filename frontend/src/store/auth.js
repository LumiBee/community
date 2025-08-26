import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { authAPI } from '@/api'

// 本地存储键名
const AUTH_USER_KEY = 'hive_auth_user'

export const useAuthStore = defineStore('auth', () => {
  // 尝试从本地存储中恢复用户信息
  const storedUser = localStorage.getItem(AUTH_USER_KEY)
  
  // 状态
  const user = ref(storedUser ? JSON.parse(storedUser) : null)
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
    
    // 每30分钟检查一次token是否需要刷新
    tokenRefreshTimer = setInterval(async () => {
      if (user.value?.token) {
        try {
          console.log('检查token是否需要刷新...')
          await refreshToken()
        } catch (error) {
          console.error('自动刷新token失败:', error)
        }
      }
    }, 30 * 60 * 1000) // 30分钟
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
    user.value = userData
    error.value = null
    
    // 将用户信息保存到本地存储，实现页面刷新后的登录状态保持
    if (userData) {
      localStorage.setItem(AUTH_USER_KEY, JSON.stringify(userData))
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
  
  // 刷新token
  const refreshToken = async () => {
    if (!user.value || !user.value.token) {
      return false
    }
    
    try {
      console.log('尝试刷新token...')
      const response = await authAPI.refreshToken()
      
      if (response && response.success) {
        // 更新用户信息和token
        const updatedUser = { ...user.value, ...response.user, token: response.token }
        setUser(updatedUser)
        console.log('Token刷新成功')
        return true
      } else {
        console.warn('Token刷新失败:', response?.message)
        return false
      }
    } catch (err) {
      console.error('Token刷新失败:', err)
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
            setUser({...user.value, ...response})
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
        if (!response.token && user.value && user.value.token) {
          response.token = user.value.token
        }
        setUser(response)
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
      
      console.log('发送登录请求，数据:', {
        account: credentials.account,
        passwordLength: credentials.password?.length,
        rememberMe: credentials.rememberMe
      })
      
      try {
        // 尝试登录
        const response = await authAPI.login(loginData)
        
        // 检查响应
        if (response && response.success) {
          console.log('登录请求成功，获取到用户信息')
          
          // 清除认证检查标记，确保下次可以正常检查
          sessionStorage.removeItem('authChecked')
          
          // 直接使用响应中的用户信息，不存储临时token
          if (response.user) {
            // 保存用户信息到store和localStorage
            setUser(response.user)
            console.log('登录成功，用户信息:', response.user)
            
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
      
      console.log('开始注册请求，数据:', {
        username: signupData.username,
        email: signupData.email,
        passwordLength: signupData.password?.length
      })
      
      // 使用API模块中的register方法
      const response = await authAPI.register({
        username: signupData.username,
        email: signupData.email,
        password: signupData.password,
        confirmPassword: signupData.confirmPassword
      })
      
      console.log('注册响应:', response)
      
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
      console.log('开始登出...')
      
      const response = await authAPI.logout()
      console.log('登出API响应:', response)
      
      if (response && response.success) {
        console.log('登出成功')
      } else {
        console.warn('登出响应异常:', response)
      }
    } catch (err) {
      console.error('登出失败:', err)
    } finally {
      // 无论API调用是否成功，都要清除本地状态
      console.log('清除本地用户状态')
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
      
      // 这里需要使用userAPI，但我们目前使用authAPI
      // 如果有专门的userAPI，应该使用userAPI.updateProfile
      const response = await authAPI.getCurrentUser() // 临时使用，实际应该是更新API
      if (response) {
        setUser({...response, ...profileData})
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
    refreshToken,
    startTokenRefresh,
    stopTokenRefresh,
    login,
    register,
    logout,
    updateUserProfile
  }
})
