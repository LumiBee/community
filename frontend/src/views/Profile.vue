<template>
  <div class="profile-page">
    <!-- 错误提示 -->
    <div v-if="errorMessage" class="alert alert-danger alert-dismissible fade show m-3" role="alert">
      <strong>加载失败!</strong> {{ errorMessage }}
      <button type="button" class="btn-close" @click="errorMessage = ''" aria-label="Close"></button>
      <div class="mt-2">
        <button class="btn btn-sm btn-outline-danger" @click="fetchProfileData">
          <i class="fas fa-sync-alt me-1"></i> 重试
        </button>
      </div>
    </div>
    
    <!-- 顶部封面区域 -->
    <div class="profile-cover" :style="coverStyle">
      <!-- 骨架屏 -->
      <div v-if="isLoading" class="cover-skeleton"></div>
      
      <!-- 预加载封面图片 -->
      <img 
        v-if="profileData.user?.backgroundImgUrl" 
        :src="profileData.user.backgroundImgUrl" 
        :alt="`${profileData.user?.name || '用户'}的封面图片`"
        class="cover-image-preload"
        @load="onCoverImageLoad"
        @error="onCoverImageError"
      />
      
      <div class="cover-overlay"></div>
      <div class="cover-pattern"></div>
      <div class="container position-relative h-100">
        <!-- 如果是页面所有者，显示更换封面按钮 -->
        <div v-if="isOwner" class="cover-edit-btn">
          <label for="coverUpload" class="btn btn-light btn-modern" :class="{ 'uploading': isLoading }">
            <i class="fas" :class="isLoading ? 'fa-spinner fa-spin' : 'fa-crop'"></i>
            <span class="ms-2">{{ isLoading ? '上传中...' : '更换封面' }}</span>
          </label>
          <input 
            type="file" 
            id="coverUpload" 
            class="d-none" 
            accept="image/*"
            @change="handleCoverUpload"
            :disabled="isLoading"
          >
        </div>
      </div>
    </div>

    <!-- 个人资料区域 -->
    <div class="container profile-container">
      <div class="row">
        <div class="col-lg-9">
          <!-- 文章列表 -->
          <div class="articles-container">
            <div v-if="isLoading" class="loading-container modern-loading">
              <div class="loading-spinner">
                <div class="spinner-ring"></div>
                <div class="spinner-ring"></div>
                <div class="spinner-ring"></div>
              </div>
              <p>加载中...</p>
            </div>
            
            <div v-else-if="articles.length === 0" class="empty-state modern-empty">
              <div class="empty-icon">
                <i class="far fa-file-alt"></i>
              </div>
              <h4>暂无文章</h4>
              <p>{{ isOwner ? '开始创作您的第一篇文章吧！' : '该用户还没有发布任何文章' }}</p>
              <router-link v-if="isOwner" to="/publish" class="btn btn-warning modern-btn">
                <i class="fas fa-pen me-2"></i> 写文章
              </router-link>
            </div>
            
            <div v-else class="row">
              <div v-for="(article, index) in articles" :key="article.id || article.articleId" class="col-md-6 mb-4">
                <div class="article-card modern-article" :style="{animationDelay: index * 0.1 + 's'}">
                  <div class="article-card-inner">
                    <div class="article-badge">文章</div>
                    <router-link :to="`/article/${article.slug}`">
                      <div class="article-cover">
                        <!-- 文章封面骨架屏 -->
                        <div v-if="!article.coverImgLoaded" class="article-cover-skeleton"></div>
                        <!-- 文章封面图片 -->
                        <img 
                          v-if="article.coverImg" 
                          :src="article.coverImg" 
                          :alt="`${article.title}的封面图片`"
                          class="article-cover-image"
                          loading="lazy"
                          @load="onArticleCoverLoad(article)"
                          @error="onArticleCoverError(article)"
                        />
                        <!-- 默认封面 -->
                        <img 
                          v-else 
                          src="/img/default.jpg" 
                          :alt="`${article.title}的默认封面`"
                          class="article-cover-image"
                          loading="lazy"
                          @load="onArticleCoverLoad(article)"
                        />
                        <div class="article-cover-overlay"></div>
                      </div>
                    </router-link>
                    <div class="article-content">
                      <router-link :to="`/article/${article.slug}`" class="article-title">
                        {{ article.title }}
                      </router-link>
                      <p class="article-excerpt">{{ article.excerpt }}</p>
                      
                      <!-- 文章操作按钮 - 仅页面所有者可见 -->
                      <div v-if="isOwner" class="article-actions">
                        <router-link 
                          :to="`/publish?edit=${article.id || article.articleId}`" 
                          class="action-btn edit-btn"
                          title="编辑文章"
                        >
                          <i class="fas fa-edit"></i>
                        </router-link>
                        <button 
                          @click="confirmDeleteArticle(article, index)" 
                          class="action-btn delete-btn"
                          title="删除文章"
                        >
                          <i class="fas fa-trash"></i>
                        </button>
                      </div>
                      
                      <div class="article-meta modern-meta">
                        <span class="article-date">
                          <i class="far fa-calendar-alt"></i> {{ formatDate(article.gmtCreate || article.gmtModified || article.gmt_create || article.gmt_modified) }}
                          <!-- 临时调试信息 -->
                          <small v-if="!article.gmtCreate && !article.gmtModified && !article.gmt_create && !article.gmt_modified" style="color: red; font-size: 0.7rem;">
                            (无日期数据)
                          </small>
                        </span>
                        <div class="article-stats">
                          <span class="stat-item" title="阅读量">
                            <i class="far fa-eye"></i> {{ article.viewCount || 0 }}
                          </span>
                          <span class="stat-item" title="点赞数">
                            <i class="far fa-heart"></i> {{ article.likes || 0 }}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 分页 -->
              <div class="pagination-container modern-pagination">
                <button 
                  @click="loadPreviousPage" 
                  :disabled="currentPage <= 1"
                  class="pagination-btn"
                >
                  <i class="fas fa-chevron-left"></i> 上一页
                </button>
                
                <!-- 页码按钮 -->
                <div class="page-numbers">
                  <button 
                    v-for="page in visiblePages" 
                    :key="page"
                    @click="goToPage(page)"
                    class="page-number-btn"
                    :class="{ active: page === currentPage, disabled: page === '...' }"
                    :disabled="page === '...'"
                  >
                    {{ page }}
                  </button>
                </div>
                
                <button 
                  @click="loadNextPage" 
                  :disabled="!hasNextPage"
                  class="pagination-btn"
                >
                  下一页 <i class="fas fa-chevron-right"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <div class="col-lg-3">
          <!-- 用户信息卡片 -->
          <div class="profile-card modern-card">
            <div class="card-glow"></div>
            <div class="avatar-container">
              <div class="avatar-wrapper">
                <div class="avatar-ring"></div>
                <!-- 头像骨架屏 -->
                <div v-if="isLoading" class="avatar-skeleton"></div>
                <!-- 预加载头像 -->
                <img 
                  v-if="profileData.user?.avatarUrl" 
                  :src="getAvatarUrl(profileData.user.avatarUrl)" 
                  :alt="`${profileData.user?.name || '用户'}的头像`" 
                  class="profile-avatar"
                  @load="onAvatarLoad"
                  @error="onAvatarError"
                />
                <!-- 默认头像 -->
                <img 
                  v-else 
                  src="/img/default01.jpg" 
                  alt="默认头像" 
                  class="profile-avatar"
                  @load="onAvatarLoad"
                />
                <div class="avatar-status" v-if="isOwner">
                  <div class="status-dot"></div>
                </div>
              </div>
            </div>
            <div class="profile-info">
              <h3 class="profile-name">{{ profileData.user?.name || '加载中...' }}</h3>
              <p class="profile-username text-muted">@{{ profileData.user?.name || '' }}</p>
              
              <!-- 关注按钮 -->
              <div class="profile-actions">
                <button 
                  v-if="!isOwner" 
                  class="btn follow-btn modern-btn" 
                  :class="profileData.isFollowed ? 'followed' : ''"
                  @click="toggleFollow"
                  :disabled="isLoading"
                >
                  <i class="fas" :class="profileData.isFollowed ? 'fa-user-check' : 'fa-user-plus'"></i>
                  <span>{{ profileData.isFollowed ? '已关注' : '关注' }}</span>
                </button>
                <router-link 
                  v-if="isOwner" 
                  to="/settings" 
                  class="btn edit-profile-btn modern-btn"
                >
                  <i class="fas fa-cog me-2"></i> 编辑资料
                </router-link>
              </div>
              
              <!-- 用户统计数据 -->
              <div class="profile-stats modern-stats">
                <div class="stat-item">
                  <div class="stat-icon">
                    <i class="fas fa-pen-nib"></i>
                  </div>
                  <div class="stat-value">{{ profileData.articleCount || 0 }}</div>
                  <div class="stat-label">文章</div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon">
                    <i class="fas fa-users"></i>
                  </div>
                  <div class="stat-value">{{ profileData.followersCount || 0 }}</div>
                  <div class="stat-label">粉丝</div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon">
                    <i class="fas fa-user-plus"></i>
                  </div>
                  <div class="stat-value">{{ profileData.followingCount || 0 }}</div>
                  <div class="stat-label">关注</div>
                </div>
              </div>
              
              <!-- 用户简介 -->
              <div class="profile-bio modern-bio">
                <div class="bio-icon">
                  <i class="fas fa-quote-left"></i>
                </div>
                <p>{{ profileData.user?.bio || '这个人很懒，什么都没有写~' }}</p>
              </div>
              
              <!-- 加入日期 -->
              <div class="profile-join-date modern-date">
                <div class="date-icon">
                  <i class="far fa-calendar-alt"></i>
                </div>
                <span>加入于 {{ formatDate(profileData.user?.gmtCreate || profileData.user?.gmtModified || profileData.user?.gmt_create || profileData.user?.gmt_modified) }}</span>
              </div>
              
              <!-- 社交链接 -->
              <div class="social-links modern-social">
                <a href="#" class="social-link" title="GitHub">
                  <i class="fab fa-github"></i>
                  <span class="social-tooltip">GitHub</span>
                </a>
                <a href="#" class="social-link" title="微博">
                  <i class="fab fa-weibo"></i>
                  <span class="social-tooltip">微博</span>
                </a>
                <a href="#" class="social-link" title="知乎">
                  <i class="fab fa-zhihu"></i>
                  <span class="social-tooltip">知乎</span>
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 图片裁剪组件 -->
    <SimpleImageCropper
      :visible="showCropper"
      :image-src="selectedImage"
      @close="closeCropper"
      @crop="handleCropComplete"
    />
    
    <!-- 删除文章确认弹窗 -->
    <ConfirmDeleteModal
      :visible="showDeleteModal"
      title="删除文章"
      :message="`您确定要删除文章「${articleToDelete?.title}」吗？`"
      warning-message="删除后，该文章将无法恢复。"
      confirm-text="确认删除"
      :position-index="deleteModalPositionIndex"
      @confirm="deleteArticle"
      @cancel="closeDeleteModal"
      @close="closeDeleteModal"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { request } from '@/api'
