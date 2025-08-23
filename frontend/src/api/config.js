import axios from 'axios'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8090', // 后端API地址
  timeout: 15000, // 请求超时时间
  withCredentials: true, // 允许携带cookie（用于Spring Security会话）
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从localStorage获取token（如果使用JWT）
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 添加CSRF token（如果启用了CSRF保护）
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content')
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content')
    if (csrfToken && csrfHeader) {
      config.headers[csrfHeader] = csrfToken
    }
    
    console.log('请求发送：', config.method.toUpperCase(), config.url)
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
    console.log('响应接收：', response.status, response.config.url)
    return response.data
  },
  error => {
    console.error('响应错误：', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 400:
          console.error('请求参数错误：', data)
          break
        case 401:
          // 未登录或认证过期
          console.error('未授权访问，请重新登录')
          localStorage.removeItem('token')
          // 可以在这里添加重定向到登录页的逻辑
          // window.location.href = '/login'
          break
        case 403:
          console.error('权限不足：', data)
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
