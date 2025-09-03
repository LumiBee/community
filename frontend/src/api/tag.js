import request from './config'

/**
 * 标签相关API
 */
export const tagAPI = {
  /**
   * 获取所有标签
   */
  getAllTags() {
    return request({
      url: '/tags',
      method: 'get'
    })
  },

  /**
   * 根据标签ID获取文章
   * @param {number} tagId - 标签ID
   * @param {number} page - 页码，默认1
   * @param {number} size - 每页大小，默认10
   */
  getArticlesByTag(tagId, page = 1, size = 10) {
    return request({
      url: `/tags/${tagId}/articles`,
      method: 'get',
      params: { page, size }
    })
  },

  /**
   * 根据标签slug获取文章
   * @param {string} tagSlug - 标签slug
   * @param {number} page - 页码，默认1
   * @param {number} size - 每页大小，默认10
   */
  getArticlesByTagSlug(tagSlug, page = 1, size = 10) {
    return request({
      url: `/tags/${encodeURIComponent(tagSlug)}/articles`,
      method: 'get',
      params: { page, size }
    })
  },

  /**
   * 创建标签
   * @param {Object} tagData - 标签数据
   * @param {string} tagData.name - 标签名
   * @param {string} tagData.description - 标签描述
   */
  createTag(tagData) {
    return request({
      url: '/tags',
      method: 'post',
      data: tagData
    })
  },

  /**
   * 更新标签
   * @param {number} tagId - 标签ID
   * @param {Object} tagData - 标签数据
   */
  updateTag(tagId, tagData) {
    return request({
      url: `/tags/${tagId}`,
      method: 'put',
      data: tagData
    })
  },

  /**
   * 删除标签
   * @param {number} tagId - 标签ID
   */
  deleteTag(tagId) {
    return request({
      url: `/tags/${tagId}`,
      method: 'delete'
    })
  },

  /**
   * 获取热门标签
   * @param {number} limit - 限制数量，默认20
   */
  getPopularTags(limit = 20) {
    return request({
      url: '/tags/popular',
      method: 'get',
      params: { limit }
    })
  },

  /**
   * 搜索标签
   * @param {string} query - 搜索关键词
   */
  searchTags(query) {
    return request({
      url: '/tags/search',
      method: 'get',
      params: { query }
    })
  }
}
