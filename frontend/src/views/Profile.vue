<template>
  <div class="profile-page">
    <!-- 顶部封面区域 -->
    <div class="profile-cover" :style="coverStyle">
      <div class="cover-overlay"></div>
      <div class="container">
        <!-- 如果是页面所有者，显示更新封面按钮 -->
        <div v-if="isOwner" class="cover-edit-btn">
          <label for="coverUpload" class="btn btn-sm btn-light">
            <i class="fas fa-camera me-1"></i> 更换封面
          </label>
          <input 
            type="file" 
            id="coverUpload" 
            class="d-none" 
            accept="image/*"
            @change="handleCoverUpload"
          >
        </div>
      </div>
    </div>

    <!-- 个人资料区域 -->
    <div class="container profile-container">
      <div class="row">
        <div class="col-lg-3">
          <!-- 用户信息卡片 -->
          <div class="profile-card">
            <div class="avatar-container">
              <img :src="userAvatar" alt="用户头像" class="profile-avatar">
            </div>
            <div class="profile-info">
              <h3 class="profile-name">{{ profileData.user?.name || '加载中...' }}</h3>
              <p class="profile-username text-muted">@{{ profileData.user?.name || '' }}</p>
              
              <!-- 关注按钮 -->
              <div class="profile-actions">
                <button 
                  v-if="!isOwner" 
                  class="btn" 
                  :class="profileData.isFollowed ? 'btn-outline-warning' : 'btn-warning'"
                  @click="toggleFollow"
                  :disabled="isLoading"
                >
                  <i class="fas" :class="profileData.isFollowed ? 'fa-user-minus' : 'fa-user-plus'"></i>
                  {{ profileData.isFollowed ? '已关注' : '关注' }}
                </button>
                <router-link 
                  v-if="isOwner" 
                  to="/settings" 
                  class="btn btn-outline-warning"
                >
                  <i class="fas fa-cog me-1"></i> 编辑资料
                </router-link>
              </div>
              
              <!-- 用户统计数据 -->
              <div class="profile-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ profileData.articleCount || 0 }}</div>
                  <div class="stat-label">文章</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ profileData.followersCount || 0 }}</div>
                  <div class="stat-label">粉丝</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ profileData.followingCount || 0 }}</div>
                  <div class="stat-label">关注</div>
                </div>
              </div>
              
              <!-- 用户简介 -->
              <div class="profile-bio">
                <p>{{ profileData.user?.bio || '这个人很懒，什么都没有写~' }}</p>
              </div>
              
              <!-- 加入日期 -->
              <div class="profile-join-date">
                <i class="far fa-calendar-alt me-1"></i>
                加入于 {{ formatDate(profileData.user?.gmtCreate) }}
              </div>
            </div>
          </div>
        </div>
        
        <div class="col-lg-9">
          <!-- 内容导航栏 -->
          <div class="content-nav">
            <ul class="nav nav-tabs">
              <li class="nav-item">
                <a class="nav-link active" href="#">文章</a>
              </li>
              <li class="nav-item">
                <a class="nav-link" href="#">收藏</a>
              </li>
            </ul>
          </div>
          
          <!-- 文章列表 -->
          <div class="articles-container">
            <div v-if="isLoading" class="text-center py-5">
              <div class="spinner-border text-warning" role="status">
                <span class="visually-hidden">加载中...</span>
              </div>
            </div>
            
            <div v-else-if="articles.length === 0" class="empty-state">
              <div class="empty-icon">
                <i class="far fa-file-alt"></i>
              </div>
              <h4>暂无文章</h4>
              <p>{{ isOwner ? '开始创作您的第一篇文章吧！' : '该用户还没有发布任何文章' }}</p>
              <router-link v-if="isOwner" to="/publish" class="btn btn-warning">
                <i class="fas fa-pen me-1"></i> 写文章
              </router-link>
            </div>
            
            <div v-else class="row">
              <div v-for="article in articles" :key="article.id" class="col-md-6 mb-4">
                <div class="article-card">
                  <router-link :to="`/article/${article.slug}`">
                    <div class="article-cover" :style="`background-image: url(${article.coverImg || '/img/default.jpg'})`"></div>
                  </router-link>
                  <div class="article-content">
                    <router-link :to="`/article/${article.slug}`" class="article-title">
                      {{ article.title }}
                    </router-link>
                    <p class="article-excerpt">{{ article.excerpt }}</p>
                    <div class="article-meta">
                      <span class="article-date">
                        <i class="far fa-calendar-alt"></i> {{ formatDate(article.gmtCreate) }}
                      </span>
                      <span class="article-stats">
                        <i class="far fa-eye me-1"></i> {{ article.viewCount || 0 }}
                        <i class="far fa-comment ms-2 me-1"></i> {{ article.commentCount || 0 }}
                        <i class="far fa-heart ms-2 me-1"></i> {{ article.likeCount || 0 }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 分页 -->
              <div class="pagination-container">
                <button 
                  class="btn btn-sm btn-outline-secondary" 
                  @click="loadPreviousPage" 
                  :disabled="currentPage <= 1"
                >
                  <i class="fas fa-chevron-left"></i> 上一页
                </button>
                <span class="mx-3">第 {{ currentPage }} 页</span>
                <button 
                  class="btn btn-sm btn-outline-secondary" 
                  @click="loadNextPage" 
                  :disabled="!hasNextPage"
                >
                  下一页 <i class="fas fa-chevron-right"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { request } from '@/api'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const profileData = ref({})
const isLoading = ref(true)
const currentPage = ref(1)
const pageSize = ref(6)
const username = ref('')
const isOwner = computed(() => profileData.value.isOwner)
const articles = computed(() => profileData.value.articles?.records || [])
const hasNextPage = computed(() => {
  if (!profileData.value.articles) return false
  return currentPage.value < profileData.value.articles.pages
})

// 计算封面样式
const coverStyle = computed(() => {
  const coverUrl = profileData.value.user?.backgroundImgUrl || '/img/bg.jpg'
  return {
    backgroundImage: `url(${coverUrl})`
  }
})

// 计算用户头像
const userAvatar = computed(() => {
  return profileData.value.user?.avatarUrl || '/img/default01.jpg'
})

// 监听路由参数变化
watch(() => route.params.name, (newName) => {
  if (newName) {
    username.value = newName
    fetchProfileData()
  }
})

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  })
}

