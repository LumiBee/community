// API统一导出文件

// 导出axios实例（用于直接使用）
export { default as request } from './config'

// 导出各个模块的API
export { authAPI } from './auth'
export { articleAPI } from './article'
export { userAPI } from './user'
export { portfolioAPI } from './portfolio'
export { tagAPI } from './tag'
export { favoriteAPI } from './favorite'
export { aiAPI } from './ai'
export { cacheAPI } from './cache'

// 导入所有API模块
import { authAPI } from './auth'
import { articleAPI } from './article'
import { userAPI } from './user'
import { portfolioAPI } from './portfolio'
import { tagAPI } from './tag'
import { favoriteAPI } from './favorite'
import { aiAPI } from './ai'
import { cacheAPI } from './cache'

// 默认导出所有API（用于统一导入）
export default {
  auth: authAPI,
  article: articleAPI,
  user: userAPI,
  portfolio: portfolioAPI,
  tag: tagAPI,
  favorite: favoriteAPI,
  ai: aiAPI,
  cache: cacheAPI
}