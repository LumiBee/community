import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI } from '@/api'

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
      clearError() // 清除之前的错误
      
      // 检查是否有token
      const token = localStorage.getItem('token')
      if (!token) {
        user.value = null
        return false
      }
      
      const response = await authAPI.getCurrentUser()
      if (response) {
        setUser(response)
        return true
      } else {
        // 如果响应为空，清除token和用户信息
        localStorage.removeItem('token')
        user.value = null
        return false
      }
    } catch (err) {
      console.error('检查认证状态失败:', err)
      // 如果获取用户信息失败，说明未登录或token无效
      localStorage.removeItem('token')
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
          
          // 登录成功后存储临时token
          localStorage.setItem('token', 'temp-token')
          
          // 直接使用响应中的用户信息
          if (response.user) {
            setUser(response.user)
            console.log('登录成功，用户信息:', response.user)
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
        
        // 登录失败，清除可能存在的token
        localStorage.removeItem('token')
        
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
      await authAPI.logout()
    } catch (err) {
      console.error('Logout error:', err)
    } finally {
      // 清除用户信息和token
      user.value = null
      localStorage.removeItem('token')
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
    login,
    register,
    logout,
    updateUserProfile
  }
})