import { articleAPI } from '@/api/article'
import { ensureBigIntAsString, debugId } from '@/utils/bigint-helper'
import { toasts } from '@/plugins/toast'
import SimpleImageCropper from '@/components/SimpleImageCropper.vue'
import ConfirmDeleteModal from '@/components/ConfirmDeleteModal.vue'
import { getAvatarUrl } from '@/utils/avatar-helper'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const profileData = ref({})
const isLoading = ref(true)
const currentPage = ref(1)
const pageSize = ref(6)
const username = ref('')
const errorMessage = ref('') // 添加错误信息

// 图片裁剪相关
const showCropper = ref(false)
const selectedImage = ref(null)
const croppedImageData = ref(null)

// 删除相关
const showDeleteModal = ref(false)
const articleToDelete = ref(null)
const deleteModalPositionIndex = ref(0)
const isOwner = computed(() => profileData.value.isOwner)
const articles = computed(() => profileData.value.articles?.records || [])
const hasNextPage = computed(() => {
  if (!profileData.value.articles) return false
  return currentPage.value < profileData.value.articles.pages
})

// 计算可见的页码
const visiblePages = computed(() => {
  if (!profileData.value.articles || profileData.value.articles.pages <= 1) {
    return []
  }
  
  const totalPages = profileData.value.articles.pages
  const current = currentPage.value
  const pages = []
  
  // 如果总页数小于等于7，显示所有页码
  if (totalPages <= 7) {
    for (let i = 1; i <= totalPages; i++) {
      pages.push(i)
    }
    return pages
  }
  
  // 如果总页数大于7，显示部分页码
  if (current <= 4) {
    // 当前页在前4页，显示前5页 + ... + 最后一页
    for (let i = 1; i <= 5; i++) {
      pages.push(i)
    }
    pages.push('...')
    pages.push(totalPages)
  } else if (current >= totalPages - 3) {
    // 当前页在后4页，显示第一页 + ... + 后5页
    pages.push(1)
    pages.push('...')
    for (let i = totalPages - 4; i <= totalPages; i++) {
      pages.push(i)
    }
  } else {
    // 当前页在中间，显示第一页 + ... + 当前页前后各2页 + ... + 最后一页
    pages.push(1)
    pages.push('...')
    for (let i = current - 2; i <= current + 2; i++) {
      pages.push(i)
    }
    pages.push('...')
    pages.push(totalPages)
  }
  
  return pages
})

