import request from './config'

/**
 * 认证相关API
 */
export const authAPI = {
  /**
   * 用户登录
   * @param {Object} loginData - 登录数据
   * @param {string} loginData.username - 用户名
   * @param {string} loginData.password - 密码
   */
  login(loginData) {
    return request({
      url: '/api/login', // 使用新的API登录端点
      method: 'post',
      data: loginData,
      headers: {
        'Content-Type': 'application/json' // 使用JSON格式
      }
    })
  },

  /**
   * 用户注册
   * @param {Object} signupData - 注册数据
   * @param {string} signupData.username - 用户名
   * @param {string} signupData.email - 邮箱
   * @param {string} signupData.password - 密码
   * @param {string} signupData.confirmPassword - 确认密码
   */
  register(signupData) {
    return request({
      url: '/api/signup',
      method: 'post',
      data: signupData,
      headers: {
        'Content-Type': 'application/json'
      }
    })
  },

  /**
   * 用户登出
   */
  logout() {
    return request({
      url: '/api/logout',
      method: 'post'
    })
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser() {
    return request({
      url: '/api/user/current',
      method: 'get'
    })
  },

  /**
   * 检查用户是否已登录
   */
  checkAuth() {
    return request({
      url: '/api/auth/check',
      method: 'get'
    })
  },

  /**
   * 刷新token
   */
  refreshToken() {
    return request({
      url: '/api/auth/refresh',
      method: 'post'
    })
  },

  /**
   * 忽略密码设置提示
   */
  dismissPasswordPrompt() {
    return request({
      url: '/api/user/dismiss-password-prompt',
      method: 'post'
    })
  }
}
