import request from './config'

/**
 * 作品集相关API
 */
export const portfolioAPI = {
  /**
   * 获取所有作品集
   */
  getAllPortfolios() {
    return request({
      url: '/api/portfolios',
      method: 'get'
    })
  },

  /**
   * 根据ID获取作品集详情
   * @param {number} portfolioId - 作品集ID
   */
  getPortfolioById(portfolioId) {
    return request({
      url: `/api/portfolio/${portfolioId}`,
      method: 'get'
    })
  },

  /**
   * 创建作品集
   * @param {Object} portfolioData - 作品集数据
   */
  createPortfolio(portfolioData) {
    return request({
      url: '/api/portfolio',
      method: 'post',
      data: portfolioData
    })
  },

  /**
   * 更新作品集
   * @param {number} portfolioId - 作品集ID
   * @param {Object} portfolioData - 作品集数据
   */
  updatePortfolio(portfolioId, portfolioData) {
    return request({
      url: `/api/portfolio/${portfolioId}`,
      method: 'put',
      data: portfolioData
    })
  },

  /**
   * 删除作品集
   * @param {number} portfolioId - 作品集ID
   */
  deletePortfolio(portfolioId) {
    return request({
      url: `/api/portfolio/${portfolioId}`,
      method: 'delete'
    })
  },

  /**
   * 获取用户的作品集列表
   * @param {number} userId - 用户ID
   */
  getUserPortfolios(userId) {
    return request({
      url: `/api/users/${userId}/portfolios`,
      method: 'get'
    })
  }
}
