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
                <h4>我的收藏夹</h4>
                <button class="btn-add-folder" @click="showCreateFolderModal = true">
                  <i class="fas fa-plus"></i>
                </button>
              </div>
              <ul class="sidebar-nav">
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
                    <span class="folder-count">({{ folder.articlesCount || 0 }})</span>
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
              
              <!-- 添加收藏夹按钮 -->
              <div class="add-collection">
                <button class="add-collection-btn" @click="showCreateFolderModal = true">
                  <i class="fas fa-plus"></i> 添加收藏夹
                </button>
              </div>
            </div>
          </div>
          
          <!-- 右侧内容区 -->
          <div class="col-md-9">
            <!-- 搜索栏 -->
            <div class="search-section">
              <div class="search-container">
                <i class="fas fa-search search-icon"></i>
                <input 
                  type="text" 
                  class="search-input" 
                  placeholder="在当前收藏夹内搜索..." 
                  v-model="searchQuery"
                  @input="searchFavorites"
                >
              </div>
            </div>
            
            <!-- 内容标题 -->
            <div class="content-header">
              <h4 class="content-title">收藏列表 ({{ pageTitle }})</h4>
            </div>
            
            <!-- 收藏内容展示 -->
            <div class="content-area">
              <!-- 加载中状态 -->
              <div v-if="loading" class="loading-state">
                <div class="loading-spinner">
                  <i class="fas fa-spinner fa-spin"></i>
                </div>
                <h3>正在加载收藏内容...</h3>
              </div>
              
              <!-- 空收藏状态 -->
              <div v-else-if="displayedFavorites.length === 0" class="empty-state">
                <div class="empty-icon">
                  <i class="fas fa-bookmark"></i>
                </div>
                <h3>{{ emptyStateTitle }}</h3>
                <p>{{ emptyStateMessage }}</p>
                <router-link to="/" class="btn-explore">
                  <i class="fas fa-compass"></i> 去探索内容
                </router-link>
              </div>
              
              <!-- 收藏列表 -->
              <div v-else class="favorites-list">
                <div 
                  v-for="favorite in displayedFavorites" 
                  :key="favorite.articleId"
                  class="favorite-item"
                  :class="{ 'fade-in': !loading }"
                >
                  <div class="favorite-card">
                    <div class="card-header">
                      <div class="card-type-indicator">
                        <i class="fas fa-file-alt"></i>
                      </div>
                      <div class="card-title-area">
                        <h3 class="card-title">{{ favorite.title || '未命名文章' }}</h3>
                        <div class="card-date">
                          <i class="fas fa-calendar"></i>
                          {{ formatDate(favorite.gmtModified) }}
                        </div>
                      </div>
                    </div>
                    
                    <div class="card-content">
                      <div class="card-author">
                        <img :src="favorite.avatarUrl || '/img/default.jpg'" alt="作者头像" class="author-avatar">
                        <span class="author-name">{{ favorite.userName || '未知作者' }}</span>
                      </div>
                      
                      <div class="card-excerpt">
                        {{ favorite.excerpt || '暂无描述' }}
                      </div>
                      
                      <div class="card-stats">
                        <span class="stat-item">
                          <i class="fas fa-eye"></i>
                          {{ favorite.viewCount || 0 }}
                        </span>
                        <span class="stat-item">
                          <i class="fas fa-heart"></i>
                          {{ favorite.likes || 0 }}
                        </span>
                      </div>
                    </div>
                    
                    <div class="card-actions">
                      <router-link :to="`/article/${favorite.slug}`" class="btn-read">
                        <i class="fas fa-book-open"></i>
                        阅读
                      </router-link>
                      <button class="btn-remove" @click="removeFromFavorites(favorite)">
                        <i class="fas fa-trash"></i>
                        移除
                      </button>
                    </div>
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
                  maxlength="50"
                >
                <div class="input-counter">{{ newFolderName.length }}/50</div>
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
                  :disabled="!newFolderName.trim()"
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

// 数据状态
const loading = ref(true)
const favoriteFolders = ref([])
const favorites = ref([])
const activeFolder = ref(null)
const searchQuery = ref('')
const filteredFavorites = ref([])

// 弹窗状态
const showCreateFolderModal = ref(false)
const isEditing = ref(false)
const newFolderName = ref('')
const editingFolderId = ref(null)
const folderToDelete = ref(null)

// 计算属性
const pageTitle = computed(() => {
  if (!activeFolder.value) {
    return '我的收藏'
  } else {
    const folder = favoriteFolders.value.find(f => f.id === activeFolder.value)
    return folder ? folder.name : '我的收藏'
  }
})

