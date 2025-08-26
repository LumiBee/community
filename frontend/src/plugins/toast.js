import { ref } from 'vue'

// 全局toast状态管理
const toasts = ref([])
let toastId = 0

// Toast方法
const showToast = (message, type = 'success', duration = 3000) => {
  const id = ++toastId
  const toast = {
    id,
    message,
    type,
    duration
  }
  
  toasts.value.push(toast)
  
  // 自动移除
  setTimeout(() => {
    removeToast(id)
  }, duration)
}

const removeToast = (id) => {
  const index = toasts.value.findIndex(toast => toast.id === id)
  if (index > -1) {
    toasts.value.splice(index, 1)
  }
}

// 创建toast方法对象
const toastMethods = {
  success: (message, duration) => showToast(message, 'success', duration),
  error: (message, duration) => showToast(message, 'error', duration),
  warning: (message, duration) => showToast(message, 'warning', duration),
  info: (message, duration) => showToast(message, 'info', duration)
}

// 创建toast插件
export default {
  install(app) {
    // 将toast状态和方法挂载到app实例上
    app.config.globalProperties.$toast = toastMethods
    
    // 同时挂载到window对象，方便在非Vue组件中使用
    window.$toast = toastMethods
    
    // 将toasts状态挂载到app实例上，供Toast组件使用
    app.config.globalProperties.$toasts = toasts
    
    console.log('Toast plugin installed successfully')
  }
}

// 导出toasts状态，供Toast组件使用
export { toasts }
