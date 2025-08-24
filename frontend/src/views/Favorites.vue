<template>
  <div class="favorites-page">
    <!-- 标题区域 -->
    <div class="hero-section">
    <div class="container-fluid" style="max-width: 1200px;">
        <div class="hero-content">
          <h1 class="hero-title">我的收藏</h1>
          <p class="hero-subtitle">在这里管理您的收藏夹，珍藏每一个灵感瞬间</p>
        </div>
      </div>
    </div>
    
    <!-- 内容区域 -->
    <section class="content-section">
      <div class="container-fluid" style="max-width: 1400px; padding: 30px 15px;">
        <div class="row">
          <!-- 左侧边栏 -->
          <div class="col-md-3">
            <div class="sidebar">
              <div class="sidebar-header">
                <h4>收藏夹</h4>
              </div>
              <ul class="sidebar-nav">
                <li 
                  class="sidebar-item" 
                  :class="{ active: activeFolder === 'all' }"
                  @click="selectFolder('all')"
                >
                  <div class="folder-name">
                    <i class="fas fa-bookmark"></i>
                    全部收藏
                  </div>
                </li>
                <li 
                  v-for="folder in favoriteFolders" 
                  :key="folder.id" 
                  class="sidebar-item"
                  :class="{ active: activeFolder === folder.id }"
                  @click="selectFolder(folder.id)"
                >
                  <div class="folder-name">
                    <i class="fas fa-folder"></i>
                    {{ folder.name }}
                  </div>
                  <div class="folder-actions">
                    <button class="btn-folder-action btn-edit-folder" @click.stop="editFolder(folder)">
                      <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn-folder-action btn-delete-folder" @click.stop="confirmDeleteFolder(folder)">
                      <i class="fas fa-trash"></i>
                    </button>
                  </div>
                </li>
              </ul>
              
              <div class="add-collection">
                <button class="add-collection-btn" @click="showCreateFolderModal = true">
                  <i class="fas fa-plus"></i> 添加收藏夹
                </button>
              </div>
            </div>
          </div>
          
          <!-- 右侧内容区 -->
          <div class="col-md-9">
            <!-- 搜索 -->
            <div class="d-flex justify-content-between align-items-center mb-4">
              <h4 class="mb-0">{{ pageTitle }}</h4>
              <div class="search-container">
                <input 
                  type="text" 
                  class="search-input" 
                  placeholder="在当前收藏夹内搜索..." 
                  v-model="searchQuery"
                  @input="searchFavorites"
                >
                <i class="fas fa-search search-icon"></i>
              </div>
            </div>
            
            <!-- 收藏内容展示 -->
            <div class="bookshelf-container">
              <div class="bookshelf">
                <div class="shelf-shadow"></div>
                <div class="shelf-content">
                  <!-- 加载中状态 -->
                  <div v-if="loading" class="empty-shelf" style="grid-column: 1 / -1;">
                    <div class="empty-shelf-icon">
                      <i class="fas fa-spinner fa-spin"></i>
                    </div>
                    <h3>正在加载收藏内容...</h3>
                  </div>
                  
                  <!-- 空收藏状态 -->
                  <div v-else-if="displayedFavorites.length === 0" class="empty-shelf">
                    <div class="empty-shelf-icon">
                      <i class="fas fa-bookmark"></i>
                    </div>
                    <h3>暂无收藏内容</h3>
                    <p>您还没有添加任何收藏，浏览文章时点击收藏按钮即可添加到这里</p>
                    <a href="/" class="explore-button">
                      <i class="fas fa-compass"></i> 去探索内容
                    </a>
                  </div>
                  
                  <!-- 收藏列表 -->
                  <div 
                    v-else
                    v-for="favorite in displayedFavorites" 
                    :key="favorite.id"
                    class="book-item"
                    :class="{ 'fade-in': !loading }"
                  >
                    <a :href="'/article/' + favorite.id" class="book">
                      <div class="book-cover">
                        <div class="book-type-indicator">
                          <i class="fas fa-file-alt"></i>
                        </div>
                        <div class="book-cover-content">
                          <div class="book-title-area">
                            <h3 class="book-title">{{ favorite.title || '未命名文章' }}</h3>
                            <div class="favorite-date">{{ formatDate(favorite.gmtCreate || favorite.gmtModified || new Date()) }}</div>
                          </div>
                          
                          <div class="book-author">
                            <img :src="favorite.user?.avatarUrl || '/img/default.jpg'" alt="作者头像" class="author-avatar">
                            <span class="author-name">{{ favorite.user?.name || '未知作者' }}</span>
                          </div>
                          
                          <p class="book-description">{{ favorite.description || favorite.content?.substring(0, 100) || '暂无描述' }}</p>
                          
                          <div class="mt-2 text-muted small">
                            <span class="badge bg-light text-secondary me-2">{{ favorite.folderName || '默认收藏夹' }}</span>
                          </div>
                        </div>
                      </div>
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
    
    <!-- 创建收藏夹弹窗 -->
    <div class="modal fade" id="createFolderModal" tabindex="-1" aria-hidden="true" ref="createFolderModal">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modern-modal">
          <div class="modern-header">
            <div class="header-icon">
              <i class="fas fa-folder-plus"></i>
            </div>
            <div class="header-content">
              <h5 class="modal-title">{{ isEditing ? '编辑收藏夹' : '创建新收藏夹' }}</h5>
              <p class="modal-subtitle">{{ isEditing ? '修改您的收藏夹信息' : '创建一个新的收藏夹来整理您的收藏' }}</p>
            </div>
            <button type="button" class="modern-close" data-bs-dismiss="modal" aria-label="Close">
              <i class="fas fa-times"></i>
            </button>
          </div>
          <div class="modern-body">
            <div class="create-folder-form">
              <div class="mb-3">
                <label for="folderName" class="form-label section-label">
                  <i class="fas fa-tag"></i> 收藏夹名称
                </label>
                <input 
                  type="text" 
                  class="modern-input" 
                  id="folderName" 
                  v-model="newFolderName"
                  placeholder="输入收藏夹名称"
                >
              </div>
              <div class="mb-3">
                <label for="folderDescription" class="form-label section-label">
                  <i class="fas fa-align-left"></i> 收藏夹描述 (选填)
                </label>
                <textarea 
                  class="modern-input" 
                  id="folderDescription" 
                  rows="3" 
                  v-model="newFolderDescription"
                  placeholder="简单描述一下这个收藏夹的内容"
                ></textarea>
              </div>
              <div class="d-flex justify-content-end mt-4">
                <button 
                  type="button" 
                  class="btn btn-secondary me-2" 
                  data-bs-dismiss="modal"
                >
                  取消
                </button>
                <button 
                  type="button" 
                  class="modern-create-btn"
                  @click="createOrUpdateFolder"
                >
                  <i class="fas" :class="isEditing ? 'fa-save' : 'fa-plus-circle'"></i>
                  {{ isEditing ? '保存修改' : '创建收藏夹' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 删除确认弹窗 -->
    <div class="modal fade" id="deleteFolderModal" tabindex="-1" aria-hidden="true" ref="deleteFolderModal">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modern-modal">
          <div class="modern-header">
            <div class="header-icon" style="background: linear-gradient(135deg, #ff7675, #d63031);">
              <i class="fas fa-exclamation-triangle"></i>
            </div>
            <div class="header-content">
              <h5 class="modal-title">删除收藏夹</h5>
              <p class="modal-subtitle">此操作不可恢复，请确认</p>
            </div>
            <button type="button" class="modern-close" data-bs-dismiss="modal" aria-label="Close">
              <i class="fas fa-times"></i>
            </button>
          </div>
          <div class="modern-body">
            <p>您确定要删除收藏夹 <strong>{{ folderToDelete?.name }}</strong> 吗？</p>
            <p class="text-danger">注意：删除后，该收藏夹中的所有收藏内容将无法恢复。</p>
            <div class="d-flex justify-content-end mt-4">
              <button 
                type="button" 
                class="btn btn-secondary me-2" 
                data-bs-dismiss="modal"
              >
                取消
              </button>
              <button 
                type="button" 
                class="btn btn-danger"
                @click="deleteFolder"
              >
                <i class="fas fa-trash-alt me-1"></i>
                确认删除
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { favoriteAPI } from '@/api/favorite'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

// 路由和状态管理
const router = useRouter()
const authStore = useAuthStore()

// 简单的toast提示函数
const showToast = (message, type = 'info') => {
  // 创建toast元素
  const toast = document.createElement('div')
  toast.className = `toast toast-${type}`
  toast.textContent = message
  
  // 添加样式
  toast.style.position = 'fixed'
  toast.style.bottom = '20px'
  toast.style.right = '20px'
  toast.style.padding = '12px 20px'
  toast.style.borderRadius = '4px'
  toast.style.zIndex = '9999'
  toast.style.fontSize = '14px'
  toast.style.transition = 'all 0.3s ease'
  toast.style.opacity = '0'
  toast.style.transform = 'translateY(20px)'
  
  // 根据类型设置不同背景色
  switch(type) {
    case 'success':
      toast.style.backgroundColor = '#4caf50'
      toast.style.color = '#fff'
      break
    case 'error':
      toast.style.backgroundColor = '#f44336'
      toast.style.color = '#fff'
      break
    case 'warning':
      toast.style.backgroundColor = '#ff9800'
      toast.style.color = '#fff'
      break
    default:
      toast.style.backgroundColor = '#2196f3'
      toast.style.color = '#fff'
  }
  
  // 添加到页面
  document.body.appendChild(toast)
  
  // 显示动画
  setTimeout(() => {
    toast.style.opacity = '1'
    toast.style.transform = 'translateY(0)'
  }, 10)
  
  // 自动关闭
  setTimeout(() => {
    toast.style.opacity = '0'
    toast.style.transform = 'translateY(20px)'
    
    // 移除元素
    setTimeout(() => {
      document.body.removeChild(toast)
    }, 300)
  }, 3000)
}

// 数据状态
const loading = ref(true)
const favoriteFolders = ref([])
const favorites = ref([])
const activeFolder = ref('all')
const searchQuery = ref('')
const filteredFavorites = ref([])

// 弹窗状态
const showCreateFolderModal = ref(false)
const isEditing = ref(false)
const newFolderName = ref('')
const newFolderDescription = ref('')
const editingFolderId = ref(null)
const folderToDelete = ref(null)

// 计算属性
const pageTitle = computed(() => {
  if (activeFolder.value === 'all') {
    return '我的收藏'
  } else {
    const folder = favoriteFolders.value.find(f => f.id === activeFolder.value)
    return folder ? folder.name : '我的收藏'
  }
})

const displayedFavorites = computed(() => {
  if (searchQuery.value.trim() !== '') {
    return filteredFavorites.value
  }
  
  if (activeFolder.value === 'all') {
    return favorites.value
  } else {
    return favorites.value.filter(fav => fav.folderId === activeFolder.value)
  }
})

// 监听弹窗状态
watch(showCreateFolderModal, (newVal) => {
  if (newVal) {
    setTimeout(() => {
      const modal = new bootstrap.Modal(document.getElementById('createFolderModal'))
      modal.show()
    }, 100)
  }
})

// 生命周期钩子
onMounted(async () => {
  loading.value = true
  try {
    await loadFavoriteFolders()
    // 只有在成功加载收藏夹后才处理收藏内容
    if (favoriteFolders.value.length > 0) {
      await loadFavorites()
    }
  } catch (error) {
    console.error('初始化收藏夹页面失败:', error)
  } finally {
    loading.value = false
    initBookAnimation()
  }
})

// 方法
const loadFavoriteFolders = async () => {
  try {
    const response = await favoriteAPI.getFavoriteFolders()
    // 后端返回的是收藏夹列表
    favoriteFolders.value = response.data || []
    console.log('收藏夹数据:', favoriteFolders.value)
  } catch (error) {
    console.error('加载收藏夹失败:', error)
  }
}

const loadFavorites = async () => {
  try {
    // 由于后端没有单独的获取收藏内容的接口，我们使用收藏夹接口的数据
    // 将每个收藏夹中的文章提取出来
    const favoritesData = []
    
    // 从收藏夹中提取文章
    favoriteFolders.value.forEach(folder => {
      if (folder.articles && Array.isArray(folder.articles)) {
        folder.articles.forEach(article => {
          // 添加文章和所属文件夹信息
          favoritesData.push({
            ...article,
            folderId: folder.id,
            folderName: folder.name
          })
        })
      }
    })
    
    favorites.value = favoritesData
    filteredFavorites.value = favoritesData
    console.log('收藏内容数据:', favorites.value)
  } catch (error) {
    console.error('处理收藏内容失败:', error)
  }
}

const selectFolder = (folderId) => {
  activeFolder.value = folderId
  searchQuery.value = ''
}

const searchFavorites = () => {
  const query = searchQuery.value.toLowerCase().trim()
  if (!query) {
    filteredFavorites.value = favorites.value
    return
  }
  
  filteredFavorites.value = favorites.value.filter(fav => {
    return (fav.title?.toLowerCase().includes(query)) || 
           (fav.description?.toLowerCase().includes(query)) ||
           (fav.content?.toLowerCase().includes(query)) ||
           (fav.user?.name?.toLowerCase().includes(query)) ||
           (fav.folderName?.toLowerCase().includes(query))
  })
}

// 根据文章类型获取图标
const getTypeIcon = (type) => {
  // 由于后端没有提供文章类型，我们默认使用文章图标
  return 'fas fa-file-alt'
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const editFolder = (folder) => {
  isEditing.value = true
  editingFolderId.value = folder.id
  newFolderName.value = folder.name
  newFolderDescription.value = folder.description || ''
  showCreateFolderModal.value = true
}

const confirmDeleteFolder = (folder) => {
  folderToDelete.value = folder
  const modal = new bootstrap.Modal(document.getElementById('deleteFolderModal'))
  modal.show()
}

const deleteFolder = async () => {
  if (!folderToDelete.value) return
  
  try {
    await favoriteAPI.deleteFavoriteFolder(folderToDelete.value.id)
    await loadFavoriteFolders()
    await loadFavorites()
    
    if (activeFolder.value === folderToDelete.value.id) {
      activeFolder.value = 'all'
    }
    
    const modal = bootstrap.Modal.getInstance(document.getElementById('deleteFolderModal'))
    modal.hide()
    
    // 显示成功提示
    showToast(`收藏夹 "${folderToDelete.value.name}" 已成功删除`, 'success')
  } catch (error) {
    console.error('删除收藏夹失败:', error)
    showToast('删除收藏夹失败，请稍后重试', 'error')
  }
}

const createOrUpdateFolder = async () => {
  if (!newFolderName.value.trim()) {
    showToast('请输入收藏夹名称', 'warning')
    return
  }
  
  try {
    // 根据后端接口，创建和更新都使用 create-folder 接口
    const requestData = {
      favoriteName: newFolderName.value,
      description: newFolderDescription.value
    }
    
    if (isEditing.value && editingFolderId.value) {
      // 如果是编辑，添加收藏夹ID
      requestData.favoriteId = editingFolderId.value
    }
    
    // 调用创建收藏夹接口
    await favoriteAPI.createFavoriteFolder(requestData)
    
    // 重新加载收藏夹列表
    await loadFavoriteFolders()
    
    // 重置表单
    newFolderName.value = ''
    newFolderDescription.value = ''
    
    // 关闭弹窗
    const modal = bootstrap.Modal.getInstance(document.getElementById('createFolderModal'))
    modal.hide()
    
    // 显示成功提示
    const successMsg = isEditing.value ? '收藏夹更新成功' : '收藏夹创建成功'
    showToast(successMsg, 'success')
    
    // 最后重置编辑状态
    isEditing.value = false
    editingFolderId.value = null
  } catch (error) {
    console.error('创建/更新收藏夹失败:', error)
    showToast('操作失败，请稍后重试', 'error')
  }
}

const initBookAnimation = () => {
  setTimeout(() => {
    const bookItems = document.querySelectorAll('.book-item')
    bookItems.forEach((item, index) => {
      setTimeout(() => {
        item.classList.add('fade-in')
      }, index * 100)
    })
  }, 300)
}
</script>

<style scoped>
@import '/css/favorites.css';

.favorites-page {
  padding: 0 0 2rem 0;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  min-height: 100vh;
}

h2, h4 {
  font-weight: 700;
  color: #333;
  margin-bottom: 0.5rem;
}

.text-muted {
  color: #6c757d;
}

/* ===== Hero Section ===== */
.hero-section {
  padding: 3rem 0;
  background: white;
  text-align: center;
  position: relative;
}

.hero-content {
  position: relative;
  z-index: 2;
}

.hero-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  color: #2c3e50;
  font-family: 'Playfair Display', Georgia, serif;
  animation: fadeInUp 0.8s ease-out;
}

.hero-subtitle {
  font-size: 1.2rem;
  color: #64748b;
  font-weight: 400;
  margin-bottom: 0;
  animation: fadeInUp 0.8s ease-out 0.2s both;
}

.hero-content::after {
  content: '';
  position: absolute;
  bottom: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, #ffc107 0%, #ffda58 100%);
  border-radius: 2px;
}

/* ===== Content Section ===== */
.content-section {
  padding: 3rem 0;
  background: white;
  margin-top: -1rem;
  border-radius: 2rem 2rem 0 0;
  box-shadow: 0 -8px 32px rgba(255, 193, 7, 0.08);
}

/* ===== 动画效果 ===== */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.book-item {
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
}

.book-item:nth-child(1) { animation-delay: 0.1s; }
.book-item:nth-child(2) { animation-delay: 0.2s; }
.book-item:nth-child(3) { animation-delay: 0.3s; }
.book-item:nth-child(4) { animation-delay: 0.4s; }
.book-item:nth-child(5) { animation-delay: 0.5s; }
.book-item:nth-child(6) { animation-delay: 0.6s; }
.book-item:nth-child(7) { animation-delay: 0.7s; }
.book-item:nth-child(8) { animation-delay: 0.8s; }
.book-item:nth-child(9) { animation-delay: 0.9s; }

/* 侧边栏动画 */
.sidebar {
  animation: fadeInUp 0.6s ease-out;
}

/* 搜索框动画 */
.search-container {
  animation: fadeInUp 0.6s ease-out 0.3s both;
}
</style>