// 图片加载状态
const coverImageLoaded = ref(false)
const avatarLoaded = ref(false)

// 计算封面样式
const coverStyle = computed(() => {
  const coverUrl = profileData.value.user?.backgroundImgUrl || '/img/bg.jpg'
  return {
    backgroundImage: coverImageLoaded.value ? `url(${coverUrl})` : 'none'
  }
})

// 图片加载处理函数
const onCoverImageLoad = () => {
  coverImageLoaded.value = true
}

const onCoverImageError = () => {
  coverImageLoaded.value = true // 即使加载失败也要隐藏骨架屏
}

const onAvatarLoad = () => {
  avatarLoaded.value = true
}

const onAvatarError = () => {
  avatarLoaded.value = true // 即使加载失败也要隐藏骨架屏
}

const onArticleCoverLoad = (article) => {
  article.coverImgLoaded = true
}

const onArticleCoverError = (article) => {
  article.coverImgLoaded = true // 即使加载失败也要隐藏骨架屏
}

// 监听路由参数变化
watch(() => route.params.name, (newName) => {
  if (newName) {
    username.value = newName
    fetchProfileData()
  }
})

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知日期'
  
  try {
    let date
    
    // 处理不同的日期格式
    if (typeof dateString === 'string') {
      // 如果是字符串，尝试解析
      date = new Date(dateString)
    } else if (dateString instanceof Date) {
      // 如果已经是Date对象
      date = dateString
    } else if (typeof dateString === 'number') {
      // 如果是时间戳
      date = new Date(dateString)
    } else {
      return '日期格式错误'
    }
    
    if (isNaN(date.getTime())) {
      return '日期无效'
    }
    
    return date.toLocaleDateString('zh-CN', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    })
  } catch (error) {
    console.error('日期格式化错误:', error, '输入:', dateString)
    return '日期错误'
  }
}

