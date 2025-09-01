import { createPinia } from 'pinia'
import { useAuthStore } from './auth'

// 创建Pinia实例
const pinia = createPinia()

// 导出store实例
export default pinia

// 导出所有store
export {
  useAuthStore
}
