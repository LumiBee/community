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
      <div class="cover-overlay"></div>
      <div class="container position-relative h-100">
        <!-- 如果是页面所有者，显示更新封面按钮 -->
        <div v-if="isOwner" class="cover-edit-btn">
          <label for="coverUpload" class="btn btn-light">
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
        
        <!-- 封面标题 -->
        <div class="cover-title">
          <h1 v-if="profileData.user?.name">{{ profileData.user.name }}的个人空间</h1>
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
              <div class="avatar-wrapper">
                <img :src="userAvatar" alt="用户头像" class="profile-avatar">
                <div class="avatar-status" v-if="isOwner"></div>
              </div>
            </div>
            <div class="profile-info">
              <h3 class="profile-name">{{ profileData.user?.name || '加载中...' }}</h3>
              <p class="profile-username text-muted">@{{ profileData.user?.name || '' }}</p>
              
              <!-- 关注按钮 -->
              <div class="profile-actions">
                <button 
                  v-if="!isOwner" 
                  class="btn follow-btn" 
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
                  class="btn edit-profile-btn"
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
              
              <!-- 社交链接 -->
              <div class="social-links">
                <a href="#" class="social-link" title="GitHub"><i class="fab fa-github"></i></a>
                <a href="#" class="social-link" title="微博"><i class="fab fa-weibo"></i></a>
                <a href="#" class="social-link" title="知乎"><i class="fab fa-zhihu"></i></a>
              </div>
            </div>
          </div>
        </div>
        
        <div class="col-lg-9">
          <!-- 内容导航栏 -->
          <div class="content-nav">
            <ul class="nav nav-tabs">
              <li class="nav-item">
                <a class="nav-link active" href="#">
                  <i class="fas fa-pen-nib me-1"></i> 文章
                </a>
              </li>
              <li class="nav-item">
                <a class="nav-link" href="#">
                  <i class="fas fa-bookmark me-1"></i> 收藏
                </a>
              </li>
              <li class="nav-item" v-if="isOwner">
                <a class="nav-link" href="#">
                  <i class="fas fa-file-alt me-1"></i> 草稿
                </a>
              </li>
            </ul>
          </div>
          
          <!-- 文章列表 -->
          <div class="articles-container">
            <div v-if="isLoading" class="loading-container">
              <div class="spinner">
                <div class="double-bounce1"></div>
                <div class="double-bounce2"></div>
              </div>
              <p>加载中...</p>
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
              <div v-for="(article, index) in articles" :key="article.id" class="col-md-6 mb-4">
                <div class="article-card" :style="{animationDelay: index * 0.1 + 's'}">
                  <div class="article-card-inner">
                    <router-link :to="`/article/${article.slug}`">
                      <div class="article-cover" :style="`background-image: url(${article.coverImg || '/img/default.jpg'})`">
                        <div class="article-cover-overlay"></div>
                      </div>
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
                          <span class="stat-item" title="阅读量"><i class="far fa-eye"></i> {{ article.viewCount || 0 }}</span>
                          <span class="stat-item" title="评论数"><i class="far fa-comment"></i> {{ article.commentCount || 0 }}</span>
                          <span class="stat-item" title="点赞数"><i class="far fa-heart"></i> {{ article.likeCount || 0 }}</span>
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 分页 -->
              <div class="pagination-container">
                <button 
                  @click="loadPreviousPage" 
                  :disabled="currentPage <= 1"
                >
                  <i class="fas fa-chevron-left"></i> 上一页
                </button>
                <span>第 {{ currentPage }} 页</span>
                <button 
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
const errorMessage = ref('') // 添加错误信息
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
    console.log(`正在获取用户 ${username.value} 的个人资料数据...`)
    const response = await request({
      url: `/api/profile/${username.value}`,
      method: 'get',
      params: { page: currentPage.value, size: pageSize.value },
      timeout: 30000 // 增加超时时间到30秒
    })
    profileData.value = response
    console.log('成功获取个人资料数据:', response)
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
  background-image: linear-gradient(to bottom, #f8f9fa, #ffffff);
}

/* 封面区域样式 */
.profile-cover {
  height: 300px;
  background-size: cover;
  background-position: center;
  position: relative;
  margin-bottom: 80px;
  transition: all 0.5s ease;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.5) 100%);
  backdrop-filter: blur(1px);
}

.cover-title {
  position: absolute;
  bottom: 40px;
  left: 20px;
  color: white;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
  z-index: 10;
}