const pageSubtitle = computed(() => {
  if (!activeFolder.value) {
    return '选择一个收藏夹查看文章'
  } else {
    const folder = favoriteFolders.value.find(f => f.id === activeFolder.value)
    return folder ? `查看收藏夹 "${folder.name}" 中的文章` : '选择一个收藏夹查看文章'
  }
})

const emptyStateTitle = computed(() => {
  if (!activeFolder.value) {
    return '请选择一个收藏夹'
  } else {
    const folder = favoriteFolders.value.find(f => f.id === activeFolder.value)
    return folder ? `收藏夹 "${folder.name}" 中暂无内容` : '请选择一个收藏夹'
  }
})

const emptyStateMessage = computed(() => {
  if (!activeFolder.value) {
    return '请从左侧选择一个收藏夹来查看其中的文章'
  } else {
    const folder = favoriteFolders.value.find(f => f.id === activeFolder.value)
    return folder ? '您还没有添加任何文章到这个收藏夹，点击收藏按钮即可添加' : '请从左侧选择一个收藏夹来查看其中的文章'
  }
})

const displayedFavorites = computed(() => {
  if (searchQuery.value.trim() !== '') {
    return filteredFavorites.value
  }
  return favorites.value
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
    console.log('开始加载收藏夹页面...')
    console.log('当前认证状态:', authStore.isAuthenticated)
    console.log('当前用户:', authStore.user)
    console.log('本地存储中的用户信息:', localStorage.getItem('hive_auth_user'))
    
    // 检查认证状态
    if (!authStore.isAuthenticated) {
      console.log('用户未认证，尝试检查认证状态...')
      const authResult = await authStore.checkAuthStatus()
      console.log('认证状态检查结果:', authResult)
      console.log('认证状态检查后:', authStore.isAuthenticated)
      console.log('认证状态检查后的用户:', authStore.user)
      
      if (!authResult) {
        console.log('用户未认证，重定向到登录页面')
        router.push({ name: 'Login', query: { redirect: '/favorites' } })
        return
      }
    }
    
    await loadFavoriteFolders()
    console.log('收藏夹加载完成')
    
    // 如果有收藏夹，自动选择第一个
    if (favoriteFolders.value.length > 0) {
      console.log('自动选择第一个收藏夹:', favoriteFolders.value[0].id)
      await selectFolder(favoriteFolders.value[0].id)
    }
    
    console.log('收藏夹页面初始化完成')
  } catch (error) {
    console.error('初始化收藏夹页面失败:', error)
    if (window.$toast) {
      window.$toast.error('加载收藏夹失败，请稍后重试')
    }
  } finally {
    loading.value = false
    initBookAnimation()
  }
})

// 方法
const loadFavoriteFolders = async () => {
  try {
    console.log('调用 getFavoriteFolders API...')
    const response = await favoriteAPI.getFavoriteFolders()
    console.log('API响应:', response)
    console.log('响应类型:', typeof response)
    console.log('响应是否为数组:', Array.isArray(response))
    
    if (Array.isArray(response)) {
      favoriteFolders.value = response
    } else if (response && Array.isArray(response.data)) {
      favoriteFolders.value = response.data
    } else {
      favoriteFolders.value = []
      console.warn('API响应格式不符合预期:', response)
    }
    
    console.log('收藏夹数据:', favoriteFolders.value)
  } catch (error) {
    console.error('加载收藏夹失败:', error)
    console.error('错误详情:', {
      status: error.status,
      message: error.message,
      data: error.data
    })
    throw error
  }
}

const loadFavorites = async () => {
  try {
    // 如果当前没有选中的收藏夹，清空文章列表
    if (!activeFolder.value) {
      favorites.value = []
      filteredFavorites.value = []
      return
    }
    
    // 获取选中收藏夹的详细信息
    const response = await favoriteAPI.getFavoriteFolderById(activeFolder.value)
    console.log('收藏夹详细信息:', response)
    
    if (response && response.articles) {
      favorites.value = response.articles
      filteredFavorites.value = response.articles
      console.log('收藏夹文章数据:', favorites.value)
    } else {
      favorites.value = []
      filteredFavorites.value = []
      console.log('收藏夹中没有文章')
    }
  } catch (error) {
    console.error('加载收藏夹文章失败:', error)
    favorites.value = []
    filteredFavorites.value = []
    throw error
  }
}

