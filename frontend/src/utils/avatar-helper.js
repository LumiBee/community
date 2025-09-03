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
  
  // 如果是完整的后端URL，转换为相对路径以使用Vite代理
  if (avatarUrl.startsWith('http://localhost:8090/')) {
    avatarUrl = avatarUrl.replace('http://localhost:8090', '')
  }
  
  // 如果是相对路径的uploads，需要添加/api前缀（因为后端设置了全局API前缀）
  if (avatarUrl.startsWith('/uploads/')) {
    avatarUrl = '/api' + avatarUrl
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
  
  // 如果是完整的后端URL，转换为相对路径以使用Vite代理
  if (avatarUrl.startsWith('http://localhost:8090/')) {
    avatarUrl = avatarUrl.replace('http://localhost:8090', '')
  }
  
  // 如果是相对路径的uploads，需要添加/api前缀（因为后端设置了全局API前缀）
  if (avatarUrl.startsWith('/uploads/')) {
    avatarUrl = '/api' + avatarUrl
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
