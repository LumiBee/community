<template>
  <div class="simple-cropper-modal" v-if="visible" @click.self="closeModal">
    <div class="cropper-container">
      <div class="cropper-header">
        <h5>裁剪封面图片</h5>
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
              class="crop-box"
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
              <span class="preview-ratio">4:1</span>
            </div>
            <div class="preview-container">
              <canvas 
                ref="previewCanvas"
                class="preview-canvas"
                width="300"
                height="75"
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

// 裁剪框位置和大小
const cropBox = ref({
  x: 0,
  y: 0,
  width: 300,
  height: 100
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
      
      // 设置初始裁剪框，确保在图片范围内
      // 根据封面区域的实际显示比例调整裁剪框大小
      const containerWidth = img.parentElement.clientWidth
      const containerHeight = img.parentElement.clientHeight
      
      // 计算4:1比例的裁剪框大小
      const maxCropWidth = Math.min(containerWidth * 0.8, 600) // 最大600px
      const maxCropHeight = maxCropWidth / 4
      
      const cropWidth = Math.min(maxCropWidth, imgRect.width * 0.8)
      const cropHeight = cropWidth / 4
      
      cropBox.value = {
        x: imgDisplayX + (imgRect.width - cropWidth) / 2,
        y: imgDisplayY + (imgRect.height - cropHeight) / 2,
        width: cropWidth,
        height: cropHeight
      }
      
      showCropBox.value = true
      console.log('图片加载完成，设置初始裁剪框:', cropBox.value)
      
      // 延迟更新预览，确保DOM已更新
      setTimeout(() => {
        updatePreview()
      }, 200)
    }, 100)
  }
}

const updateScale = () => {
  if (imageRef.value) {
    imageRef.value.style.transform = `scale(${scale.value}) rotate(${rotation.value}deg)`
  }
  updatePreview()
}

const rotate = (degree) => {
  rotation.value += degree
  updateScale()
}

const reset = () => {
  scale.value = 1
  rotation.value = 0
  updateScale()
  if (imageRef.value) {
    const img = imageRef.value
    const imgRect = img.getBoundingClientRect()
    const containerRect = img.parentElement.getBoundingClientRect()
    
    const imgDisplayX = imgRect.left - containerRect.left
    const imgDisplayY = imgRect.top - containerRect.top
    
    // 计算4:1比例的裁剪框大小
    const maxCropWidth = Math.min(img.parentElement.clientWidth * 0.8, 600)
    const cropWidth = Math.min(maxCropWidth, imgRect.width * 0.8)
    const cropHeight = cropWidth / 4
    
    cropBox.value = {
      x: imgDisplayX + (imgRect.width - cropWidth) / 2,
      y: imgDisplayY + (imgRect.height - cropHeight) / 2,
      width: cropWidth,
      height: cropHeight
    }
  }
}

const startDrag = (event) => {
  isDragging.value = true
  dragStart.value = {
    x: event.clientX - cropBox.value.x,
    y: event.clientY - cropBox.value.y
  }
  
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

const onDrag = (event) => {
  if (!isDragging.value) return
  
  cropBox.value.x = event.clientX - dragStart.value.x
  cropBox.value.y = event.clientY - dragStart.value.y
  
  // 限制在图片范围内
  if (imageRef.value) {
    const imgRect = imageRef.value.getBoundingClientRect()
    const containerRect = imageRef.value.parentElement.getBoundingClientRect()
    
    const imgDisplayX = imgRect.left - containerRect.left
    const imgDisplayY = imgRect.top - containerRect.top
    
    const maxX = imgDisplayX + imgRect.width - cropBox.value.width
    const maxY = imgDisplayY + imgRect.height - cropBox.value.height
    
    cropBox.value.x = Math.max(imgDisplayX, Math.min(cropBox.value.x, maxX))
    cropBox.value.y = Math.max(imgDisplayY, Math.min(cropBox.value.y, maxY))
  }
}

const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  updatePreview()
}

const startResize = (handle) => {
  isResizing.value = true
  resizeHandle.value = handle
  dragStart.value = {
    x: event.clientX,
    y: event.clientY
  }
  
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
}

