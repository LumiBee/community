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
      url: '/login',
      method: 'post',
      data: loginData,
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      transformRequest: [function (data) {
        // 将对象转换为表单格式，因为Spring Security默认期望表单数据
        let ret = ''
        for (let key in data) {
          ret += encodeURIComponent(key) + '=' + encodeURIComponent(data[key]) + '&'
        }
        return ret.slice(0, -1)
      }]
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
      url: '/signup',
      method: 'post',
      data: signupData,
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      transformRequest: [function (data) {
        let ret = ''
        for (let key in data) {
          ret += encodeURIComponent(key) + '=' + encodeURIComponent(data[key]) + '&'
        }
        return ret.slice(0, -1)
      }]
    })
  },

  /**
   * 用户登出
   */
  logout() {
    return request({
      url: '/logout',
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
   * 忽略密码设置提示
   */
  dismissPasswordPrompt() {
    return request({
      url: '/api/user/dismiss-password-prompt',
      method: 'post'
    })
  }
}