const selectFolder = async (folderId) => {
  activeFolder.value = folderId
  searchQuery.value = ''
  
  // 加载选中收藏夹的文章
  if (folderId) {
    try {
      await loadFavorites()
    } catch (error) {
      console.error('切换收藏夹失败:', error)
      if (window.$toast) {
        window.$toast.error('加载收藏夹内容失败，请稍后重试')
      }
    }
  } else {
    favorites.value = []
    filteredFavorites.value = []
  }
}

const searchFavorites = () => {
  const query = searchQuery.value.toLowerCase().trim()
  if (!query) {
    filteredFavorites.value = favorites.value
    return
  }
  
  filteredFavorites.value = favorites.value.filter(fav => {
    return (fav.title?.toLowerCase().includes(query)) || 
           (fav.excerpt?.toLowerCase().includes(query)) ||
           (fav.userName?.toLowerCase().includes(query)) ||
           (fav.folderName?.toLowerCase().includes(query))
  })
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const editFolder = (folder) => {
  console.log('编辑收藏夹:', folder)
  isEditing.value = true
  editingFolderId.value = folder.id
  newFolderName.value = folder.name
  showCreateFolderModal.value = true
}

const confirmDeleteFolder = (folder) => {
  console.log('确认删除收藏夹:', folder)
  folderToDelete.value = folder
  const modal = new bootstrap.Modal(document.getElementById('deleteFolderModal'))
  modal.show()
}

const deleteFolder = async () => {
  if (!folderToDelete.value) return
  
  try {
    await favoriteAPI.deleteFavoriteFolder(folderToDelete.value.id)
    await loadFavoriteFolders()
    
    // 如果删除的是当前选中的收藏夹，需要重新选择
    if (activeFolder.value === folderToDelete.value.id) {
      if (favoriteFolders.value.length > 0) {
        // 选择第一个收藏夹
        await selectFolder(favoriteFolders.value[0].id)
      } else {
        // 没有收藏夹了，清空状态
        activeFolder.value = null
        favorites.value = []
        filteredFavorites.value = []
      }
    }
    
    const modal = bootstrap.Modal.getInstance(document.getElementById('deleteFolderModal'))
    modal.hide()
    
    if (window.$toast) {
      window.$toast.success(`收藏夹 "${folderToDelete.value.name}" 已成功删除`)
    }
  } catch (error) {
    console.error('删除收藏夹失败:', error)
    if (window.$toast) {
      window.$toast.error('删除收藏夹失败，请稍后重试')
    }
  }
}

const createOrUpdateFolder = async () => {
  if (!newFolderName.value.trim()) {
    if (window.$toast) {
      window.$toast.warning('请输入收藏夹名称')
    }
    return
  }
  
  try {
    const requestData = {
      favoriteName: newFolderName.value.trim()
    }
    
    if (isEditing.value && editingFolderId.value) {
      // 编辑现有收藏夹
      await favoriteAPI.updateFavoriteFolder(editingFolderId.value, requestData)
    } else {
      // 创建新收藏夹
      await favoriteAPI.createFavoriteFolder(requestData)
    }
    
    // 重新加载收藏夹列表
    await loadFavoriteFolders()
    
    // 如果是创建新收藏夹，自动选择它
    if (!isEditing.value) {
      const newFolder = favoriteFolders.value.find(f => f.name === newFolderName.value.trim())
      if (newFolder) {
        await selectFolder(newFolder.id)
      }
    }
    
    // 重置表单
    newFolderName.value = ''
    
    // 关闭弹窗
    const modal = bootstrap.Modal.getInstance(document.getElementById('createFolderModal'))
    modal.hide()
    
    // 显示成功提示
    const successMsg = isEditing.value ? '收藏夹更新成功' : '收藏夹创建成功'
    if (window.$toast) {
      window.$toast.success(successMsg)
    }
    
    // 重置编辑状态
    isEditing.value = false
    editingFolderId.value = null
  } catch (error) {
    console.error('创建/更新收藏夹失败:', error)
    if (window.$toast) {
      window.$toast.error('操作失败，请稍后重试')
    }
  }
}

const removeFromFavorites = async (favorite) => {
  try {
    await favoriteAPI.removeFromAllFolders(favorite.articleId)
    
    // 重新加载当前收藏夹的文章
    if (activeFolder.value) {
      await loadFavorites()
    }
    
    if (window.$toast) {
      window.$toast.success('文章已从收藏夹中移除')
    }
  } catch (error) {
    console.error('移除收藏失败:', error)
    if (window.$toast) {
      window.$toast.error('移除失败，请稍后重试')
    }
  }
}

const initBookAnimation = () => {
  setTimeout(() => {
    const bookItems = document.querySelectorAll('.favorite-item')
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
  padding: 0;
  background: #ffffff;
  min-height: 100vh;
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
  padding: 2rem 0;
  background: white;
}

/* ===== Sidebar ===== */
.sidebar {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  height: fit-content;
  position: sticky;
  top: 100px;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f1f3f4;
}

.sidebar-header h4 {
  margin: 0;
  color: #2c3e50;
  font-weight: 600;
  font-size: 1.1rem;
}

.btn-add-folder {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffc107 0%, #ffda58 100%);
  border: none;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  box-shadow: 0 2px 8px rgba(255, 193, 7, 0.3);
}

.btn-add-folder:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.4);
}

.sidebar-nav {
  list-style: none;
  padding: 0;
  margin: 0;
}

.sidebar-item {
  margin-bottom: 0.5rem;
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.sidebar-item:hover {
  background: #fff9c4;
}

.sidebar-item.active {
  background: linear-gradient(135deg, #ffc107 0%, #ffda58 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.3);
}

.folder-name {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  font-weight: 500;
  position: relative;
}

.folder-name i {
  margin-right: 0.75rem;
  font-size: 1rem;
  color: #6c757d;
}

.sidebar-item.active .folder-name i {
  color: white;
}

.folder-count {
  margin-left: auto;
  font-size: 0.8rem;
  opacity: 0.8;
}

.folder-actions {
  position: absolute;
  right: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  gap: 0.25rem;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.sidebar-item:hover .folder-actions {
  opacity: 1;
}

.sidebar-item.active .folder-actions {
  opacity: 1;
}

.btn-folder-action {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  color: #6c757d;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.7rem;
}

.btn-edit-folder {
  background: rgba(255, 193, 7, 0.9);
  color: #2c3e50;
}

.btn-edit-folder:hover {
  background: #ffc107;
  color: #2c3e50;
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(255, 193, 7, 0.3);
}

.btn-delete-folder {
  background: rgba(220, 53, 69, 0.9);
  color: white;
}

.btn-delete-folder:hover {
  background: #dc3545;
  color: white;
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3);
}

.sidebar-item.active .btn-folder-action {
  background: rgba(255, 255, 255, 0.95);
  color: #000000;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.sidebar-item.active .btn-edit-folder {
  background: rgba(255, 193, 7, 0.95);
  color: #2c3e50;
}

.sidebar-item.active .btn-delete-folder {
  background: rgba(220, 53, 69, 0.95);
  color: white;
}

.sidebar-item.active .btn-edit-folder:hover {
  background: #ffc107;
  color: #2c3e50;
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(255, 193, 7, 0.3);
}

.sidebar-item.active .btn-delete-folder:hover {
  background: #dc3545;
  color: white;
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3);
}

.btn-folder-action:hover {
  background: white;
  color: #dc3545;
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 添加收藏夹按钮 */
.add-collection {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #f1f3f4;
}

.add-collection-btn {
  width: 100%;
  background: transparent;
  border: 2px dashed #dee2e6;
  padding: 0.75rem;
  border-radius: 8px;
  color: #6c757d;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.add-collection-btn:hover {
  background: #fff9c4;
  color: #ff8f00;
  border-color: #ffc107;
}

.add-collection-btn i {
  margin-right: 0.5rem;
}

/* ===== Search Section ===== */
.search-section {
  margin-bottom: 1.5rem;
}

.search-container {
  position: relative;
  max-width: 400px;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #6c757d;
  font-size: 0.9rem;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  border: 1px solid #e9ecef;
  border-radius: 25px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  background: #f8f9fa;
}

.search-input:focus {
  outline: none;
  border-color: #ffc107;
  box-shadow: 0 0 0 3px rgba(255, 193, 7, 0.1);
  background: white;
}

/* ===== Content Header ===== */
.content-header {
  margin-bottom: 2rem;
}

.content-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

/* ===== Content Area ===== */
.content-area {
  min-height: 400px;
}

/* ===== Loading State ===== */
.loading-state {
  text-align: center;
  padding: 3rem 1rem;
}

.loading-spinner {
  font-size: 2rem;
  color: #ffc107;
  margin-bottom: 1rem;
}

.loading-state h3 {
  color: #6c757d;
  font-weight: 500;
}

/* ===== Empty State ===== */
.empty-state {
  text-align: center;
  padding: 3rem 1rem;
}

.empty-icon {
  font-size: 4rem;
  color: #dee2e6;
  margin-bottom: 1.5rem;
}

.empty-state h3 {
  color: #6c757d;
  font-weight: 600;
  margin-bottom: 1rem;
}

.empty-state p {
  color: #adb5bd;
  margin-bottom: 2rem;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.btn-explore {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #ffc107 0%, #ffda58 100%);
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.3);
}

.btn-explore:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 193, 7, 0.4);
  color: white;
  text-decoration: none;
}

/* ===== Favorites List ===== */
.favorites-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.favorite-item {
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
}

.favorite-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid #f1f3f4;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.favorite-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(255, 193, 7, 0.15);
  border-color: #ffc107;
}