// 获取个人资料数据
const fetchProfileData = async () => {
  isLoading.value = true
  try {
    const response = await request({
      url: `/api/profile/${username.value}`,
      method: 'get',
      params: { page: currentPage.value, size: pageSize.value }
    })
    profileData.value = response
    console.log('Profile data:', response)
  } catch (error) {
    console.error('获取个人资料失败:', error)
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
    await request({
      url: `/api/user/${profileData.value.user.id}/follow`,
      method: 'post'
    })
    // 更新关注状态
    profileData.value.isFollowed = !profileData.value.isFollowed
    // 更新粉丝数
    if (profileData.value.isFollowed) {
      profileData.value.followersCount++
    } else {
      profileData.value.followersCount--
    }
  } catch (error) {
    console.error('关注操作失败:', error)
  } finally {
    isLoading.value = false
  }
}

// 上传封面图片
const handleCoverUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const formData = new FormData()
  formData.append('coverImageFile', file)
  
  try {
    isLoading.value = true
    const response = await request({
      url: '/update-cover',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.success) {
      // 更新封面图片
      profileData.value.user.backgroundImgUrl = response.newImageUrl
    }
  } catch (error) {
    console.error('上传封面失败:', error)
  } finally {
    isLoading.value = false
    // 清空文件输入，允许再次选择同一文件
    event.target.value = ''
  }
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

// 组件挂载时获取数据
onMounted(() => {
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
  background-color: #f8f9fa;
  min-height: 100vh;
}

/* 封面区域样式 */
.profile-cover {
  height: 240px;
  background-size: cover;
  background-position: center;
  position: relative;
  margin-bottom: 80px;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(rgba(0,0,0,0.1), rgba(0,0,0,0.4));
}

.cover-edit-btn {
  position: absolute;
  bottom: 20px;
  right: 20px;
  z-index: 10;
}

/* 个人资料容器 */
.profile-container {
  position: relative;
  margin-top: -80px;
}

/* 用户信息卡片 */
.profile-card {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  padding: 20px;
  text-align: center;
  margin-bottom: 20px;
}

.avatar-container {
  margin-top: -60px;
  margin-bottom: 15px;
  display: flex;
  justify-content: center;
}

.profile-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 4px solid white;
  object-fit: cover;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.profile-name {
  font-weight: 600;
  margin-bottom: 5px;
}

.profile-username {
  color: #6c757d;
  margin-bottom: 15px;
}

.profile-actions {
  margin-bottom: 20px;
}

/* 用户统计数据 */
.profile-stats {
  display: flex;
  justify-content: space-around;
  margin: 20px 0;
  padding: 10px 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-weight: 600;
  font-size: 1.2rem;
  color: #343a40;
}

.stat-label {
  font-size: 0.85rem;
  color: #6c757d;
}

.profile-bio {
  padding: 10px 0;
  color: #495057;
  font-size: 0.95rem;
}

.profile-join-date {
  font-size: 0.85rem;
  color: #6c757d;
}

/* 内容导航栏 */
.content-nav {
  margin-bottom: 20px;
}

.nav-tabs .nav-link {
  color: #495057;
  border: none;
  padding: 10px 15px;
  font-weight: 500;
}

.nav-tabs .nav-link.active {
  color: #f6d55c;
  border-bottom: 2px solid #f6d55c;
  background-color: transparent;
}

/* 文章卡片样式 */
.article-card {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transition: transform 0.2s, box-shadow 0.2s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.article-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.article-cover {
  height: 160px;
  background-size: cover;
  background-position: center;
}

.article-content {
  padding: 15px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.article-title {
  font-weight: 600;
  font-size: 1.1rem;
  margin-bottom: 10px;
  color: #343a40;
  text-decoration: none;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-excerpt {
  color: #6c757d;
  font-size: 0.9rem;
  margin-bottom: 15px;
  flex-grow: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #6c757d;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #6c757d;
}

.empty-icon {
  font-size: 3rem;
  color: #dee2e6;
  margin-bottom: 15px;
}

/* 分页容器 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin: 30px 0;
  width: 100%;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .profile-container {
    margin-top: -60px;
  }
  
  .avatar-container {
    margin-top: -50px;
  }
  
  .profile-avatar {
    width: 100px;
    height: 100px;
  }
}

@media (max-width: 768px) {
  .profile-cover {
    height: 180px;
    margin-bottom: 60px;
  }
  
  .profile-container {
    margin-top: -40px;
  }
  
  .avatar-container {
    margin-top: -40px;
  }
  
  .profile-avatar {
    width: 80px;
    height: 80px;
  }
}
</style>