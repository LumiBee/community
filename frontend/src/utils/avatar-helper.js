/**
 * 头像URL处理工具函数
 */

/**
 * 获取头像URL，添加时间戳避免缓存
 * @param {string} avatarUrl - 原始头像URL
 * @returns {string} 处理后的头像URL
 */
export const getAvatarUrl = (avatarUrl) => {
  if (!avatarUrl) {
    return '/img/default01.jpg'
  }
  
  // 如果是默认头像，直接返回
  if (avatarUrl === '/img/default01.jpg' || avatarUrl === '/img/default.jpg') {
    return avatarUrl
  }
  
  // 添加时间戳参数避免缓存
  const timestamp = new Date().getTime()
  const separator = avatarUrl.includes('?') ? '&' : '?'
  return `${avatarUrl}${separator}t=${timestamp}`
}

/**
 * 获取作者头像URL
 * @param {string} avatarUrl - 原始头像URL
 * @returns {string} 处理后的头像URL
 */
export const getAuthorAvatarUrl = (avatarUrl) => {
  if (!avatarUrl) {
    return '/img/default01.jpg'
  }
  
  // 如果是默认头像，直接返回
  if (avatarUrl === '/img/default01.jpg' || avatarUrl === '/img/default.jpg') {
    return avatarUrl
  }
  
  // 添加时间戳参数避免缓存
  const timestamp = new Date().getTime()
  const separator = avatarUrl.includes('?') ? '&' : '?'
  return `${avatarUrl}${separator}t=${timestamp}`
}
