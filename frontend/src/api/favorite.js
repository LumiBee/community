import request from './config'

/**
 * 收藏相关API
 */
export const favoriteAPI = {
  /**
   * 获取用户的收藏列表
   * @param {number} page - 页码，默认1
   * @param {number} size - 每页大小，默认10
   */
  getFavorites(page = 1, size = 10) {
    return request({
      url: '/api/favorites/my-folders',
      method: 'get',
      params: { page, size }
    })
  },

  /**
   * 收藏/取消收藏文章
   * @param {number} articleId - 文章ID
   */
  toggleFavorite(articleId) {
    return request({
      url: `/api/articles/${articleId}/favorite`,
      method: 'post'
    })
  },

  /**
   * 检查文章是否已收藏
   * @param {number} articleId - 文章ID
   */
  isFavorited(articleId) {
    return request({
      url: `/api/articles/${articleId}/is-favorited`,
      method: 'get'
    })
  },

  /**
   * 获取文章的收藏数量
   * @param {number} articleId - 文章ID
   */
  getFavoriteCount(articleId) {
    return request({
      url: `/api/articles/${articleId}/favorite-count`,
      method: 'get'
    })
  },

  /**
   * 创建收藏夹
   * @param {Object} favoriteData - 收藏夹数据
   * @param {string} favoriteData.name - 收藏夹名称
   * @param {string} favoriteData.description - 收藏夹描述
   */
  createFavoriteFolder(favoriteData) {
    return request({
      url: '/api/favorites/create-folder',
      method: 'post',
      data: favoriteData
    })
  },

  /**
   * 获取用户的收藏夹列表
   */
  getFavoriteFolders() {
    return request({
      url: '/api/favorites/my-folders',
      method: 'get'
    })
  },

  /**
   * 获取收藏夹详情
   * @param {number} folderId - 收藏夹ID
   */
  getFavoriteFolderById(folderId) {
    return request({
      url: `/api/favorites/details/${folderId}`,
      method: 'get'
    })
  },

  /**
   * 更新收藏夹
   * @param {number} folderId - 收藏夹ID
   * @param {Object} favoriteData - 收藏夹数据
   */
  updateFavoriteFolder(folderId, favoriteData) {
    // 注意：后端没有直接的更新接口，这里使用创建接口
    return request({
      url: '/api/favorites/create-folder',
      method: 'post',
      data: {
        favoriteId: folderId,
        favoriteName: favoriteData.name,
        description: favoriteData.description
      }
    })
  },

  /**
   * 删除收藏夹
   * @param {number} folderId - 收藏夹ID
   */
  deleteFavoriteFolder(folderId) {
    return request({
      url: `/api/favorites/remove-folder/${folderId}`,
      method: 'delete'
    })
  },

  /**
   * 将文章添加到收藏夹
   * @param {number} folderId - 收藏夹ID
   * @param {number} articleId - 文章ID
   */
  addToFolder(folderId, articleId) {
    return request({
      url: '/api/favorites/add-to-folder',
      method: 'post',
      data: { favoriteId: folderId, articleId: articleId }
    })
  },

  /**
   * 从收藏夹移除文章
   * @param {number} folderId - 收藏夹ID
   * @param {number} articleId - 文章ID
   */
  removeFromFolder(folderId, articleId) {
    return request({
      url: `/api/favorites/remove-all/${articleId}`,
      method: 'delete'
    })
  },
  
  /**
   * 从所有收藏夹中移除文章
   * @param {number} articleId - 文章ID
   */
  removeFromAllFolders(articleId) {
    return request({
      url: `/api/favorites/remove-all/${articleId}`,
      method: 'delete'
    })
  },
  
  /**
   * 创建收藏夹并添加文章
   * @param {number} articleId - 文章ID
   * @param {string} favoriteName - 收藏夹名称
   */
  createAndAdd(articleId, favoriteName) {
    return request({
      url: '/api/favorites/create-and-add',
      method: 'post',
      data: {
        articleId,
        favoriteName
      }
    })
  }
}
