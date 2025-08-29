import axios from 'axios'
import { useAuthStore } from '@/store/auth'
import { getSafeUserFromStorage, debugId } from '@/utils/bigint-helper'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8090', // 后端API地址
  timeout: 30000, // 请求超时时间增加到30秒
  withCredentials: true, // 允许携带cookie（用于Spring Security会话）
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  },
  // 添加重试配置
  retry: 3, // 重试次数
  retryDelay: 1000 // 重试间隔（毫秒）
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加CSRF token（从cookie中获取）
    const cookies = document.cookie.split(';')
    const xsrfCookie = cookies.find(cookie => cookie.trim().startsWith('XSRF-TOKEN='))
    if (xsrfCookie) {
      const xsrfToken = xsrfCookie.split('=')[1]
      config.headers['X-XSRF-TOKEN'] = decodeURIComponent(xsrfToken)
      
      // 对于POST、PUT、DELETE请求，确保设置了CSRF token
      if (['post', 'put', 'delete'].includes(config.method.toLowerCase())) {
        console.log(`添加CSRF令牌到${config.method.toUpperCase()}请求:`, xsrfToken)
      }
    } else {
      // 对于POST、PUT、DELETE请求，如果没有CSRF token，尝试从服务器获取
      if (['post', 'put', 'delete'].includes(config.method.toLowerCase())) {
        console.warn(`未找到CSRF令牌，${config.method.toUpperCase()}请求可能会被拒绝`)
      }
    }
    
    // 从本地存储中获取用户信息，如果存在则添加认证头
    const storedUser = getSafeUserFromStorage()
    
    if (storedUser) {
      try {
        if (storedUser && storedUser.token) {
          config.headers['Authorization'] = `Bearer ${storedUser.token}`
        } else if (storedUser && storedUser.id) {
          // 如果没有token但有用户ID，可能是旧的存储格式，尝试刷新用户信息
          console.warn('用户信息中没有token，但有用户ID:', storedUser.id)
          debugId(storedUser.id, 'API请求中的用户ID')
        } else {
          console.warn('用户信息中没有token:', storedUser)
        }
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    } else {
      console.warn('本地存储中没有用户信息')
    }
    
    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 直接返回响应数据，不做额外处理
    return response.data
  },
  async error => {
    console.error('响应错误：', error)
    
    // 实现请求重试逻辑
    const config = error.config;
    
    // 如果配置了重试，且未设置重试计数器，则初始化计数器
    if (config && config.retry && !config._retryCount) {
      config._retryCount = 0;
    }
    
    // 检查是否可以重试
    if (config && config.retry && config._retryCount < config.retry) {
      config._retryCount++;
      
      console.log(`请求重试中 (${config._retryCount}/${config.retry}): ${config.url}`);
      
      // 创建新的Promise来处理重试延迟
      return new Promise(resolve => {
        setTimeout(() => {
          console.log(`重试请求: ${config.url}`);
          resolve(axios(config));
        }, config.retryDelay || 1000);
      });
    }
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 400:
          console.error('请求参数错误：', data)
          break
        case 401:
          // 未登录或认证过期
          console.error('认证失败：', data)
          
          // 对于登录请求，我们不重定向，而是在页面上显示错误信息
          if (error.config.url.includes('/login')) {
            return Promise.reject({
              status,
              message: '用户名或密码错误',
              data
            })
          }
          
          // 对于其他请求，尝试刷新token
          if (!error.config.url.includes('/auth/refresh')) {
            try {
              const authStore = useAuthStore()
              const refreshSuccess = await authStore.refreshToken()
              
              if (refreshSuccess) {
                // 刷新成功，重试原请求
                console.log('Token刷新成功，重试原请求')
                const originalRequest = error.config
                // 更新请求头中的token
                const storedUser = localStorage.getItem('hive_auth_user')
                if (storedUser) {
                  const user = JSON.parse(storedUser)
                  if (user && user.token) {
                    originalRequest.headers['Authorization'] = `Bearer ${user.token}`
                  }
                }
                return axios(originalRequest)
              }
            } catch (refreshError) {
              console.error('Token刷新失败:', refreshError)
            }
          }
          break
        case 403:
          console.error('权限不足：', data)
          
          // 对于403错误，可能是CSRF令牌问题，尝试刷新CSRF令牌并重试
          if (!error.config.url.includes('/auth/refresh') && !error.config._skipRetry) {
            try {
              // 尝试重新获取CSRF令牌
              const cookies = document.cookie.split(';')
              const xsrfCookie = cookies.find(cookie => cookie.trim().startsWith('XSRF-TOKEN='))
              
              if (xsrfCookie) {
                const xsrfToken = xsrfCookie.split('=')[1]
                const originalRequest = error.config
                originalRequest.headers = originalRequest.headers || {}
                originalRequest.headers['X-XSRF-TOKEN'] = decodeURIComponent(xsrfToken)
                console.log('更新CSRF令牌并重试请求:', xsrfToken)
                // 标记请求已重试，避免无限循环
                originalRequest._skipRetry = true
                return axios(originalRequest)
              }
            } catch (csrfError) {
              console.error('CSRF令牌刷新失败:', csrfError)
            }
          }
          break
        case 404:
          console.error('请求的资源不存在：', data)
          break
        case 500:
          console.error('服务器内部错误：', data)
          break
        default:
          console.error(`HTTP错误 ${status}：`, data)
      }
      
      // 返回格式化的错误信息
      return Promise.reject({
        status,
        message: data?.message || `HTTP错误 ${status}`,
        data
      })
    } else if (error.request) {
      // 网络错误
      console.error('网络错误：', error.message)
      return Promise.reject({
        status: 0,
        message: '网络连接失败，请检查您的网络',
        data: null
      })
    } else {
      // 其他错误
      console.error('未知错误：', error.message)
      return Promise.reject({
        status: -1,
        message: error.message || '未知错误',
        data: null
      })
    }
  }
)

export default request
