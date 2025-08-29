<template>
  <div class="image-cropper-modal" v-if="visible" @click.self="closeModal">
    <div class="cropper-container">
      <div class="cropper-header">
        <h5>裁剪封面图片</h5>
        <button class="btn-close" @click="closeModal">
          <i class="fas fa-times"></i>
        </button>
      </div>
      
      <div class="cropper-content">
        <div class="cropper-preview">
          <vue-cropper
            ref="cropper"
            :src="imageSrc"
            :aspect-ratio="3/1"
            :view-mode="2"
            :auto-crop-area="0.8"
            :background="true"
            :movable="true"
            :zoomable="true"
            :scalable="true"
            :rotatable="true"
            :center="true"
            :highlight="false"
            :crop-box-movable="true"
            :crop-box-resizable="true"
            :toggle-drag-mode-on-dblclick="false"
            :min-crop-box-width="200"
            :min-crop-box-height="100"
            :ready="onCropperReady"
            :crop="onCrop"
            class="cropper"
          />
        </div>
        
        <div class="cropper-controls">
          <div class="control-group">
            <label>缩放</label>
            <input 
              type="range" 
              min="0.1" 
              max="3" 
              step="0.1" 
              v-model="zoom" 
              @input="setZoom"
              class="zoom-slider"
            >
            <span class="zoom-value">{{ Math.round(zoom * 100) }}%</span>
          </div>
          
          <div class="control-group">
            <label>旋转</label>
            <button class="btn btn-outline-secondary btn-sm" @click="rotate(-90)">
              <i class="fas fa-undo"></i> 向左
            </button>
            <button class="btn btn-outline-secondary btn-sm" @click="rotate(90)">
              向右 <i class="fas fa-redo"></i>
            </button>
          </div>
          
          <div class="control-group">
            <label>重置</label>
            <button class="btn btn-outline-warning btn-sm" @click="reset">
              <i class="fas fa-refresh"></i> 重置
            </button>
          </div>
        </div>
      </div>
      
      <div class="cropper-footer">
        <button class="btn btn-secondary" @click="closeModal">
          取消
        </button>
        <button class="btn btn-primary" @click="cropImage" :disabled="!isReady">
          <i class="fas fa-crop"></i> 确认裁剪
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { VueCropper } from 'vue-cropper'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  imageSrc: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['close', 'crop'])

const cropper = ref(null)
const zoom = ref(1)
const isReady = ref(false)

const onCropperReady = () => {
  isReady.value = true
  console.log('裁剪器准备就绪')
}

const onCrop = (event) => {
  // 裁剪事件处理
}

const setZoom = () => {
  if (cropper.value) {
    cropper.value.zoomTo(zoom.value)
  }
}

const rotate = (degree) => {
  if (cropper.value) {
    cropper.value.rotate(degree)
  }
}

const reset = () => {
  if (cropper.value) {
    cropper.value.reset()
    zoom.value = 1
  }
}

const cropImage = () => {
  if (cropper.value) {
    cropper.value.getCropData((data) => {
      emit('crop', data)
      closeModal()
    })
  }
}

const closeModal = () => {
  emit('close')
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    isReady.value = false
    zoom.value = 1
  }
})
</script>

<style>
/* vue-cropper 基本样式 */
.vue-cropper {
  width: 100%;
  height: 100%;
}

.vue-cropper .cropper-container {
  width: 100%;
  height: 100%;
}

.vue-cropper .cropper-wrap-box {
  width: 100%;
  height: 100%;
}

.vue-cropper .cropper-canvas {
  width: 100%;
  height: 100%;
}

.vue-cropper .cropper-view-box {
  outline: 1px solid #fff;
  outline-color: rgba(255, 255, 255, 0.75);
}

.vue-cropper .cropper-face {
  background-color: inherit;
}

.vue-cropper .cropper-line {
  background-color: #fff;
}

.vue-cropper .cropper-point {
  background-color: #fff;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.vue-cropper .cropper-center {
  background-color: #fff;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.vue-cropper .cropper-dashed {
  border: 0 dashed #fff;
}

.vue-cropper .cropper-modal {
  background-color: rgba(0, 0, 0, 0.5);
}

.vue-cropper .cropper-view-box {
  outline: 1px solid #fff;
  outline-color: rgba(255, 255, 255, 0.75);
}

.vue-cropper .cropper-face {
  background-color: inherit;
}

.vue-cropper .cropper-line {
  background-color: #fff;
}

.vue-cropper .cropper-point {
  background-color: #fff;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.vue-cropper .cropper-center {
  background-color: #fff;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.vue-cropper .cropper-dashed {
  border: 0 dashed #fff;
}

.vue-cropper .cropper-modal {
  background-color: rgba(0, 0, 0, 0.5);
}

/* 组件样式 */
</style>

<style scoped>
.image-cropper-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.cropper-container {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 900px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.cropper-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e9ecef;
}

.cropper-header h5 {
  margin: 0;
  font-weight: 600;
  color: #343a40;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.2rem;
  color: #6c757d;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.btn-close:hover {
  background: #f8f9fa;
  color: #343a40;
}

.cropper-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.cropper-preview {
  flex: 1;
  min-height: 400px;
  position: relative;
  background: #f8f9fa;
}

.cropper {
  width: 100%;
  height: 100%;
}

.cropper-controls {
  padding: 20px 24px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.control-group {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.control-group:last-child {
  margin-bottom: 0;
}

.control-group label {
  font-weight: 600;
  color: #495057;
  min-width: 60px;
}

.zoom-slider {
  flex: 1;
  max-width: 200px;
}

.zoom-value {
  font-weight: 600;
  color: #6c757d;
  min-width: 50px;
  text-align: right;
}

.cropper-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e9ecef;
  background: #f8f9fa;
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(45deg, #f6d55c, #f3a712);
  border: none;
  color: white;
}

.btn-primary:hover {
  background: linear-gradient(45deg, #f3a712, #f6d55c);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(246, 213, 92, 0.3);
}

.btn-secondary {
  background: #6c757d;
  border: none;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
  transform: translateY(-2px);
}

.btn-outline-secondary {
  border-color: #6c757d;
  color: #6c757d;
}

.btn-outline-secondary:hover {
  background: #6c757d;
  border-color: #6c757d;
  color: white;
}

.btn-outline-warning {
  border-color: #ffc107;
  color: #856404;
}

.btn-outline-warning:hover {
  background: #ffc107;
  border-color: #ffc107;
  color: #212529;
}

@media (max-width: 768px) {
  .cropper-container {
    max-width: 95vw;
    max-height: 95vh;
  }
  
  .cropper-preview {
    min-height: 300px;
  }
  
  .control-group {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .zoom-slider {
    max-width: 100%;
  }
}
</style>