const onResize = (event) => {
  if (!isResizing.value) return
  
  const deltaX = event.clientX - dragStart.value.x
  const deltaY = event.clientY - dragStart.value.y
  
  switch (resizeHandle.value) {
    case 'se':
      const newWidthSE = Math.max(100, cropBox.value.width + deltaX)
      cropBox.value.width = newWidthSE
      cropBox.value.height = newWidthSE / 4 // 保持4:1比例
      break
    case 'sw':
      const newWidthSW = Math.max(100, cropBox.value.width - deltaX)
      cropBox.value.width = newWidthSW
      cropBox.value.height = newWidthSW / 4 // 保持4:1比例
      cropBox.value.x += deltaX
      break
    case 'ne':
      const newWidthNE = Math.max(100, cropBox.value.width + deltaX)
      cropBox.value.width = newWidthNE
      cropBox.value.height = newWidthNE / 4 // 保持4:1比例
      cropBox.value.y += deltaY
      break
    case 'nw':
      const newWidthNW = Math.max(100, cropBox.value.width - deltaX)
      cropBox.value.width = newWidthNW
      cropBox.value.height = newWidthNW / 4 // 保持4:1比例
      cropBox.value.x += deltaX
      cropBox.value.y += deltaY
      break
  }
  
  dragStart.value = {
    x: event.clientX,
    y: event.clientY
  }
}

const stopResize = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
  updatePreview()
}

const cropImage = () => {
  // 创建canvas进行裁剪
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  const img = imageRef.value
  
  // 使用统一的裁剪计算函数
  const cropArea = calculateCropArea()
  if (!cropArea) return
  
  console.log('最终裁剪计算:', cropArea)
  
  // 设置canvas尺寸为裁剪框大小
  canvas.width = cropBox.value.width
  canvas.height = cropBox.value.height
  
  // 绘制裁剪后的图片
  ctx.drawImage(
    img,
    cropArea.cropX,
    cropArea.cropY,
    cropArea.cropWidth,
    cropArea.cropHeight,
    0,
    0,
    cropBox.value.width,
    cropBox.value.height
  )
  
  // 转换为base64
  const croppedData = canvas.toDataURL('image/jpeg', 0.9)
  emit('crop', croppedData)
  closeModal()
}

const updatePreview = () => {
  if (!imageRef.value || !previewCanvas.value || !showCropBox.value) return
  
  const canvas = previewCanvas.value
  const ctx = canvas.getContext('2d')
  const img = imageRef.value
  
  // 清空画布
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  
  try {
    // 使用统一的裁剪计算函数
    const cropArea = calculateCropArea()
    if (!cropArea) return
    
    console.log('预览计算:', cropArea)
    
    // 绘制预览
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
  } catch (error) {
    console.warn('预览更新失败:', error)
  }
}

const closeModal = () => {
  emit('close')
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    scale.value = 1
    rotation.value = 0
    showCropBox.value = false
  }
})
</script>

<style scoped>
.simple-cropper-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  z-index: 9999;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 20px;
  padding-top: 40px;
}

.cropper-container {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 900px;
  max-height: 85vh;
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

.cropper-main {
  flex: 1;
  display: flex;
  gap: 20px;
  padding: 20px;
  overflow: hidden;
}

.image-preview {
  flex: 1;
  min-height: 400px;
  position: relative;
  background: #f8f9fa;
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.3s ease;
  transform-origin: center center;
}

.crop-box {
  position: absolute;
  border: 2px solid #fff;
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.5);
  cursor: move;
  pointer-events: auto;
  z-index: 10;
}

.crop-handle {
  position: absolute;
  width: 10px;
  height: 10px;
  background: #fff;
  border: 1px solid #333;
  border-radius: 50%;
}

.crop-handle-nw {
  top: -5px;
  left: -5px;
  cursor: nw-resize;
}

.crop-handle-ne {
  top: -5px;
  right: -5px;
  cursor: ne-resize;
}

.crop-handle-sw {
  bottom: -5px;
  left: -5px;
  cursor: sw-resize;
}

.crop-handle-se {
  bottom: -5px;
  right: -5px;
  cursor: se-resize;
}

/* 预览窗口样式 */
.preview-window {
  width: 320px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.preview-header {
  padding: 12px 16px;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
  border-radius: 8px 8px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-header h6 {
  margin: 0;
  font-weight: 600;
  color: #495057;
  font-size: 0.9rem;
}

.preview-ratio {
  background: #f6d55c;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.preview-container {
  flex: 1;
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 0 0 8px 8px;
}

.preview-canvas {
  border: 1px solid #dee2e6;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  background: white;
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

.scale-slider {
  flex: 1;
  max-width: 200px;
}

.scale-value {
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
  .simple-cropper-modal {
    padding-top: 20px;
  }
  
  .cropper-container {
    max-width: 95vw;
    max-height: 90vh;
  }
  
  .cropper-main {
    flex-direction: column;
    gap: 16px;
    padding: 16px;
  }
  
  .image-preview {
    min-height: 300px;
  }
  
  .preview-window {
    width: 100%;
    max-width: 320px;
    align-self: center;
  }
  
  .control-group {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .scale-slider {
    max-width: 100%;
  }
}
</style>
