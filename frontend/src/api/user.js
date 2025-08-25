import request from './config'

/**
 * 用户相关API
 */
export const userAPI = {
  /**
   * 获取用户个人资料
   * @param {string} username - 用户名
   * @param {number} page - 页码，默认1
   * @param {number} size - 每页大小，默认6
   */
  getProfile(username, page = 1, size = 6) {
    return request({
      url: `/api/profile/${username}`,
      method: 'get',
      params: { page, size }
    })
  },

  /**
   * 根据用户名获取用户信息
   * @param {string} username - 用户名
   */
  getUserByUsername(username) {
    return request({
      url: `/api/users/${username}`,
      method: 'get'
    })
  },

  /**
   * 更新用户资料
   * @param {Object} userData - 用户数据
   */
  updateProfile(userData) {
    return request({
      url: '/api/user/profile',
      method: 'put',
      data: userData
    })
  },

  /**
   * 上传头像
   * @param {FormData} formData - 包含头像文件的表单数据
   */
  uploadAvatar(formData) {
    return request({
      url: '/api/user/avatar',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 关注/取消关注用户
   * @param {number} userId - 用户ID
   */
  toggleFollow(userId) {
    return request({
      url: `/api/user/${userId}/follow`,
      method: 'post'
    })
  },

  /**
   * 获取用户的关注者列表
   * @param {number} userId - 用户ID
   */
  getFollowers(userId) {
    return request({
      url: `/api/users/${userId}/followers`,
      method: 'get'
    })
  },

  /**
   * 获取用户的关注列表
   * @param {number} userId - 用户ID
   */
  getFollowing(userId) {
    return request({
      url: `/api/users/${userId}/following`,
      method: 'get'
    })
  },

  /**
   * 获取用户的文章列表
   * @param {number} userId - 用户ID
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   */
  getUserArticles(userId, page = 1, size = 6) {
    return request({
      url: `/api/users/${userId}/articles`,
      method: 'get',
      params: { page, size }
    })
  },

  /**
   * 更改密码
   * @param {Object} passwordData - 密码数据
   * @param {string} passwordData.oldPassword - 旧密码
   * @param {string} passwordData.newPassword - 新密码
   */
  changePassword(passwordData) {
    return request({
      url: '/api/user/password',
      method: 'put',
      data: passwordData
    })
  },

  /**
   * 获取用户统计信息
   * @param {number} userId - 用户ID
   */
  getUserStats(userId) {
    return request({
      url: `/api/users/${userId}/stats`,
      method: 'get'
    })
  },

  /**
   * 检查是否关注了某用户
   * @param {number} userId - 用户ID
   */
  isFollowing(userId) {
    return request({
      url: `/api/user/${userId}/is-following`,
      method: 'get'
    })
  }
}
