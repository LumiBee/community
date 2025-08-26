<template>
  <Transition name="modal">
    <div v-if="visible" class="favorite-modal-overlay" @click="closeModal">
      <div class="favorite-modal" @click.stop>
        <!-- 模态框头部 -->
        <div class="modal-header">
          <div class="title-section">
            <i class="fas fa-bookmark icon-bookmark"></i>
            <h5 class="modal-title">收藏到文件夹</h5>
          </div>
          <button type="button" class="btn-close" @click="closeModal" aria-label="Close">
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <div class="modal-body">
          <p class="modal-subtitle">选择一个收藏夹或创建新的收藏夹</p>
          
          <!-- 现有收藏夹列表 -->
          <div class="existing-folders">
            <h6 class="section-title">
              <i class="fas fa-folder-open"></i>
              我的收藏夹
            </h6>
            
            <div class="folder-list" v-if="folders.length > 0">
              <div
                v-for="folder in folders"
                :key="folder.id"
                class="folder-item"
                :class="{ 'active': selectedFolderId === folder.id }"
                @click="selectFolder(folder)"
              >
                <i class="fas fa-folder folder-icon"></i>
                <span class="folder-name">{{ folder.name }}</span>
                <span class="folder-count">{{ folder.articleCount || 0 }}</span>
                <i class="fas fa-check check-icon" v-if="selectedFolderId === folder.id"></i>
              </div>
            </div>
            
            <div v-else class="empty-state">
              <i class="fas fa-folder-open"></i>
              <p>还没有收藏夹，创建一个吧！</p>
            </div>
          </div>
          
          <!-- 分割线 -->
          <div class="divider">
            <span>或</span>
          </div>
          
          <!-- 创建新收藏夹 -->
          <div class="create-new-folder">
            <h6 class="section-title">
              <i class="fas fa-plus-circle"></i>
              创建新收藏夹
            </h6>
            
            <div class="create-form">
              <div class="input-wrapper">
                <i class="fas fa-folder-plus input-icon"></i>
                <input
                  type="text"
                  class="form-input"
                  v-model="newFolderName"
                  placeholder="输入收藏夹名称"
                  @keyup.enter="createAndAdd"
                />
              </div>
              <button
                class="btn-create"
                @click="createAndAdd"
                :disabled="!newFolderName.trim() || loading"
              >
                <i class="fas fa-plus"></i>
                <span>创建并收藏</span>
              </button>
            </div>
          </div>
        </div>
        
        <!-- 模态框底部 -->
        <div class="modal-footer">
          <button type="button" class="btn-cancel" @click="closeModal">
            取消
          </button>
          <button
            type="button"
            class="btn-confirm"
            @click="addToSelectedFolder"
            :disabled="!selectedFolderId && !newFolderName.trim()"
          >
            <i class="fas fa-bookmark"></i>
            <span>收藏</span>
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch } from 'vue'
import { favoriteAPI } from '@/api'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  articleId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['close', 'success'])

// 响应式数据
const folders = ref([])
const selectedFolderId = ref(null)
const newFolderName = ref('')
const loading = ref(false)

// 监听模态框显示状态
watch(() => props.visible, async (newVisible) => {
  if (newVisible) {
    await loadFolders()
  }
})

// 加载用户收藏夹列表
const loadFolders = async () => {
  try {
    loading.value = true
    const response = await favoriteAPI.getFavoriteFolders()
    folders.value = response || []
    selectedFolderId.value = null
    newFolderName.value = ''
  } catch (error) {
    console.error('加载收藏夹失败:', error)
  } finally {
    loading.value = false
  }
}

// 选择收藏夹
const selectFolder = (folder) => {
  selectedFolderId.value = folder.id
  newFolderName.value = '' // 清空新收藏夹名称
}