// 获取个人资料数据
const fetchProfileData = async () => {
  isLoading.value = true
  coverImageLoaded.value = false
  avatarLoaded.value = false
  
  try {
    const response = await request({
      url: `/api/profile/${username.value}`,
      method: 'get',
      params: { page: currentPage.value, size: pageSize.value },
      timeout: 30000 // 增加超时时间到30秒
    })
    
    // 为文章添加加载状态
    if (response.articles?.records) {
      response.articles.records.forEach(article => {
        article.coverImgLoaded = false
      })
    }
    
    profileData.value = response
  } catch (error) {
    console.error('获取个人资料失败:', error)
    // 显示更详细的错误信息
    if (error.response) {
      console.error('错误响应状态:', error.response.status)
      console.error('错误响应数据:', error.response.data)
      errorMessage.value = `服务器错误 (${error.response.status}): ${error.message}`
    } else if (error.request) {
      console.error('请求发送但无响应:', error.request)
      errorMessage.value = '服务器未响应，请检查网络连接或稍后再试'
    } else {
      console.error('请求设置错误:', error.message)
      errorMessage.value = `请求错误: ${error.message}`
    }
  } finally {
    isLoading.value = false
  }
}

// 关注/取消关注
const toggleFollow = async () => {
  if (!authStore.isAuthenticated) {
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  
  try {
    isLoading.value = true
    // 确保用户ID作为字符串处理，避免JavaScript大整数精度丢失
    const userIdStr = ensureBigIntAsString(profileData.value.user.id);
    debugId(profileData.value.user.id, '目标用户ID');
    const response = await request({
      url: `/api/user/${userIdStr}/follow`,
      method: 'post'
    })

    
    // 检查响应格式并更新关注状态 - 响应数据直接是对象
    if (response && response.isFollowing !== undefined) {
      // 后端返回的是 isFollowing，我们需要更新为 isFollowed
      profileData.value.isFollowed = response.isFollowing
      
      // 更新粉丝数
      if (response.isFollowing) {
        profileData.value.followersCount++
      } else {
        profileData.value.followersCount--
      }
    } else {
      console.error('响应格式错误:', response)
    }
  } catch (error) {
    console.error('关注操作失败:', error)
  } finally {
    isLoading.value = false
  }
}

// 处理图片选择
const handleCoverUpload = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    window.$toast?.error('请选择有效的图片文件（JPG、PNG、GIF、WebP）')
    event.target.value = ''
    return
  }
  
  // 验证文件大小（5MB）
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    window.$toast?.error('图片文件大小不能超过5MB')
    event.target.value = ''
    return
  }
  
  // 创建图片URL并显示裁剪界面
  const imageUrl = URL.createObjectURL(file)
  selectedImage.value = imageUrl
  showCropper.value = true
  
  // 清空文件输入，允许再次选择同一文件
  event.target.value = ''
}

// 处理裁剪完成
const handleCropComplete = async (croppedData) => {
  try {
    isLoading.value = true
    
    // 将base64数据转换为Blob
    const base64Data = croppedData.split(',')[1]
    const byteCharacters = atob(base64Data)
    const byteNumbers = new Array(byteCharacters.length)
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i)
    }
    const byteArray = new Uint8Array(byteNumbers)
    const blob = new Blob([byteArray], { type: 'image/jpeg' })
    
    // 创建FormData
    const formData = new FormData()
    formData.append('coverImageFile', blob, 'cropped-cover.jpg')
    
    const response = await request({
      url: '/update-cover',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    
    if (response && response.success) {
      // 更新封面图片
      profileData.value.user.backgroundImgUrl = response.newImageUrl
      window.$toast?.success('封面图片更新成功！')
    } else {
      console.error('上传失败，响应:', response)
      window.$toast?.error(response?.message || '上传失败，请稍后重试')
    }
  } catch (error) {
    console.error('上传封面失败:', error)
    if (error.response) {
      console.error('错误响应:', error.response.data)
      window.$toast?.error(error.response.data?.message || '上传失败，请稍后重试')
    } else if (error.request) {
      window.$toast?.error('网络连接失败，请检查网络连接')
    } else {
      window.$toast?.error('上传失败，请稍后重试')
    }
  } finally {
    isLoading.value = false
  }
}

// 关闭裁剪界面
const closeCropper = () => {
  showCropper.value = false
  selectedImage.value = null
  croppedImageData.value = null
}

// 加载上一页
const loadPreviousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    fetchProfileData()
  }
}

// 加载下一页
const loadNextPage = () => {
  if (hasNextPage.value) {
    currentPage.value++
    fetchProfileData()
  }
}

// 跳转到指定页面
const goToPage = (page) => {
  if (page === '...' || page === currentPage.value) return
  
  const totalPages = profileData.value.articles?.pages || 1
  if (page >= 1 && page <= totalPages) {
    currentPage.value = page
    fetchProfileData()
  }
}

// 确认删除文章
const confirmDeleteArticle = (article, index) => {
  articleToDelete.value = article
  deleteModalPositionIndex.value = index
  showDeleteModal.value = true
}

// 删除文章
const deleteArticle = async () => {
  if (!articleToDelete.value) {
    window.$toast?.error('删除失败：文章信息丢失')
    return
  }
  
  // 检查文章ID，支持多种字段名
  const articleId = articleToDelete.value.id || articleToDelete.value.articleId
  if (!articleId) {
    window.$toast?.error('删除失败：文章ID无效')
    return
  }
  
  // 保存文章信息，防止在异步操作中被修改
  const articleTitle = articleToDelete.value.title
  
  // 立即关闭弹窗，防止重复点击
  showDeleteModal.value = false
  
  try {
    isLoading.value = true
    await articleAPI.deleteArticle(articleId)
    
    // 显示成功消息
    window.$toast?.success(`文章「${articleTitle}」删除成功！`)
    
    // 延迟一下再刷新页面，让用户看到成功消息
    setTimeout(() => {
      window.location.reload()
    }, 1000)
  } catch (error) {
    console.error('删除文章失败:', error)
    window.$toast?.error('删除文章失败，请稍后重试')
  } finally {
    isLoading.value = false
    // 清理状态
    articleToDelete.value = null
    deleteModalPositionIndex.value = 0
  }
}

