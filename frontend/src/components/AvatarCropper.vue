<template>
  <div class="avatar-cropper-modal" v-if="visible" @click.self="closeModal">
    <div class="cropper-container">
      <div class="cropper-header">
        <h5>裁剪头像</h5>
        <button class="btn-close" @click="closeModal">
          <i class="fas fa-times"></i>
        </button>
      </div>
      
      <div class="cropper-content">
        <div class="cropper-main">
          <div class="image-preview">
            <img 
              :src="imageSrc" 
              ref="imageRef"
              @load="onImageLoad"
              class="preview-image"
              alt="预览图片"
            />
            <div 
              v-if="showCropBox"
              class="crop-box crop-circle"
              :style="cropBoxStyle"
              @mousedown="startDrag"
            >
              <div class="crop-handle crop-handle-nw" @mousedown.stop="startResize('nw')"></div>
              <div class="crop-handle crop-handle-ne" @mousedown.stop="startResize('ne')"></div>
              <div class="crop-handle crop-handle-sw" @mousedown.stop="startResize('sw')"></div>
              <div class="crop-handle crop-handle-se" @mousedown.stop="startResize('se')"></div>
            </div>
          </div>
          
          <!-- 预览窗口 -->
          <div class="preview-window">
            <div class="preview-header">
              <h6>实时预览</h6>
              <span class="preview-ratio">1:1</span>
            </div>
            <div class="preview-container">
              <canvas 
                ref="previewCanvas"
                class="preview-canvas"
                width="120"
                height="120"
              ></canvas>
            </div>
          </div>
        </div>
        
        <div class="cropper-controls">
          <div class="control-group">
            <label>缩放</label>
            <input 
              type="range" 
              min="0.5" 
              max="3" 
              step="0.1" 
              v-model="scale" 
              @input="updateScale"
              class="scale-slider"
            >
            <span class="scale-value">{{ Math.round(scale * 100) }}%</span>
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
        <button class="btn btn-primary" @click="cropImage" :disabled="!showCropBox">
          <i class="fas fa-crop"></i> 确认裁剪
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

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

const imageRef = ref(null)
const previewCanvas = ref(null)
const scale = ref(1)
const rotation = ref(0)
const showCropBox = ref(false)

// 裁剪框位置和大小 (1:1 正方形)
const cropBox = ref({
  x: 0,
  y: 0,
  width: 200,
  height: 200
})

// 拖拽状态
const isDragging = ref(false)
const isResizing = ref(false)
const resizeHandle = ref('')
const dragStart = ref({ x: 0, y: 0 })

const cropBoxStyle = computed(() => ({
  left: cropBox.value.x + 'px',
  top: cropBox.value.y + 'px',
  width: cropBox.value.width + 'px',
  height: cropBox.value.height + 'px'
}))

// 统一的裁剪计算函数
const calculateCropArea = () => {
  if (!imageRef.value) return null
  
  const img = imageRef.value
  const imgRect = img.getBoundingClientRect()
  const containerRect = img.parentElement.getBoundingClientRect()
  
  // 计算图片在容器中的实际位置和尺寸
  const imgDisplayWidth = imgRect.width
  const imgDisplayHeight = imgRect.height
  const imgDisplayX = imgRect.left - containerRect.left
  const imgDisplayY = imgRect.top - containerRect.top
  
  // 计算裁剪区域相对于图片的位置
  const cropX = Math.max(0, (cropBox.value.x - imgDisplayX) / imgDisplayWidth * img.naturalWidth)
  const cropY = Math.max(0, (cropBox.value.y - imgDisplayY) / imgDisplayHeight * img.naturalHeight)
  const cropWidth = Math.min(cropBox.value.width / imgDisplayWidth * img.naturalWidth, img.naturalWidth - cropX)
  const cropHeight = Math.min(cropBox.value.height / imgDisplayHeight * img.naturalHeight, img.naturalHeight - cropY)
  
  return {
    cropX,
    cropY,
    cropWidth,
    cropHeight,
    imgDisplayWidth,
    imgDisplayHeight,
    imgDisplayX,
    imgDisplayY,
    naturalWidth: img.naturalWidth,
    naturalHeight: img.naturalHeight
  }
}