.card-header {
  padding: 1.5rem 1.5rem 1rem 1.5rem;
  background: linear-gradient(135deg, #fff9c4 0%, #ffffff 100%);
  border-bottom: 1px solid #f1f3f4;
  position: relative;
}

.card-type-indicator {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #ffc107 0%, #ffda58 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.9rem;
  box-shadow: 0 2px 8px rgba(255, 193, 7, 0.3);
}

.card-title-area {
  margin-top: 0.5rem;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 0.5rem 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-date {
  font-size: 0.85rem;
  color: #6c757d;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.card-date i {
  color: #ffc107;
}

.card-content {
  padding: 1rem 1.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-author {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
}

.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 0.75rem;
  border: 2px solid #f1f3f4;
}

.author-name {
  font-size: 0.9rem;
  color: #6c757d;
  font-weight: 500;
}

.card-excerpt {
  font-size: 0.9rem;
  color: #6c757d;
  line-height: 1.6;
  margin-bottom: 1rem;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.card-stats {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.85rem;
  color: #6c757d;
}

.stat-item i {
  color: #ffc107;
}

.card-actions {
  display: flex;
  gap: 0.5rem;
  padding: 1rem 1.5rem;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.btn-read, .btn-remove {
  flex: 1;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  text-decoration: none;
}

.btn-read {
  background: linear-gradient(135deg, #ffc107 0%, #ffda58 100%);
  color: white;
}

.btn-read:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.3);
  color: white;
  text-decoration: none;
}

.btn-remove {
  background: #f8f9fa;
  color: #6c757d;
  border: 1px solid #dee2e6;
}

.btn-remove:hover {
  background: #dc3545;
  color: white;
  border-color: #dc3545;
}

/* ===== Modal Styles ===== */
.modern-modal {
  border: none;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.modern-header {
  display: flex;
  align-items: center;
  padding: 1.5rem;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 1px solid #e9ecef;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #ffc107 0%, #ffda58 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 1rem;
}

.header-content {
  flex: 1;
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 0.25rem 0;
}

.modal-subtitle {
  color: #6c757d;
  margin: 0;
  font-size: 0.9rem;
}

.modern-close {
  width: 32px;
  height: 32px;
  border: none;
  background: #f8f9fa;
  border-radius: 50%;
  color: #6c757d;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.modern-close:hover {
  background: #e9ecef;
  color: #495057;
}

.modern-body {
  padding: 1.5rem;
}

.modern-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.modern-input:focus {
  outline: none;
  border-color: #ffc107;
  box-shadow: 0 0 0 3px rgba(255, 193, 7, 0.1);
}

.input-counter {
  text-align: right;
  font-size: 0.8rem;
  color: #6c757d;
  margin-top: 0.25rem;
}

.modern-create-btn {
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #ffc107 0%, #ffda58 100%);
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.modern-create-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.3);
}

.modern-create-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

.favorite-item {
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
}

.favorite-item:nth-child(1) { animation-delay: 0.1s; }
.favorite-item:nth-child(2) { animation-delay: 0.2s; }
.favorite-item:nth-child(3) { animation-delay: 0.3s; }
.favorite-item:nth-child(4) { animation-delay: 0.4s; }
.favorite-item:nth-child(5) { animation-delay: 0.5s; }

/* 侧边栏动画 */
.sidebar {
  animation: fadeInUp 0.6s ease-out;
}

/* 搜索框动画 */
.search-container {
  animation: fadeInUp 0.6s ease-out 0.3s both;
}

/* ===== 响应式设计 ===== */
@media (max-width: 768px) {
  .content-section {
    padding: 1rem 0;
  }
  
  .sidebar {
    position: static;
    margin-bottom: 2rem;
  }
  
  .search-container {
    max-width: 100%;
  }
  
  .favorites-list {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .card-actions {
    flex-direction: column;
  }
  
  .btn-read, .btn-remove {
    width: 100%;
    justify-content: center;
  }
}
</style>