// 关闭删除确认弹窗
const closeDeleteModal = () => {
  showDeleteModal.value = false
  articleToDelete.value = null
  deleteModalPositionIndex.value = 0
}



// 预加载关键图片资源
const preloadCriticalImages = () => {
  const criticalImages = [
    '/img/bg.jpg',
    '/img/default01.jpg',
    '/img/default.jpg'
  ]
  
  criticalImages.forEach(src => {
    const img = new Image()
    img.src = src
  })
}

// 组件挂载时获取数据
onMounted(() => {
  // 预加载关键图片
  preloadCriticalImages()
  
  // 从路由参数中获取用户名
  if (route.params.name) {
    username.value = route.params.name
    fetchProfileData()
  } else {
    // 如果没有指定用户名，则获取当前登录用户的资料
    if (authStore.isAuthenticated) {
      username.value = authStore.userName
      fetchProfileData()
    } else {
      router.push({ name: 'Login', query: { redirect: route.fullPath } })
    }
  }
})
</script>

<style scoped>
/* 页面整体样式 */
.profile-page {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  min-height: 100vh;
  position: relative;
  will-change: transform;
  transform: translateZ(0);
}

.profile-page::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 20% 80%, rgba(246, 213, 92, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(243, 167, 18, 0.1) 0%, transparent 50%);
  z-index: -1;
}

/* 封面区域样式 */
.profile-cover {
  width: 95vw;
  max-width: 1400px;
  margin: 0 auto 60px auto;
  background-size: cover;
  background-position: center;
  position: relative;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  aspect-ratio: 4/1;
  height: auto;
  border-radius: 30px;
  box-shadow: 
    0 20px 40px rgba(0,0,0,0.1),
    0 8px 16px rgba(0,0,0,0.06);
}

/* 封面骨架屏 */
.cover-skeleton {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  z-index: 1;
}

/* 预加载封面图片 */
.cover-image-preload {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 0;
}

.cover-image-preload[src] {
  opacity: 1;
}

@keyframes skeleton-loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    135deg, 
    rgba(0,0,0,0.2) 0%, 
    rgba(0,0,0,0.4) 50%,
    rgba(0,0,0,0.6) 100%
  );
  backdrop-filter: blur(2px);
}

.cover-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 20% 20%, rgba(255,255,255,0.15) 2px, transparent 2px),
    radial-gradient(circle at 80% 80%, rgba(255,255,255,0.1) 1px, transparent 1px),
    linear-gradient(45deg, transparent 40%, rgba(255,255,255,0.05) 50%, transparent 60%);
  background-size: 60px 60px, 40px 40px, 100% 100%;
  opacity: 0.3;
  z-index: 1;
  animation: patternFloat 20s ease-in-out infinite;
}

@keyframes patternFloat {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-10px) rotate(1deg); }
}



.cover-title {
  position: absolute;
  bottom: 40px;
  left: 20px;
  color: white;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
  z-index: 10;
}

.title-badge {
  position: relative;
  display: inline-block;
}

.title-badge h1 {
  font-size: 2.2rem;
  font-weight: 700;
  margin: 0;
  animation: fadeInUp 0.8s ease;
}

.title-underline {
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 4px;
  background: linear-gradient(to right, #f6d55c, #f3a712);
  border-radius: 2px;
  animation: underlineGrow 0.8s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes underlineGrow {
  from {
    width: 0;
  }
  to {
    width: 100px;
  }
}

.cover-edit-btn {
  position: absolute;
  top: 30px;
  right: 30px;
  z-index: 10;
  opacity: 0.95;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.cover-edit-btn .btn-modern {
  border-radius: 25px;
  padding: 12px 24px;
  font-weight: 600;
  font-size: 0.9rem;
  box-shadow: 
    0 8px 25px rgba(0,0,0,0.15),
    0 4px 10px rgba(0,0,0,0.1);
  background: rgba(255,255,255,0.9);
  border: 1px solid rgba(255,255,255,0.4);
  backdrop-filter: blur(10px);
  letter-spacing: 0.5px;
}

.cover-edit-btn:hover {
  transform: translateY(-3px);
}

.cover-edit-btn .btn-modern.uploading {
  background: rgba(255,255,255,0.8);
  cursor: not-allowed;
  opacity: 0.8;
}

.cover-edit-btn .btn-modern.uploading:hover {
  transform: none;
}

/* 个人资料容器 */
.profile-container {
  position: relative;
  margin-top: -40px;
  padding-bottom: 50px;
  max-width: 1400px;
  margin-left: auto;
  margin-right: auto;
}

/* 用户信息卡片 */
.profile-card {
  background-color: white;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.08);
  padding: 25px;
  text-align: center;
  margin-bottom: 30px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border: 1px solid rgba(0,0,0,0.05);
  position: relative;
}

.profile-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0,0,0,0.1);
}

.modern-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border: 1px solid rgba(0,0,0,0.03);
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  padding: 30px;
}