const onImageLoad = () => {
  if (imageRef.value) {
    const img = imageRef.value
    const containerWidth = img.parentElement.clientWidth
    const containerHeight = img.parentElement.clientHeight
    
    // 等待图片完全加载
    setTimeout(() => {
      const imgRect = img.getBoundingClientRect()
      const containerRect = img.parentElement.getBoundingClientRect()
      
      // 计算图片在容器中的实际位置
      const imgDisplayX = imgRect.left - containerRect.left
      const imgDisplayY = imgRect.top - containerRect.top
      
      // 设置初始裁剪框，确保在图片范围内且为正方形
      const minSize = Math.min(containerWidth, containerHeight) * 0.6
      const centerX = (containerWidth - minSize) / 2
      const centerY = (containerHeight - minSize) / 2
      
      cropBox.value = {
        x: centerX,
        y: centerY,
        width: minSize,
        height: minSize
      }
      
      showCropBox.value = true
      updatePreview()
    }, 100)
  }
}

const updatePreview = () => {
  if (!previewCanvas.value || !imageRef.value || !showCropBox.value) return
  
  const canvas = previewCanvas.value
  const ctx = canvas.getContext('2d')
  const img = imageRef.value
  
  // 清空画布
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  
  // 创建圆形裁剪路径
  ctx.save()
  ctx.beginPath()
  ctx.arc(canvas.width / 2, canvas.height / 2, canvas.width / 2, 0, 2 * Math.PI)
  ctx.clip()
  
  // 计算裁剪区域
  const cropArea = calculateCropArea()
  if (!cropArea) return
  
  // 绘制裁剪后的图片
  ctx.drawImage(
    img,
    cropArea.cropX,
    cropArea.cropY,
    cropArea.cropWidth,
    cropArea.cropHeight,
    0,
    0,
    canvas.width,
    canvas.height
  )
  
  ctx.restore()
}

// 拖拽开始
const startDrag = (e) => {
  if (isResizing.value) return
  
  isDragging.value = true
  dragStart.value = {
    x: e.clientX - cropBox.value.x,
    y: e.clientY - cropBox.value.y
  }
  
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

// 拖拽中
const onDrag = (e) => {
  if (!isDragging.value) return
  
  const newX = e.clientX - dragStart.value.x
  const newY = e.clientY - dragStart.value.y
  
  // 限制在图片范围内
  const img = imageRef.value
  const imgRect = img.getBoundingClientRect()
  const containerRect = img.parentElement.getBoundingClientRect()
  
  const maxX = containerRect.width - cropBox.value.width
  const maxY = containerRect.height - cropBox.value.height
  
  cropBox.value.x = Math.max(0, Math.min(newX, maxX))
  cropBox.value.y = Math.max(0, Math.min(newY, maxY))
  
  updatePreview()
}

// 拖拽结束
const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

// 开始调整大小
const startResize = (handle) => {
  isResizing.value = true
  resizeHandle.value = handle
  dragStart.value = { x: 0, y: 0 }
  
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
}

// 调整大小中
const onResize = (e) => {
  if (!isResizing.value) return
  
  const deltaX = e.movementX
  const deltaY = e.movementY
  
  const handle = resizeHandle.value
  let newWidth = cropBox.value.width
  let newHeight = cropBox.value.height
  let newX = cropBox.value.x
  let newY = cropBox.value.y
  
  // 保持正方形比例
  const sizeChange = Math.max(Math.abs(deltaX), Math.abs(deltaY))
  const sign = (deltaX + deltaY) > 0 ? 1 : -1
  
  if (handle.includes('e')) {
    newWidth += sizeChange * sign
  }
  if (handle.includes('w')) {
    newWidth -= sizeChange * sign
    newX += sizeChange * sign
  }
  if (handle.includes('s')) {
    newHeight += sizeChange * sign
  }
  if (handle.includes('n')) {
    newHeight -= sizeChange * sign
    newY += sizeChange * sign
  }
  
  // 确保最小尺寸
  const minSize = 50
  if (newWidth >= minSize && newHeight >= minSize) {
    cropBox.value.width = newWidth
    cropBox.value.height = newWidth // 保持正方形
    cropBox.value.x = newX
    cropBox.value.y = newY
  }
  
  updatePreview()
}

// 调整大小结束
const stopResize = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
}

// 缩放
const updateScale = () => {
  if (imageRef.value) {
    imageRef.value.style.transform = `scale(${scale.value}) rotate(${rotation.value}deg)`
  }
}

// 旋转
const rotate = (angle) => {
  rotation.value += angle
  if (imageRef.value) {
    imageRef.value.style.transform = `scale(${scale.value}) rotate(${rotation.value}deg)`
  }
}

// 重置
const reset = () => {
  scale.value = 1
  rotation.value = 0
  if (imageRef.value) {
    imageRef.value.style.transform = 'scale(1) rotate(0deg)'
  }
  onImageLoad()
}

