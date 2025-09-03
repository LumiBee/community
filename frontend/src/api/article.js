import request from './config'
import { useAuthStore } from '@/store/auth'

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
      url: '/home',
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
      url: `/article/${slug}`,
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
   * 根据ID获取文章详情
   * @param {number} articleId - 文章ID
   */
  getArticleById(articleId) {
    return request({
      url: `/article/id/${articleId}`,
      method: 'get'
    }).catch(error => {
      console.error(`获取文章失败 [${articleId}]:`, error)
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
      url: '/articles/popular',
      method: 'get',
      params: { limit }
    })
  },

  /**
   * 获取精选文章
   */
  getFeaturedArticles() {
    return request({
      url: '/articles/featured',
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
      url: `/article/${articleId}/related`,
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
      url: '/search',
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
      url: `/article/${articleId}/like`,
      method: 'post'
    })
  },

  /**
   * 发布文章
   * @param {Object} articleData - 文章数据
   */
  publishArticle(articleData) {
    return request({
      url: '/article/publish',
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
      url: `/article/${articleId}/edit`,
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
      url: `/article/delete/${articleId}`,
      method: 'delete'
    })
  },

  /**
   * 保存草稿
   * @param {Object} draftData - 草稿数据
   */
  async saveDraft(draftData) {
    console.log('API调用: saveDraft', draftData)
    
    // 准备请求数据，确保字段名与后端DTO匹配
    const requestData = {
      title: draftData.title,
      content: draftData.content,
      excerpt: draftData.excerpt || '',
      tagsName: draftData.tags || [],
      portfolioName: draftData.portfolioName || null
    };
    
    // 如果有草稿ID，则添加到请求数据中（用于更新现有草稿）
    if (draftData.id) {
      requestData.articleId = draftData.id;
      console.log('更新现有草稿，articleId:', draftData.id);
    } else {
      console.log('创建新草稿');
    }
    
    console.log('请求数据:', requestData);
    
    // 检查token是否需要刷新（只有在即将过期时才刷新）
    const authStore = useAuthStore()
    if (authStore.user?.token) {
      try {
        // 只有在token即将过期时才尝试刷新
        const shouldRefresh = await authStore.shouldRefreshToken()
        if (shouldRefresh) {
          console.log('token即将过期，尝试刷新...')
          const refreshSuccess = await authStore.refreshToken()
          if (!refreshSuccess) {
            console.warn('token刷新失败，可能需要重新登录')
            throw new Error('登录状态已过期，请重新登录后再保存草稿')
          }
          console.log('token刷新成功')
        }
      } catch (refreshError) {
        console.warn('token刷新失败:', refreshError)
        // 如果token刷新失败，直接返回错误，不继续执行
        throw new Error('Token刷新失败，无法保存草稿')
      }
    }
    
    // 调用API
    const response = await request({
      url: '/article/save-draft',
      method: 'post',
      data: requestData
    });
    
    console.log('保存草稿API响应:', response);
    return response;
  },

  /**
   * 获取草稿列表（使用文章API，通过status过滤）
   */
  getDrafts() {
    return request({
      url: '/article/drafts',
      method: 'get'
    })
  },

  /**
   * 根据ID获取草稿（使用文章详情API）
   * @param {number} draftId - 草稿ID
   */
  getDraftById(draftId) {
    return this.getArticleById(draftId)
  },

  /**
   * 删除草稿（使用文章删除API）
   * @param {number} draftId - 草稿ID
   */
  deleteDraft(draftId) {
    return this.deleteArticle(draftId)
  }
}