.modern-card .card-glow {
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  background: radial-gradient(circle, rgba(246,213,92,0.1) 0%, transparent 70%);
  border-radius: 16px;
  z-index: -1;
  opacity: 0.5;
  animation: glow 3s infinite alternate;
}

@keyframes glow {
  from {
    transform: scale(1);
    opacity: 0.5;
  }
  to {
    transform: scale(1.1);
    opacity: 0.7;
  }
}

.avatar-container {
  margin-top: -70px;
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
}

.avatar-ring {
  position: absolute;
  top: -5px;
  left: -5px;
  right: -5px;
  bottom: -5px;
  border: 2px solid rgba(246, 213, 92, 0.5);
  border-radius: 50%;
  z-index: -1;
  animation: pulseRing 3s infinite;
}

@keyframes pulseRing {
  0% {
    transform: scale(0.9);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.1);
    opacity: 1;
  }
  100% {
    transform: scale(0.9);
    opacity: 0.7;
  }
}

/* 头像骨架屏 */
.avatar-skeleton {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  border: 5px solid white;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.profile-avatar {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  border: 5px solid white;
  object-fit: cover;
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
  transition: all 0.4s ease;
  animation: float 3s infinite ease-in-out;
}

.avatar-wrapper:hover .profile-avatar {
  transform: scale(1.05);
  border-color: rgba(246, 213, 92, 0.8);
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

.avatar-status {
  position: absolute;
  bottom: 10px;
  right: 10px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: #4CAF50;
  border: 3px solid white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
  animation: pulse 2s infinite;
}

.avatar-status .status-dot {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: #4CAF50;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(76, 175, 80, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(76, 175, 80, 0); }
  100% { box-shadow: 0 0 0 0 rgba(76, 175, 80, 0); }
}

.profile-name {
  font-weight: 700;
  margin-bottom: 5px;
  font-size: 1.8rem;
  background: linear-gradient(45deg, #343a40, #495057);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: fadeIn 0.8s ease;
}

.profile-username {
  color: #6c757d;
  margin-bottom: 20px;
  font-size: 1rem;
  letter-spacing: 0.5px;
  animation: fadeIn 1s ease;
}

.profile-actions {
  margin-bottom: 25px;
  animation: fadeIn 1.2s ease;
}

.modern-btn {
  border-radius: 50px;
  padding: 10px 25px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
  background: linear-gradient(45deg, #f6d55c, #f3a712);
  border: none;
  color: white;
}

.modern-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(246, 213, 92, 0.3);
  background: linear-gradient(45deg, #f3a712, #f6d55c);
  color: white;
}

.follow-btn.followed {
  background: white;
  color: #495057;
  border: 1px solid #dee2e6;
}

.follow-btn.followed:hover {
  background: #f8f9fa;
  color: #495057;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 用户统计数据 */
.profile-stats {
  display: flex;
  justify-content: space-around;
  margin: 25px 0;
  padding: 15px 0;
  border-top: 1px solid rgba(0,0,0,0.05);
  border-bottom: 1px solid rgba(0,0,0,0.05);
  background-color: rgba(246, 213, 92, 0.03);
  border-radius: 8px;
  animation: fadeIn 1.4s ease;
}

.modern-stats .stat-item {
  text-align: center;
  position: relative;
  padding: 0 15px;
}

.modern-stats .stat-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 20%;
  height: 60%;
  width: 1px;
  background: linear-gradient(to bottom, transparent, rgba(0,0,0,0.1), transparent);
}

.modern-stats .stat-icon {
  font-size: 1.5rem;
  color: #f6d55c;
  margin-bottom: 5px;
  animation: pulseIcon 2s infinite;
}

@keyframes pulseIcon {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.stat-value {
  font-weight: 700;
  font-size: 1.5rem;
  color: #f6d55c;
  text-shadow: 0 1px 1px rgba(0,0,0,0.05);
  margin-bottom: 5px;
}

.stat-label {
  font-size: 0.9rem;
  color: #6c757d;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.profile-bio {
  padding: 20px 0;
  color: #495057;
  font-size: 1rem;
  line-height: 1.6;
  font-style: italic;
  position: relative;
  animation: fadeIn 1.6s ease;
}

.modern-bio .bio-icon {
  position: absolute;
  top: -15px;
  left: 50%;
  transform: translateX(-50%);
  background-color: #f6d55c;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  z-index: 1;
}

.profile-bio::before {
  content: '"';
  font-size: 2rem;
  color: rgba(246, 213, 92, 0.2);
  position: absolute;
  left: -10px;
  top: 5px;
}

.profile-bio::after {
  content: '"';
  font-size: 2rem;
  color: rgba(246, 213, 92, 0.2);
  position: absolute;
  right: -10px;
  bottom: 5px;
}

.profile-join-date {
  font-size: 0.9rem;
  color: #868e96;
  display: inline-block;
  padding: 8px 15px;
  border-radius: 20px;
  background-color: #f8f9fa;
  margin-top: 10px;
  animation: fadeIn 1.8s ease;
}

.modern-date .date-icon {
  font-size: 1.2rem;
  color: #f6d55c;
  margin-right: 5px;
  animation: pulseIcon 2s infinite;
}

.social-links {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 15px;
  animation: fadeIn 2s ease;
}

.modern-social .social-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f8f9fa;
  color: #6c757d;
  text-decoration: none;
  transition: all 0.3s ease;
  border: 1px solid rgba(0,0,0,0.05);
  position: relative;
}

.modern-social .social-link:hover {
  background-color: #f6d55c;
  color: white;
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(246, 213, 92, 0.3);
}

.modern-social .social-link .social-tooltip {
  visibility: hidden;
  width: 120px;
  background-color: #333;
  color: #fff;
  text-align: center;
  border-radius: 6px;
  padding: 5px 0;
  position: absolute;
  z-index: 1;
  bottom: 100%;
  left: 50%;
  margin-left: -60px;
  opacity: 0;
  transition: opacity 0.3s;
}

.modern-social .social-link:hover .social-tooltip {
  visibility: visible;
  opacity: 1;
}

/* 文章容器样式 */
.articles-container {
  margin-top: 0;
  padding-top: 20px;
}

/* 文章卡片样式 */
.loading-container {
  text-align: center;
  padding: 40px 0;
  color: #6c757d;
}

.modern-loading .loading-spinner {
  width: 50px;
  height: 50px;
  position: relative;
  margin: 0 auto 20px;
}

.modern-loading .spinner-ring {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: #f6d55c;
  opacity: 0.6;
  position: absolute;
  top: 0;
  left: 0;
  animation: bounce 2s infinite ease-in-out;
}

.modern-loading .spinner-ring:nth-child(1) { animation-delay: -1.5s; }
.modern-loading .spinner-ring:nth-child(2) { animation-delay: -1.0s; }
.modern-loading .spinner-ring:nth-child(3) { animation-delay: -0.5s; }

@keyframes bounce {
  0%, 100% { transform: scale(0.0); }
  50% { transform: scale(1.0); }
}

.article-card {
  background-color: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 5px 20px rgba(0,0,0,0.06);
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(0,0,0,0.03);
  position: relative;
  animation: fadeInUp 0.6s ease forwards;
  opacity: 0;
  transform: translateY(20px);
}

.modern-article {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border: 1px solid rgba(0,0,0,0.03);
  box-shadow: 0 5px 20px rgba(0,0,0,0.06);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.modern-article:hover {
  transform: translateY(-8px);
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
}

.article-card-inner {
  height: 100%;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.article-card:hover .article-card-inner {
  transform: translateY(-8px);
}

.article-card:before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #f6d55c, #f3a712);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.4s ease;
  z-index: 1;
}

.article-card:hover:before {
  transform: scaleX(1);
}

.article-badge {
  position: absolute;
  top: 15px;
  left: 15px;
  background-color: #f6d55c;
  color: white;
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 2;
  animation: pulseBadge 2s infinite;
}

@keyframes pulseBadge {
  0% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.1); opacity: 1; }
  100% { transform: scale(1); opacity: 0.8; }
}

.article-cover {
  height: 180px;
  background-size: cover;
  background-position: center;
  transition: all 0.5s ease;
  position: relative;
  overflow: hidden;
}

/* 文章封面骨架屏 */
.article-cover-skeleton {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  z-index: 1;
}

/* 文章封面图片 */
.article-cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: opacity 0.3s ease;
  opacity: 0;
}

.article-cover-image[src] {
  opacity: 1;
}

.modern-article .article-cover {
  height: 180px;
}

.article-cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, transparent 50%, rgba(0,0,0,0.5));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.article-card:hover .article-cover {
  height: 200px;
}

.article-card:hover .article-cover-overlay {
  opacity: 1;
}

.article-content {
  padding: 20px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  position: relative;
}

.article-title {
  font-weight: 700;
  font-size: 1.2rem;
  margin-bottom: 12px;
  color: #343a40;
  text-decoration: none;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s ease;
  line-height: 1.4;
}

.article-card:hover .article-title {
  color: #f6d55c;
}

.article-excerpt {
  color: #6c757d;
  font-size: 0.95rem;
  margin-bottom: 15px;
  flex-grow: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.6;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.85rem;
  color: #868e96;
  padding-top: 15px;
  border-top: 1px solid rgba(0,0,0,0.08);
  animation: fadeIn 1.8s ease;
  margin-top: 15px;
  position: relative;
  z-index: 2;
}

.modern-meta .article-date {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #495057;
  font-weight: 600;
  position: relative;
  z-index: 1;
  background: rgba(246, 213, 92, 0.1);
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid rgba(246, 213, 92, 0.2);
}

.modern-meta .article-date i {
  font-size: 0.8rem;
  color: #f6d55c;
}

.article-stats {
  display: flex;
  gap: 10px;
}

.article-stats .stat-item {
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.article-stats .stat-item:hover {
  color: #f6d55c;
}

.article-stats .stat-item i {
  margin-right: 4px;
}

/* 文章操作按钮样式 */
.article-actions {
  display: flex;
  gap: 8px;
  padding: 8px 12px;
  margin: 12px 0;
  border-radius: 8px;
  position: relative;
  justify-content: flex-end;
}

.action-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  font-size: 0.85rem;
  transition: all 0.2s ease;
  border: none;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  color: #6c757d;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  position: relative;
  overflow: hidden;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(0,0,0,0.15);
}

.action-btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.action-btn:focus {
  outline: none;
  transform: translateY(-1px);
}

.edit-btn {
  color: #007bff;
}

.edit-btn:hover {
  background: #007bff;
  color: white;
}

.delete-btn {
  color: #dc3545;
}

.delete-btn:hover {
  background: #dc3545;
  color: white;
}

.action-btn i {
  font-size: 0.9rem;
  transition: transform 0.2s ease;
}

.action-btn:hover i {
  transform: scale(1.1);
}



@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #6c757d;
  background-color: rgba(255,255,255,0.8);
  border-radius: 16px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
  margin: 20px 0;
  position: relative;
  overflow: hidden;
}

