import request from './config'

/**
 * 收藏相关API
 */
export const favoriteAPI = {
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
   * 创建收藏夹
   * @param {Object} favoriteData - 收藏夹数据
   * @param {string} favoriteData.favoriteName - 收藏夹名称
   */
  createFavoriteFolder(favoriteData) {
    return request({
      url: '/api/favorites/create-folder',
      method: 'post',
      data: favoriteData
    })
  },

  /**
   * 更新收藏夹名称
   * @param {number} folderId - 收藏夹ID
   * @param {Object} favoriteData - 收藏夹数据
   * @param {string} favoriteData.favoriteName - 新的收藏夹名称
   */
  updateFavoriteFolder(folderId, favoriteData) {
    return request({
      url: `/api/favorites/update-folder/${folderId}`,
      method: 'put',
      data: favoriteData
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
      data: { 
        favoriteId: folderId, 
        articleId: articleId 
      }
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
   * 从指定收藏夹中移除文章
   * @param {number} articleId - 文章ID
   * @param {number} folderId - 收藏夹ID
   */
  removeFromFolder(articleId, folderId) {
    return request({
      url: `/api/favorites/remove-from-folder/${articleId}/${folderId}`,
      method: 'delete'
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
  }
}
