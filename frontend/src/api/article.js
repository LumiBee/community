import request from './config'

/**
 * 文章相关API
 */
export const articleAPI = {
  /**
   * 获取首页文章列表（分页）
   * @param {number} page - 页码，默认1
   * @param {number} size - 每页大小，默认8
   */
  getHomeArticles(page = 1, size = 8) {
    return request({
      url: '/api/home',
      method: 'get',
      params: { page, size }
    })
  },

  /**
   * 根据slug获取文章详情
   * @param {string} slug - 文章slug
   */
  getArticleBySlug(slug) {
    return request({
      url: `/api/article/${slug}`,
      method: 'get'
    }).catch(error => {
      console.error(`获取文章失败 [${slug}]:`, error)
      if (error.status === 404) {
        return { status: 404 }
      }
      throw error
    })
  },

  /**
   * 获取热门文章
   * @param {number} limit - 限制数量，默认6
   */
  getPopularArticles(limit = 6) {
    return request({
      url: '/api/articles/popular',
      method: 'get',
      params: { limit }
    })
  },

  /**
   * 获取精选文章
   */
  getFeaturedArticles() {
    return request({
      url: '/api/articles/featured',
      method: 'get'
    })
  },

  /**
   * 获取相关文章
   * @param {number} articleId - 文章ID
   * @param {number} limit - 限制数量，默认6
   */
  getRelatedArticles(articleId, limit = 6) {
    return request({
      url: `/api/article/${articleId}/related`,
      method: 'get',
      params: { limit }
    }).catch(error => {
      console.error(`获取相关文章失败 [${articleId}]:`, error)
      return []
    })
  },

  /**
   * 搜索文章
   * @param {string} query - 搜索关键词
   */
  searchArticles(query) {
    return request({
      url: '/api/search',
      method: 'get',
      params: { query }
    })
  },

  /**
   * 切换文章点赞状态
   * @param {number} articleId - 文章ID
   */
  toggleLike(articleId) {
    return request({
      url: `/api/article/${articleId}/like`,
      method: 'post'
    })
  },

  /**
   * 发布文章
   * @param {Object} articleData - 文章数据
   */
  publishArticle(articleData) {
    return request({
      url: '/api/articles',
      method: 'post',
      data: articleData
    })
  },

  /**
   * 更新文章
   * @param {number} articleId - 文章ID
   * @param {Object} articleData - 文章数据
   */
  updateArticle(articleId, articleData) {
    return request({
      url: `/api/articles/${articleId}`,
      method: 'put',
      data: articleData
    })
  },

  /**
   * 删除文章
   * @param {number} articleId - 文章ID
   */
  deleteArticle(articleId) {
    return request({
      url: `/api/articles/${articleId}`,
      method: 'delete'
    })
  },

  /**
   * 保存草稿
   * @param {Object} draftData - 草稿数据
   */
  saveDraft(draftData) {
    return request({
      url: '/api/drafts',
      method: 'post',
      data: draftData
    })
  },

  /**
   * 获取草稿列表
   */
  getDrafts() {
    return request({
      url: '/api/drafts',
      method: 'get'
    })
  },

  /**
   * 根据ID获取草稿
   * @param {number} draftId - 草稿ID
   */
  getDraftById(draftId) {
    return request({
      url: `/api/drafts/${draftId}`,
      method: 'get'
    })
  },

  /**
   * 删除草稿
   * @param {number} draftId - 草稿ID
   */
  deleteDraft(draftId) {
    return request({
      url: `/api/drafts/${draftId}`,
      method: 'delete'
    })
  }
}