.modern-empty {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border: 1px solid rgba(0,0,0,0.03);
  box-shadow: 0 5px 20px rgba(0,0,0,0.06);
}

.empty-state::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, rgba(246, 213, 92, 0.3), rgba(246, 213, 92, 0.1));
}

.empty-icon {
  font-size: 5rem;
  color: rgba(246, 213, 92, 0.2);
  margin-bottom: 20px;
  animation: pulse 2s infinite ease-in-out;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.7; }
  50% { transform: scale(1.1); opacity: 1; }
  100% { transform: scale(1); opacity: 0.7; }
}

.empty-state h4 {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 15px;
  color: #495057;
}

.empty-state p {
  font-size: 1.1rem;
  margin-bottom: 25px;
  max-width: 80%;
  margin-left: auto;
  margin-right: auto;
}

.empty-state .btn {
  padding: 10px 25px;
  font-weight: 600;
  border-radius: 50px;
  box-shadow: 0 5px 15px rgba(246, 213, 92, 0.3);
  transition: all 0.3s ease;
}

.empty-state .btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(246, 213, 92, 0.4);
}

/* 分页容器 */
.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 40px 0;
  width: 100%;
  animation: fadeIn 2.2s ease;
}

.modern-pagination .pagination-btn {
  padding: 8px 20px;
  border-radius: 50px;
  font-weight: 600;
  transition: all 0.3s ease;
  border: none;
  background-color: white;
  color: #495057;
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
  margin: 0 5px;
}

