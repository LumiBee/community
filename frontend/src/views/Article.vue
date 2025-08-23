<template>
  <div class="article-page">
    <div class="container-fluid" style="max-width: 1200px;">
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-warning" role="status">
          <span class="visually-hidden">加载中...</span>
        </div>
      </div>
      
      <div v-else-if="article" class="row">
        <!-- 文章内容 -->
        <div class="col-lg-8">
          <article class="article-content-wrapper">
            <!-- 文章头部 -->
            <header class="article-header mb-4" data-aos="fade-up">
              <h1 class="article-title">{{ article.title }}</h1>
              
              <div class="article-meta d-flex align-items-center justify-content-between">
                <div class="author-info d-flex align-items-center">
                  <img
                    :src="article.avatarUrl || '/img/default01.jpg'"
                    alt="作者头像"
                    class="author-avatar me-3"
                  />
                  <div>
                    <div class="author-name">{{ article.userName || '匿名' }}</div>
                    <div class="publish-time text-muted small">
                      发布于 {{ formatDate(article.gmtCreate) }}
                      <span v-if="article.gmtModified && article.gmtModified !== article.gmtCreate">
                        · 更新于 {{ formatDate(article.gmtModified) }}
                      </span>
                    </div>
                  </div>
                </div>
                
                <div class="article-stats d-flex align-items-center">
                  <span class="stat-item me-3">
                    <i class="bi bi-eye me-1"></i>
                    {{ article.viewCount || 0 }}
                  </span>
                  <span class="stat-item">
                    <i class="bi bi-heart me-1"></i>
                    {{ article.likes || 0 }}
                  </span>
                </div>
              </div>
            </header>
            
            <!-- 文章内容 -->
            <div class="article-content" data-aos="fade-up" data-aos-delay="200">
              <div v-html="renderedContent" class="markdown-content"></div>
            </div>
            
            <!-- 文章操作 -->
            <div class="article-actions mt-4 pt-4 border-top" data-aos="fade-up" data-aos-delay="400">
              <div class="d-flex justify-content-between align-items-center">
                <div class="action-buttons">
                  <button
                    @click="toggleLike"
                    :class="['btn', article.isLiked ? 'btn-danger' : 'btn-outline-danger']"
                    :disabled="!authStore.isAuthenticated"
                  >
                    <i class="bi bi-heart me-1"></i>
                    {{ article.isLiked ? '已点赞' : '点赞' }} ({{ article.likes || 0 }})
                  </button>
                  
                  <button
                    @click="toggleFavorite"
                    :class="['btn ms-2', article.isFavorited ? 'btn-warning' : 'btn-outline-warning']"
                    :disabled="!authStore.isAuthenticated"
                  >
                    <i class="bi bi-star me-1"></i>
                    {{ article.isFavorited ? '已收藏' : '收藏' }}
                  </button>
                </div>
                
                <div class="share-buttons">
                  <button class="btn btn-outline-secondary btn-sm" @click="shareArticle">
                    <i class="bi bi-share me-1"></i>
                    分享
                  </button>
                </div>
              </div>
            </div>
          </article>
        </div>
        
        <!-- 侧边栏 -->
        <div class="col-lg-4">
          <div class="sidebar-sticky">
            <!-- 作者信息 -->
            <div class="author-card card mb-4" data-aos="fade-left">
              <div class="card-body text-center">
                <img
                  :src="article.avatarUrl || '/img/default01.jpg'"
                  alt="作者头像"
                  class="author-avatar-large mb-3"
                />
                <h5 class="author-name">{{ article.userName || '匿名' }}</h5>
                <p class="author-bio text-muted">{{ article.userBio || '这个用户很懒，什么都没有留下...' }}</p>
                
                <div class="author-stats mb-3">
                  <div class="row text-center">
                    <div class="col-4">
                      <div class="stat-number">{{ article.userArticleCount || 0 }}</div>
                      <div class="stat-label">文章</div>
                    </div>
                    <div class="col-4">
                      <div class="stat-number">{{ article.userFollowersCount || 0 }}</div>
                      <div class="stat-label">粉丝</div>
                    </div>
                    <div class="col-4">
                      <div class="stat-number">{{ article.userFollowingCount || 0 }}</div>
                      <div class="stat-label">关注</div>
                    </div>
                  </div>
                </div>
                
                <button
                  v-if="authStore.isAuthenticated && article.userId !== authStore.user?.id"
                  @click="toggleFollow"
                  :class="['btn', article.isFollowed ? 'btn-secondary' : 'btn-warning']"
                >
                  {{ article.isFollowed ? '已关注' : '关注' }}
                </button>
              </div>
            </div>
            
            <!-- 相关文章 -->
            <div v-if="relatedArticles.length > 0" class="related-articles card" data-aos="fade-left" data-aos-delay="200">
              <div class="card-header">
                <h6 class="mb-0">相关文章</h6>
              </div>
              <div class="card-body p-0">
                <router-link
                  v-for="relatedArticle in relatedArticles"
                  :key="relatedArticle.id"
                  :to="`/article/${relatedArticle.slug}`"
                  class="related-article-item"
                >
                  <div class="d-flex">
                    <div class="related-article-content">
                      <h6 class="related-article-title">{{ relatedArticle.title }}</h6>
                      <div class="related-article-meta">
                        <small class="text-muted">
                          <i class="bi bi-eye me-1"></i>{{ relatedArticle.viewCount || 0 }}
                          <i class="bi bi-heart ms-2 me-1"></i>{{ relatedArticle.likes || 0 }}
                        </small>
                      </div>
                    </div>
                  </div>
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="text-center py-5">
        <h3>文章未找到</h3>
        <p class="text-muted">抱歉，您访问的文章不存在或已被删除。</p>
        <router-link to="/" class="btn btn-warning">返回首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { articleAPI, userAPI } from '@/api'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const route = useRoute()
