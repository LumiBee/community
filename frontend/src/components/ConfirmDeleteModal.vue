<template>
  <div v-if="visible" class="confirm-delete-overlay" @click="handleOverlayClick">
    <div class="confirm-delete-modal" :style="modalPosition" @click.stop>
      <!-- 头部 -->
      <div class="modal-header">
        <div class="warning-icon">
          <i class="fas fa-exclamation-triangle"></i>
        </div>
        <h5 class="modal-title">{{ title }}</h5>
      </div>
      
      <!-- 内容区域 -->
      <div class="modal-body">
        <p class="confirm-message">{{ message }}</p>
        <p class="warning-message">{{ warningMessage }}</p>
      </div>
      
      <!-- 底部按钮 -->
      <div class="modal-footer">
        <button type="button" class="btn btn-cancel" @click="handleCancel">
          {{ cancelText }}
        </button>
        <button type="button" class="btn btn-confirm" @click="handleConfirm">
          {{ confirmText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, computed, watch } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '确认删除'
  },
  message: {
    type: String,
    default: '您确定要删除这个项目吗？'
  },
  warningMessage: {
    type: String,
    default: '此操作无法撤销！'
  },
  cancelText: {
    type: String,
    default: '取消'
  },
  confirmText: {
    type: String,
    default: '确认删除'
  }
})

const emit = defineEmits(['confirm', 'cancel', 'close'])

// 弹窗位置始终居中
const modalPosition = computed(() => {
  if (!props.visible) {
    return {}
  }
  
  // 始终居中显示
  return {
    top: '50%',
    left: '50%'
  }
})

const handleConfirm = () => {
  emit('confirm')
}

const handleCancel = () => {
  emit('cancel')
}

// 监听弹窗显示状态，控制页面滚动
watch(() => props.visible, (newVisible) => {
  if (newVisible) {
    // 弹窗显示时禁用页面滚动
    document.body.style.overflow = 'hidden'
  } else {
    // 弹窗隐藏时恢复页面滚动
    document.body.style.overflow = ''
  }
})

const handleOverlayClick = () => {
  emit('close')
}
</script>

<style scoped>
.confirm-delete-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  z-index: 1050;
  pointer-events: auto;
}

.confirm-delete-modal {
  position: fixed;
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-width: 400px;
  width: 100%;
  transform: translate(-50%, -50%);
  animation: modalSlideIn 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform, opacity;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}

.modal-header {
  display: flex;
  align-items: center;
  padding: 1.5rem 1.5rem 0.5rem 1.5rem;
  gap: 0.75rem;
}

.warning-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #ffc107, #ff9800);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.warning-icon i {
  color: white;
  font-size: 1.2rem;
}

.modal-title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a202c;
  line-height: 1.4;
}

.modal-body {
  padding: 0.5rem 1.5rem 1.5rem 1.5rem;
}

.confirm-message {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  color: #2d3748;
  line-height: 1.5;
}

.warning-message {
  margin: 0;
  font-size: 0.875rem;
  color: #e53e3e;
  font-weight: 500;
  line-height: 1.4;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: 0 1.5rem 1.5rem 1.5rem;
}

.btn {
  padding: 0.625rem 1.25rem;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 80px;
}

.btn-cancel {
  background-color: #f7fafc;
  color: #4a5568;
  border: 1px solid #e2e8f0;
}

.btn-cancel:hover {
  background-color: #edf2f7;
  color: #2d3748;
}

.btn-confirm {
  background-color: #e53e3e;
  color: white;
}

.btn-confirm:hover {
  background-color: #c53030;
}

.btn-confirm:active {
  transform: translateY(1px);
}

.btn-cancel:active {
  transform: translateY(1px);
}
</style>
