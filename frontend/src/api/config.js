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
    
    // 添加CSRF token（从cookie中获取）
    const cookies = document.cookie.split(';')
    const xsrfCookie = cookies.find(cookie => cookie.trim().startsWith('XSRF-TOKEN='))
    if (xsrfCookie) {
      const xsrfToken = xsrfCookie.split('=')[1]
      config.headers['X-XSRF-TOKEN'] = decodeURIComponent(xsrfToken)
      console.log('添加CSRF令牌:', xsrfToken)
    } else {
      console.log('未找到CSRF令牌')
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
    console.log('响应接收：', response.status, response.config.url, response.data)
    // 直接返回响应数据，不做额外处理
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
          // 未登录或认证过期，或者用户名密码错误
          console.error('认证失败：', data)
          localStorage.removeItem('token')
          // 对于登录请求，我们不重定向，而是在页面上显示错误信息
          if (error.config.url.includes('/login')) {
            return Promise.reject({
              status,
              message: '用户名或密码错误',
              data
            })
          }
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