const authStore = useAuthStore()

// 响应式数据
const loading = ref(true)
const article = ref(null)
const relatedArticles = ref([])
const renderedContent = ref('')

// 获取文章数据
const loadArticle = async () => {
  try {
    loading.value = true
    const slug = route.params.slug
    
    const response = await articleAPI.getArticleBySlug(slug)
    article.value = response.data
    
    // 渲染Markdown内容
    if (article.value?.content) {
      const rawHtml = marked.parse(article.value.content)
      renderedContent.value = DOMPurify.sanitize(rawHtml)
    }
    
    // 获取相关文章
    if (article.value?.id) {
      const relatedResponse = await articleAPI.getRelatedArticles(article.value.id)
      relatedArticles.value = relatedResponse.data || []
    }
    
  } catch (error) {
    console.error('加载文章失败:', error)
  } finally {
    loading.value = false
  }
}

// 点赞功能
const toggleLike = async () => {
  if (!authStore.isAuthenticated) {
    return
  }
  
  try {
    const response = await articleAPI.toggleLike(article.value.id)
    article.value.isLiked = response.data.isLiked
    article.value.likes = response.data.totalLikes
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

// 收藏功能
const toggleFavorite = async () => {
  if (!authStore.isAuthenticated) {
    return
  }
  
  try {
    const response = await articleAPI.toggleFavorite(article.value.id)
    article.value.isFavorited = response.data.isFavorited
  } catch (error) {
    console.error('收藏失败:', error)
  }
}

// 关注功能
const toggleFollow = async () => {
  if (!authStore.isAuthenticated) {
    return
  }
  
  try {
    const response = await userAPI.toggleFollow(article.value.userId)
    article.value.isFollowed = response.data.isFollowed
  } catch (error) {
    console.error('关注失败:', error)
  }
}

// 分享功能
const shareArticle = () => {
  if (navigator.share) {
    navigator.share({
      title: article.value.title,
      text: article.value.excerpt,
      url: window.location.href
    })
  } else {
    // 复制链接到剪贴板
    navigator.clipboard.writeText(window.location.href)
    alert('链接已复制到剪贴板')
  }
}

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

// 生命周期
onMounted(() => {
  loadArticle()
})
</script>

<style scoped>
.article-page {
  padding: 2rem 0;
}

.article-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2d3748;
  line-height: 1.2;
  margin-bottom: 1.5rem;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.author-avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}

.author-name {
  font-weight: 600;
  color: #2d3748;
}

.article-content {
  margin: 2rem 0;
}

.markdown-content {
  line-height: 1.8;
  color: #4a5568;
}

.markdown-content h1,
.markdown-content h2,
.markdown-content h3,
.markdown-content h4,
.markdown-content h5,
.markdown-content h6 {
  margin-top: 2rem;
  margin-bottom: 1rem;
  font-weight: 600;
  color: #2d3748;
}

.markdown-content p {
  margin-bottom: 1rem;
}

.markdown-content img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 1rem 0;
}

.markdown-content blockquote {
  border-left: 4px solid #f6d55c;
  padding-left: 1rem;
  margin: 1.5rem 0;
  color: #718096;
  font-style: italic;
}

.markdown-content code {
  background: #f7fafc;
  padding: 0.2rem 0.4rem;
  border-radius: 4px;
  font-family: 'Fira Code', monospace;
  font-size: 0.875rem;
}

.markdown-content pre {
  background: #2d3748;
  color: white;
  padding: 1rem;
  border-radius: 8px;
  overflow-x: auto;
}

.markdown-content pre code {
  background: none;
  color: inherit;
  padding: 0;
}

.author-card {
  border: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2d3748;
}

.stat-label {
  font-size: 0.875rem;
  color: #718096;
}

.related-article-item {
  display: block;
  padding: 1rem;
  border-bottom: 1px solid #e2e8f0;
  text-decoration: none;
  color: inherit;
  transition: background 0.2s ease;
}

.related-article-item:hover {
  background: #f7fafc;
  text-decoration: none;
  color: inherit;
}

.related-article-item:last-child {
  border-bottom: none;
}

.related-article-title {
  font-size: 0.9rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

.sidebar-sticky {
  position: sticky;
  top: 100px;
}

@media (max-width: 768px) {
  .article-title {
    font-size: 2rem;
  }
  
  .article-meta {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 1rem;
  }
  
  .sidebar-sticky {
    position: static;
    margin-top: 2rem;
  }
}
</style>