// 裁剪图片
const cropImage = () => {
  if (!imageRef.value || !showCropBox.value) return
  
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  
  // 设置画布大小为裁剪区域大小
  const cropArea = calculateCropArea()
  if (!cropArea) return
  
  canvas.width = cropArea.cropWidth
  canvas.height = cropArea.cropHeight
  
  // 创建圆形裁剪路径
  ctx.save()
  ctx.beginPath()
  ctx.arc(canvas.width / 2, canvas.height / 2, canvas.width / 2, 0, 2 * Math.PI)
  ctx.clip()
  
  // 绘制裁剪后的图片
  ctx.drawImage(
    imageRef.value,
    cropArea.cropX,
    cropArea.cropY,
    cropArea.cropWidth,
    cropArea.cropHeight,
    0,
    0,
    canvas.width,
    canvas.height
  )
  
  ctx.restore()
  
  // 转换为Blob
  canvas.toBlob((blob) => {
    emit('crop', blob)
  }, 'image/jpeg', 0.9)
}

// 关闭模态框
const closeModal = () => {
  emit('close')
}

// 监听visible变化
watch(() => props.visible, (newVal) => {
  if (newVal) {
    showCropBox.value = false
    scale.value = 1
    rotation.value = 0
  }
})
</script>

<style scoped>
.avatar-cropper-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.cropper-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  max-width: 90vw;
  max-height: 90vh;
  overflow: hidden;
}

.cropper-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
}

.cropper-header h5 {
  margin: 0;
  font-weight: 600;
  color: #333;
}

.btn-close {
  background: none;
  border: none;
  font-size: 18px;
  color: #666;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}

.btn-close:hover {
  background: #e9ecef;
  color: #333;
}

.cropper-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 24px;
}

.cropper-main {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.image-preview {
  position: relative;
  width: 400px;
  height: 400px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  overflow: hidden;
  background: #f8f9fa;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.crop-box {
  position: absolute;
  border: 2px solid #007bff;
  cursor: move;
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.5);
}

.crop-circle {
  border-radius: 50%;
  border: 2px solid #007bff;
}

.crop-handle {
  position: absolute;
  width: 12px;
  height: 12px;
  background: #007bff;
  border: 2px solid white;
  border-radius: 50%;
  cursor: pointer;
}

.crop-handle-nw {
  top: -6px;
  left: -6px;
  cursor: nw-resize;
}

.crop-handle-ne {
  top: -6px;
  right: -6px;
  cursor: ne-resize;
}

.crop-handle-sw {
  bottom: -6px;
  left: -6px;
  cursor: sw-resize;
}

.crop-handle-se {
  bottom: -6px;
  right: -6px;
  cursor: se-resize;
}

.preview-window {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  min-width: 150px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.preview-header h6 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.preview-ratio {
  font-size: 12px;
  color: #666;
  background: #e9ecef;
  padding: 2px 8px;
  border-radius: 12px;
}

.preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 8px;
}

.preview-canvas {
  border-radius: 50%;
  border: 2px solid #e9ecef;
}

.cropper-controls {
  display: flex;
  gap: 24px;
  justify-content: center;
  flex-wrap: wrap;
}

.control-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.control-group label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  min-width: 40px;
}

.scale-slider {
  width: 100px;
}

.scale-value {
  font-size: 12px;
  color: #666;
  min-width: 40px;
}

.btn {
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #dee2e6;
  background: white;
  color: #333;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn:hover {
  background: #f8f9fa;
  border-color: #adb5bd;
}

.btn-outline-secondary {
  border-color: #6c757d;
  color: #6c757d;
}

.btn-outline-secondary:hover {
  background: #6c757d;
  color: white;
}

.btn-outline-warning {
  border-color: #ffc107;
  color: #856404;
}

.btn-outline-warning:hover {
  background: #ffc107;
  color: #212529;
}

.cropper-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e9ecef;
  background: #f8f9fa;
}

.btn-secondary {
  background: #6c757d;
  border-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
  border-color: #545b62;
}

.btn-primary {
  background: #007bff;
  border-color: #007bff;
  color: white;
}

.btn-primary:hover {
  background: #0056b3;
  border-color: #0056b3;
}

.btn-primary:disabled {
  background: #6c757d;
  border-color: #6c757d;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .cropper-main {
    flex-direction: column;
  }
  
  .image-preview {
    width: 300px;
    height: 300px;
  }
  
  .cropper-controls {
    flex-direction: column;
    align-items: center;
  }
}
</style>