.modern-pagination .pagination-btn:hover:not(:disabled) {
  background-color: #f6d55c;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(246, 213, 92, 0.3);
}

.modern-pagination .pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-weight: 600;
  color: #495057;
  background-color: rgba(246, 213, 92, 0.1);
  padding: 8px 15px;
  border-radius: 50px;
}

/* 页码按钮样式 */
.page-numbers {
  display: flex;
  gap: 5px;
  margin: 0 10px;
}

.page-number-btn {
  padding: 8px 12px;
  border-radius: 50px;
  font-weight: 600;
  transition: all 0.3s ease;
  border: none;
  background-color: white;
  color: #495057;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  min-width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.page-number-btn:hover:not(:disabled) {
  background-color: #f6d55c;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(246, 213, 92, 0.3);
}

.page-number-btn.active {
  background-color: #f6d55c;
  color: white;
  box-shadow: 0 4px 12px rgba(246, 213, 92, 0.4);
}

.page-number-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background-color: #f8f9fa;
  color: #6c757d;
}

.page-number-btn.disabled:hover {
  transform: none;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

/* 响应式调整 */
@media (max-width: 992px) {
  .profile-container {
    margin-top: -35px;
  }
  
  .avatar-container {
    margin-top: -55px;
  }
  
  .avatar-skeleton {
    width: 120px;
    height: 120px;
  }
  
  .profile-avatar {
    width: 120px;
    height: 120px;
  }
  
  .modern-article .article-cover {
    height: 180px;
  }
}

@media (max-width: 768px) {
  .profile-cover {
    width: 92vw;
    aspect-ratio: 2.8/1;
    margin-bottom: 50px;
    border-radius: 20px;
  }
  
  .profile-container {
    margin-top: -30px;
  }
  
  .avatar-container {
    margin-top: -45px;
  }
  
  .avatar-skeleton {
    width: 100px;
    height: 100px;
  }
  
  .profile-avatar {
    width: 100px;
    height: 100px;
  }
  
  .profile-name {
    font-size: 1.5rem;
  }
  
  .stat-value {
    font-size: 1.3rem;
  }
  
  .stat-label {
    font-size: 0.8rem;
  }
}

@media (max-width: 576px) {
  .profile-cover {
    width: 90vw;
    aspect-ratio: 2.5/1;
    /* 保持合适比例，高度自动调整 */
    border-radius: 15px;
  }
  
  .avatar-container {
    margin-top: -25px;
  }
  
  .avatar-skeleton {
    width: 80px;
    height: 80px;
  }
  
  .profile-avatar {
    width: 80px;
    height: 80px;
    border-width: 3px;
  }
  
  .profile-name {
    font-size: 1.3rem;
  }
  
  .profile-bio::before,
  .profile-bio::after {
    display: none;
  }
  
  /* 小屏幕上的按钮样式调整 */
  .article-actions {
    flex-direction: row;
    gap: 6px;
    padding: 6px 8px;
    justify-content: flex-end;
  }
  
  .action-btn {
    width: 32px;
    height: 32px;
    font-size: 0.8rem;
  }
  
  /* 小屏幕上的分页样式调整 */
  .pagination-container {
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .page-numbers {
    order: 2;
    width: 100%;
    justify-content: center;
    margin: 10px 0;
  }
  
  .page-number-btn {
    min-width: 35px;
    height: 35px;
    font-size: 0.85rem;
  }
}
</style>