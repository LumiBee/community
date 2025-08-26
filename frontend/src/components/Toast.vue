<template>
  <div class="vue-toast-container" v-if="toastList.length > 0">
    <TransitionGroup name="vue-toast" tag="div" class="vue-toast-list">
      <div
        v-for="toast in toastList"
        :key="toast.id"
        :class="['vue-toast', `vue-toast-${toast.type}`]"
        @click="removeToast(toast.id)"
      >
        <div class="vue-toast-icon">
          <i :class="getIconClass(toast.type)"></i>
        </div>
        <div class="vue-toast-body">
          <div class="vue-toast-title">{{ getTitle(toast.type) }}</div>
          <div class="vue-toast-message">{{ toast.message }}</div>
        </div>
        <!-- 进度条 -->
        <div class="vue-toast-progress" :style="{ animationDuration: `${toast.duration}ms` }"></div>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { toasts } from '@/plugins/toast'

// 确保toasts是响应式的
const toastList = computed(() => toasts.value)

// 获取图标类名
const getIconClass = (type) => {
  switch (type) {
    case 'success':
      return 'fas fa-check-circle'
    case 'error':
      return 'fas fa-times-circle'
    case 'warning':
      return 'fas fa-exclamation-triangle'
    default:
      return 'fas fa-info-circle'
  }
}

// 获取标题
const getTitle = (type) => {
  switch (type) {
    case 'success':
      return '操作成功'
    case 'error':
      return '发生错误'
    case 'warning':
      return '请注意'
    default:
      return '提示'
  }
}

// 移除toast
const removeToast = (id) => {
  const index = toasts.value.findIndex(toast => toast.id === id)
  if (index > -1) {
    toasts.value.splice(index, 1)
  }
}
</script>

<style scoped>
.vue-toast-container {
  position: fixed;
  top: 80px;
  right: 20px;
  z-index: 9999; /* 确保z-index足够高 */
  pointer-events: none;
}

.vue-toast-list {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

/* 进度条样式 */
.vue-toast-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  border-radius: 0 0 12px 12px;
  animation: progressShrink linear forwards;
  transform-origin: left;
}

/* 不同类型toast的进度条颜色 */
.vue-toast-success .vue-toast-progress {
  background: linear-gradient(90deg, transparent, rgba(180, 255, 88, 0.8));
}

.vue-toast-error .vue-toast-progress {
  background: linear-gradient(90deg, transparent, rgba(220, 53, 69, 0.8));
}

.vue-toast-warning .vue-toast-progress {
  background: linear-gradient(90deg, transparent, rgba(255, 193, 7, 0.8));
}

.vue-toast-info .vue-toast-progress {
  background: linear-gradient(90deg, transparent, rgba(23, 162, 184, 0.8));
}

@keyframes progressShrink {
  from {
    width: 100%;
  }
  to {
    width: 0%;
  }
}

/* 确保toast有相对定位以便进度条定位 */
.vue-toast {
  min-width: 300px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  color: #333;
  border-radius: 12px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  padding: 12px 20px;
  border-left: 5px solid;
  pointer-events: auto;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
}

/* 添加悬停时的微动画 */
.vue-toast:hover {
  transform: translateX(-5px) scale(1.02);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
  animation: none !important; /* 悬停时暂停类型动画 */
}

/* 添加点击时的反馈动画 */
.vue-toast:active {
  transform: translateX(-3px) scale(0.98);
  transition: all 0.1s ease;
  animation: none !important; /* 点击时暂停类型动画 */
}

.vue-toast-icon {
  font-size: 1.5rem;
  margin-right: 15px;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.vue-toast:hover .vue-toast-icon {
  transform: scale(1.1) rotate(5deg);
}

.vue-toast-body {
  flex-grow: 1;
  transition: all 0.3s ease;
}

.vue-toast-title {
  font-weight: 700;
  margin-bottom: 3px;
  font-family: 'Playfair Display', Georgia, serif;
  font-size: 0.95rem;
  transition: all 0.3s ease;
}

.vue-toast:hover .vue-toast-title {
  transform: translateX(2px);
}

.vue-toast-message {
  font-size: 0.9rem;
  color: #555;
  line-height: 1.4;
  transition: all 0.3s ease;
}

.vue-toast:hover .vue-toast-message {
  transform: translateX(2px);
}

/* 根据类型设置颜色和特殊效果 */
.vue-toast-success { 
  border-color: #b4ff58; 
  animation: successPulse 2s ease-in-out infinite !important;
}
.vue-toast-success .vue-toast-icon { 
  color: #b1ff58; 
}

.vue-toast-error { 
  border-color: #dc3545; 
  animation: errorShake 0.5s ease-in-out !important;
}
.vue-toast-error .vue-toast-icon { 
  color: #dc3545; 
}

.vue-toast-warning { 
  border-color: #ffc107; 
  animation: warningGlow 2s ease-in-out infinite !important;
}
.vue-toast-warning .vue-toast-icon { 
  color: #ffc107; 
}

.vue-toast-info { 
  border-color: #17a2b8; 
  animation: infoFloat 3s ease-in-out infinite !important;
}
.vue-toast-info .vue-toast-icon { 
  color: #17a2b8; 
}

/* 特殊动画效果 */
@keyframes successPulse {
  0%, 100% { box-shadow: 0 5px 15px rgba(180, 255, 88, 0.15); }
  50% { box-shadow: 0 5px 20px rgba(180, 255, 88, 0.3); }
}

@keyframes errorShake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

@keyframes warningGlow {
  0%, 100% { box-shadow: 0 5px 15px rgba(255, 193, 7, 0.15); }
  50% { box-shadow: 0 5px 20px rgba(255, 193, 7, 0.4); }
}

@keyframes infoFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-2px); }
}

/* 动画效果 */
.vue-toast-enter-active {
  transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) !important;
}

.vue-toast-leave-active {
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1) !important;
}

.vue-toast-enter-from {
  opacity: 0 !important;
  transform: translateX(100%) scale(0.8) rotateY(-15deg) !important;
}

.vue-toast-leave-to {
  opacity: 0 !important;
  transform: translateX(100%) scale(0.8) rotateY(15deg) !important;
}

/* 添加进入时的弹性效果 */
.vue-toast-enter-active .vue-toast {
  animation: toastBounce 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) !important;
}

@keyframes toastBounce {
  0% {
    transform: translateX(100%) scale(0.8);
  }
  50% {
    transform: translateX(-10px) scale(1.05);
  }
  100% {
    transform: translateX(0) scale(1);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .vue-toast-container {
    top: 60px;
    right: 10px;
    left: 10px;
  }
  
  .vue-toast {
    min-width: auto;
    width: 100%;
  }
  
  .vue-toast-list {
    align-items: stretch;
  }
}
</style>