// 添加到选中的收藏夹
const addToSelectedFolder = async () => {
  if (!selectedFolderId.value) {
    return
  }
  
  try {
    loading.value = true
    await favoriteAPI.addToFolder(selectedFolderId.value, props.articleId)
    emit('success', { type: 'add', folderId: selectedFolderId.value })
    closeModal()
  } catch (error) {
    console.error('添加到收藏夹失败:', error)
    alert('添加到收藏夹失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 创建新收藏夹并添加文章
const createAndAdd = async () => {
  if (!newFolderName.value.trim()) {
    return
  }
  
  try {
    loading.value = true
    await favoriteAPI.createAndAdd(props.articleId, newFolderName.value.trim())
    emit('success', { type: 'create', folderName: newFolderName.value.trim() })
    closeModal()
  } catch (error) {
    console.error('创建收藏夹失败:', error)
    alert('创建收藏夹失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 关闭模态框
const closeModal = () => {
  emit('close')
}
</script>

<style scoped>
/* 模态框动画 */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

/* 遮罩层 */
.favorite-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
  padding: 1rem;
}

/* 模态框主体 */
.favorite-modal {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  width: 100%;
  max-width: 450px;
  max-height: 85vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 模态框头部 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #f1f5f9;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.icon-bookmark {
  color: #f6d55c;
  font-size: 1.25rem;
}

.modal-title {
  margin: 0;
  font-weight: 600;
  color: #1e293b;
  font-size: 1.25rem;
}

.btn-close {
  background: none;
  border: none;
  color: #64748b;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.btn-close:hover {
  background: #f1f5f9;
  color: #334155;
}

/* 模态框内容 */
.modal-body {
  padding: 1.5rem;
  overflow-y: auto;
  flex: 1;
}

.modal-subtitle {
  color: #64748b;
  margin-bottom: 1.5rem;
  font-size: 0.95rem;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  font-weight: 600;
  color: #334155;
  font-size: 1rem;
}

.section-title i {
  color: #f6d55c;
}

/* 收藏夹列表 */
.folder-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.folder-item {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.folder-item:hover {
  border-color: #cbd5e0;
  background: #f8fafc;
}

.folder-item.active {
  border-color: #f6d55c;
  background: #fffbeb;
}

.folder-icon {
  color: #f6d55c;
  margin-right: 0.75rem;
}

.folder-name {
  flex: 1;
  font-weight: 500;
  color: #334155;
}

.folder-count {
  color: #94a3b8;
  font-size: 0.85rem;
  padding: 0.25rem 0.5rem;
  background: #f1f5f9;
  border-radius: 12px;
}

.check-icon {
  color: #f6d55c;
  margin-left: 0.75rem;
  display: none;
}

.folder-item.active .check-icon {
  display: inline;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 2rem 1rem;
  color: #94a3b8;
  border: 1px dashed #e2e8f0;
  border-radius: 8px;
  margin-bottom: 1.5rem;
}

.empty-state i {
  font-size: 2rem;
  margin-bottom: 0.75rem;
  display: block;
}

.empty-state p {
  margin: 0;
}

/* 分割线 */
.divider {
  position: relative;
  text-align: center;
  margin: 1.5rem 0;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e2e8f0;
}

.divider span {
  position: relative;
  background: white;
  padding: 0 0.75rem;
  color: #94a3b8;
  font-size: 0.85rem;
}

/* 创建新收藏夹 */
.create-form {
  display: flex;
  gap: 0.75rem;
}

.input-wrapper {
  position: relative;
  flex: 1;
}

.input-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}

.form-input {
  width: 100%;
  padding: 0.75rem 0.75rem 0.75rem 2.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: all 0.2s ease;
}

.form-input:focus {
  outline: none;
  border-color: #f6d55c;
  box-shadow: 0 0 0 3px rgba(246, 213, 92, 0.1);
}

.btn-create {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0 1rem;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-create:hover:not(:disabled) {
  background: #059669;
}

.btn-create:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 模态框底部 */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: 1.25rem 1.5rem;
  border-top: 1px solid #f1f5f9;
}

.btn-cancel {
  padding: 0.625rem 1.25rem;
  background: white;
  color: #64748b;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  background: #f8fafc;
  border-color: #cbd5e0;
}

.btn-confirm {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1.25rem;
  background: #f6d55c;
  color: #1e293b;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-confirm:hover:not(:disabled) {
  background: #f7c948;
}

.btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 480px) {
  .create-form {
    flex-direction: column;
  }
  
  .btn-create {
    width: 100%;
    justify-content: center;
    padding: 0.75rem;
  }
}
</style>