.cover-title h1 {
  font-size: 2.2rem;
  font-weight: 700;
  margin: 0;
  animation: fadeInUp 0.8s ease;
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

.cover-edit-btn {
  position: absolute;
  bottom: 20px;
  right: 20px;
  z-index: 10;
  opacity: 0.9;
  transition: all 0.3s ease;
}

.cover-edit-btn .btn {
  border-radius: 50px;
  padding: 8px 16px;
  font-weight: 600;
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.cover-edit-btn:hover {
  transform: translateY(-3px);
}

/* 个人资料容器 */
.profile-container {
  position: relative;
  margin-top: -80px;
  padding-bottom: 50px;
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
}

.profile-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0,0,0,0.1);
}

.avatar-container {
  margin-top: -80px;
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
}

.profile-avatar {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  border: 5px solid white;
  object-fit: cover;
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
  transition: all 0.4s ease;
}

.avatar-wrapper:hover .profile-avatar {
  transform: scale(1.05);
  border-color: rgba(246, 213, 92, 0.8);
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

.follow-btn, .edit-profile-btn {
  border-radius: 50px;
  padding: 8px 20px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
  background: linear-gradient(45deg, #f6d55c, #f3a712);
  border: none;
  color: white;
}

.follow-btn:hover, .edit-profile-btn:hover {
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
}

.stat-item {
  text-align: center;
  position: relative;
  padding: 0 15px;
}

.stat-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 20%;
  height: 60%;
  width: 1px;
  background: linear-gradient(to bottom, transparent, rgba(0,0,0,0.1), transparent);
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
  animation: fadeIn 1.6s ease;
}

.social-links {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 15px;
  animation: fadeIn 1.8s ease;
}

.social-link {
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
}

.social-link:hover {
  background-color: #f6d55c;
  color: white;
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(246, 213, 92, 0.3);
}

/* 内容导航栏 */
.content-nav {
  margin-bottom: 30px;
  position: relative;
}

.nav-tabs {
  border-bottom: none;
  display: inline-flex;
  background-color: white;
  border-radius: 50px;
  padding: 5px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}

.nav-tabs .nav-link {
  color: #495057;
  border: none;
  padding: 12px 25px;
  font-weight: 600;
  border-radius: 50px;
  transition: all 0.3s ease;
  margin: 0 5px;
  font-size: 0.95rem;
  letter-spacing: 0.5px;
}

.nav-tabs .nav-link:hover:not(.active) {
  background-color: rgba(246, 213, 92, 0.1);
  color: #495057;
}

.nav-tabs .nav-link.active {
  color: #fff;
  background-color: #f6d55c;
  box-shadow: 0 5px 15px rgba(246, 213, 92, 0.3);
}

/* 文章卡片样式 */
.loading-container {
  text-align: center;
  padding: 40px 0;
  color: #6c757d;
}

.spinner {
  width: 50px;
  height: 50px;
  position: relative;
  margin: 0 auto 20px;
}

.double-bounce1, .double-bounce2 {
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

.double-bounce2 {
  animation-delay: -1.0s;
}

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

.article-cover {
  height: 180px;
  background-size: cover;
  background-position: center;
  transition: all 0.5s ease;
  position: relative;
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
  margin-bottom: 20px;
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
  font-size: 0.85rem;
  color: #868e96;
  padding-top: 15px;
  border-top: 1px solid rgba(0,0,0,0.05);
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
}

.pagination-container button {
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

.pagination-container button:hover:not(:disabled) {
  background-color: #f6d55c;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(246, 213, 92, 0.3);
}

.pagination-container button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-container span {
  font-weight: 600;
  color: #495057;
  background-color: rgba(246, 213, 92, 0.1);
  padding: 8px 15px;
  border-radius: 50px;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .profile-container {
    margin-top: -60px;
  }
  
  .avatar-container {
    margin-top: -60px;
  }
  
  .profile-avatar {
    width: 120px;
    height: 120px;
  }
  
  .nav-tabs .nav-link {
    padding: 10px 15px;
    font-size: 0.9rem;
  }
  
  .article-card:hover .article-cover {
    height: 180px;
  }
}

@media (max-width: 768px) {
  .profile-cover {
    height: 220px;
    margin-bottom: 60px;
  }
  
  .profile-container {
    margin-top: -50px;
  }
  
  .avatar-container {
    margin-top: -50px;
  }
  
  .profile-avatar {
    width: 100px;
    height: 100px;
  }
  
  .profile-name {
    font-size: 1.5rem;
  }
  
  .nav-tabs {
    width: 100%;
    justify-content: center;
  }
  
  .nav-tabs .nav-link {
    padding: 8px 12px;
    font-size: 0.85rem;
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
    height: 180px;
  }
  
  .avatar-container {
    margin-top: -40px;
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
}
</style>