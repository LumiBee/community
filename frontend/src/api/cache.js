import { request } from './index'

/**
 * 缓存管理API
 * 仅管理员可访问
 */
export const cacheAPI = {
  /**
   * 清理所有缓存
   */
  clearAllCaches() {
    return request({
      url: '/api/admin/cache/all',
      method: 'DELETE'
    })
  },

  /**
   * 清理文章相关缓存
   */
  clearArticleCaches() {
    return request({
      url: '/api/admin/cache/articles',
      method: 'DELETE'
    })
  },

  /**
   * 清理特定用户的文章缓存
   * @param {number} userId 用户ID
   */
  clearUserCaches(userId) {
    return request({
      url: `/api/admin/cache/user/${userId}`,
      method: 'DELETE'
    })
  }
}
