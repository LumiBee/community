<template>
  <div class="tag-articles-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="container-fluid" style="max-width: 1200px;">
        <div class="hero-content">
          <div class="tag-info">
            <span class="tag-badge">{{ tagName }}</span>
            <h1 class="hero-title">{{ tagName }} 相关文章</h1>
            <p class="hero-subtitle">
              共找到 <strong>{{ totalArticles }}</strong> 篇文章
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- Content Section -->
    <section class="content-section">
      <div class="container-fluid" style="max-width: 1200px;">
        <div class="row">
          <!-- Articles List -->
          <div class="col-md-8">
            <div class="articles-header">
              <h3 class="section-title">文章列表</h3>
              <div class="sort-controls">
                <select v-model="sortBy" @change="handleSortChange" class="sort-select">
                  <option value="latest">最新发布</option>
                  <option value="popular">最受欢迎</option>
                  <option value="views">浏览最多</option>
                </select>
              </div>
            </div>

            <!-- Loading State -->
            <div v-if="loading" class="loading-state">
              <div class="loading-spinner"></div>
              <p>正在加载文章...</p>
            </div>

            <!-- Articles Grid -->
            <div v-else-if="articles.length > 0" class="articles-grid">
              <router-link
                v-for="article in articles"
                :key="article.id"
                :to="`/article/${article.slug}`"
                class="article-card"
              >
                <div class="article-content">
                  <div class="article-header">
                    <h4 class="article-title">{{ article.title }}</h4>
                    <span class="article-date">{{ formatDate(article.gmtModified) }}</span>
                  </div>
                  <p class="article-excerpt">{{ article.excerpt }}</p>
                  <div class="article-footer">
                    <div class="author-info">
                      <img
                        v-if="article.avatarUrl"
                        :src="article.avatarUrl"
                        alt="作者头像"
                        class="author-avatar"
                      />
                      <div class="author-avatar-fallback" v-else>
                        {{ (article.userName || '佚名').charAt(0).toUpperCase() }}
                      </div>
                      <span class="author-name">{{ article.userName || '佚名' }}</span>
                    </div>
                    <div class="article-stats">
                      <span class="stat-item">
                        <i class="fas fa-eye"></i>
                        {{ article.viewCount || 0 }}
                      </span>
                      <span class="stat-item">
                        <i class="fas fa-heart"></i>
                        {{ article.likes || 0 }}
                      </span>
                    </div>
                  </div>
                </div>
              </router-link>
            </div>

            <!-- Empty State -->
            <div v-else class="empty-state">
              <div class="empty-state-icon">📄</div>
              <h3 class="empty-state-title">暂无相关文章</h3>
              <p class="empty-state-text">
                该标签下还没有发布的文章
              </p>
              <router-link to="/" class="btn btn-primary">返回首页</router-link>
            </div>

            <!-- Pagination -->
            <nav v-if="pagination.totalPages > 1" class="pagination-nav">
              <ul class="pagination">
                <li class="page-item" :class="{ disabled: pagination.current === 1 }">
                  <a class="page-link" @click.prevent="changePage(pagination.current - 1)">上一页</a>
                </li>
                <li
                  v-for="page in pageNumbers"
                  :key="page"
                  class="page-item"
                  :class="{ active: page === pagination.current }"
                >
                  <a class="page-link" @click.prevent="changePage(page)">{{ page }}</a>
                </li>
                <li class="page-item" :class="{ disabled: pagination.current === pagination.totalPages }">
                  <a class="page-link" @click.prevent="changePage(pagination.current + 1)">下一页</a>
                </li>
              </ul>
            </nav>
          </div>

          <!-- Sidebar -->
          <div class="col-md-4">
            <!-- Related Tags -->
            <div class="sidebar-section">
              <h4 class="sidebar-title">相关标签</h4>
              <div class="related-tags">
                <router-link
                  v-for="tag in relatedTags"
                  :key="tag.id"
                  :to="{ name: 'TagArticles', params: { tagName: tag.name } }"
                  class="tag-link"
                  :class="getTagColorClass(tag)"
                >
                  {{ tag.name }}
                </router-link>
              </div>
            </div>

            <!-- Popular Articles -->
            <div class="sidebar-section">
              <h4 class="sidebar-title">热门文章</h4>
              <div class="popular-articles">
                <router-link
                  v-for="article in popularArticles"
                  :key="article.id"
                  :to="`/article/${article.slug}`"
                  class="popular-article-item"
                >
                  <h5 class="popular-article-title">{{ article.title }}</h5>
                  <div class="popular-article-meta">
                    <span class="stat-item">
                      <i class="fas fa-eye"></i>
                      {{ article.viewCount || 0 }}
                    </span>
                    <span class="stat-item">
                      <i class="fas fa-heart"></i>
                      {{ article.likes || 0 }}
                    </span>
                  </div>
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { tagAPI, articleAPI } from '@/api'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(true)
const articles = ref([])
const relatedTags = ref([])
const popularArticles = ref([])
const sortBy = ref('latest')
const pagination = ref({
  current: 1,
  size: 10,
  totalPages: 1,
  total: 0
})

// 计算属性
const tagName = computed(() => route.params.tagName)
const totalArticles = computed(() => pagination.value.total)

const pageNumbers = computed(() => {
  const current = pagination.value.current
  const total = pagination.value.totalPages
  const range = []
  
  for (let i = Math.max(1, current - 2); i <= Math.min(total, current + 2); i++) {
    range.push(i)
  }
  
  return range
})

// 方法
const loadArticles = async (page = 1) => {
  try {
    loading.value = true
    // 使用encodeURIComponent确保标签名中的特殊字符被正确编码
    const response = await tagAPI.getArticlesByTagName(tagName.value, page, pagination.value.size)
    console.log('获取到的标签文章数据:', response)
    
    if (response && Array.isArray(response)) {
      articles.value = response.map(article => ({
        ...article,
        id: article.articleId || article.id,
        slug: article.slug || article.articleId,
        title: article.title || '无标题',
        excerpt: article.excerpt || article.description || '无描述',
        gmtModified: article.gmtModified || article.createdAt || new Date().toISOString(),
        viewCount: article.viewCount || 0,
        likes: article.likes || 0,
        userName: article.userName || article.author || '佚名',
        avatarUrl: article.avatarUrl || null
      }))
      console.log('处理后的文章数据:', articles.value)
      
      pagination.value = {
        current: page,
        size: pagination.value.size,
        totalPages: Math.ceil(articles.value.length / pagination.value.size) || 1,
        total: articles.value.length || 0
      }
    } else {
      console.error('服务器返回的数据格式不正确:', response)
      articles.value = []
      pagination.value = {
        current: 1,
        size: pagination.value.size,
        totalPages: 1,
        total: 0
      }
    }
  } catch (error) {
    console.error('加载文章失败:', error)
    articles.value = []
    pagination.value = {
      current: 1,
      size: pagination.value.size,
      totalPages: 1,
      total: 0
    }
  } finally {
    loading.value = false
  }
}

const loadRelatedTags = async () => {
  try {
    const response = await tagAPI.getAllTags()
    console.log('获取到的相关标签数据:', response)
    
    if (response && Array.isArray(response)) {
      relatedTags.value = response
        .map(tag => ({
          ...tag,
          id: tag.tagId || tag.id || Math.random().toString(36).substr(2, 9),
          name: tag.name || '未命名标签',
          articleCount: tag.articleCount || 0
        }))
        .filter(tag => tag.name.toLowerCase() !== tagName.value.toLowerCase())
        .sort((a, b) => (b.articleCount || 0) - (a.articleCount || 0))
        .slice(0, 8)
    } else {
      console.error('服务器返回的数据格式不正确:', response)
      relatedTags.value = []
    }
  } catch (error) {
    console.error('加载相关标签失败:', error)
    relatedTags.value = []
  }
}

const loadPopularArticles = async () => {
  try {
    const response = await articleAPI.getPopularArticles(5)
    console.log('获取到的热门文章数据:', response)
    
    if (response && Array.isArray(response)) {
      popularArticles.value = response.map(article => ({
        ...article,
        id: article.articleId || article.id,
        slug: article.slug || article.articleId,
        title: article.title || '无标题',
        viewCount: article.viewCount || 0,
        likes: article.likes || 0
      }))
    } else {
      console.error('服务器返回的数据格式不正确:', response)
      popularArticles.value = []
    }
  } catch (error) {
    console.error('加载热门文章失败:', error)
    popularArticles.value = []
  }
}

const handleSortChange = () => {
  // 根据排序重新加载文章
  loadArticles(1)
}

const changePage = (page) => {
  if (page >= 1 && page <= pagination.value.totalPages && page !== pagination.value.current) {
    loadArticles(page)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '未知日期'
  const date = new Date(dateString)
  const now = new Date()
  const diff = now - date
  
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days} 天前`
  
  return date.toLocaleDateString('zh-CN')
}

const getTagColorClass = (tag) => {
  const colorClasses = ['tag-java', 'tag-elasticsearch', 'tag-kabana', 'tag-redis', 'tag-database'];
  return colorClasses[tag.id % colorClasses.length];
}

// 监听路由变化
watch(() => route.params.tagName, (newTagName) => {
  if (newTagName) {
    loadArticles()
    loadRelatedTags()
  }
})

// 生命周期
onMounted(() => {
  loadArticles()
  loadRelatedTags()
  loadPopularArticles()
})
</script>

<style scoped>
/* ===== 整体布局 ===== */
.tag-articles-page {
  background-color: #f8f9fa;
  min-height: 100vh;
}

/* ===== Hero Section ===== */
.hero-section {
  padding: 3rem 0 2rem;
  background-color: #fff;
  position: relative;
  text-align: center;
  border-bottom: 1px solid #eaeaea;
}

.hero-content {
  position: relative;
  z-index: 2;
}

.tag-info {
  display: inline-block;
}

.tag-badge {
  display: inline-block;
  background-color: #ffc107;
  color: #333;
  padding: 0.4rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  margin-bottom: 1rem;
}

.hero-title {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
  margin: 0.5rem 0;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.hero-subtitle {
  font-size: 1.1rem;
  color: #666;
  margin-bottom: 0;
}

/* ===== Content Section ===== */
.content-section {
  padding: 2.5rem 0;
  background-color: #f8f9fa;
}

/* ===== Articles Header ===== */
.articles-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-title {
  font-size: 1.4rem;
  font-weight: 600;
  color: #333;
  margin: 0;
  position: relative;
  padding-bottom: 0.5rem;
  display: inline-block;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40px;
  height: 2px;
  background-color: #ffc107;
  border-radius: 1px;
}

.sort-controls .sort-select {
  padding: 0.5rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background-color: white;
  color: #666;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.sort-select:focus {
  outline: none;
  border-color: #ffc107;
  box-shadow: 0 0 0 3px rgba(255, 193, 7, 0.15);
}

/* ===== Articles Grid ===== */
.articles-grid {
  display: grid;
  gap: 1.25rem;
  margin-bottom: 2rem;
}

.article-card {
  text-decoration: none;
  color: inherit;
  transition: transform 0.2s ease;
}

.article-content {
  background-color: white;
  border-radius: 10px;
  padding: 1.25rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border: 1px solid #eaeaea;
  transition: all 0.2s ease;
}

.article-card:hover .article-content {
  box-shadow: 0 5px 15px rgba(0,0,0,0.08);
  border-color: #ffc107;
  transform: translateY(-3px);
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
  gap: 1rem;
}

.article-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: #333;
  margin: 0;
  line-height: 1.4;
  flex: 1;
}

.article-card:hover .article-title {
  color: #ffc107;
}

.article-date {
  font-size: 0.85rem;
  color: #888;
  white-space: nowrap;
}

.article-excerpt {
  color: #666;
  line-height: 1.6;
  margin-bottom: 1rem;
  font-size: 0.95rem;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 0.75rem;
  border-top: 1px solid #f0f0f0;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.author-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.author-avatar-fallback {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #ffc107;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.author-name {
  font-size: 0.85rem;
  color: #666;
  font-weight: 500;
}

.article-stats {
  display: flex;
  gap: 0.75rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #888;
  font-size: 0.85rem;
}

.stat-item i {
  color: #ffc107;
  font-size: 0.75rem;
}

/* ===== Sidebar ===== */
.col-md-4 {
  padding-left: 2rem;
}

.sidebar-section {
  background-color: white;
  border-radius: 10px;
  padding: 1.25rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border: 1px solid #eaeaea;
}

.sidebar-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
  position: relative;
  padding-bottom: 0.5rem;
  display: inline-block;
}

.sidebar-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 30px;
  height: 2px;
  background-color: #ffc107;
  border-radius: 1px;
}

/* ===== Related Tags ===== */
.related-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag-link {
  padding: 0.35rem 0.7rem;
  border-radius: 20px;
  text-decoration: none;
  font-size: 0.85rem;
  font-weight: 500;
  transition: all 0.2s ease;
  color: white;
}

.tag-java {
  background-color: #d0bfff;
}

.tag-elasticsearch {
  background-color: #ff8fab;
}

.tag-kabana {
  background-color: #90e0c5;
}

.tag-redis {
  background-color: #ffc478;
}

.tag-database {
  background-color: #8ecae6;
}

.tag-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
}

.tag-count {
  opacity: 0.7;
  font-size: 0.8em;
}

/* ===== Popular Articles ===== */
.popular-articles {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.popular-article-item {
  text-decoration: none;
  color: inherit;
  padding: 0.75rem;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.2s ease;
}

.popular-article-item:hover {
  background-color: #f8f9fa;
  border-color: #ffc107;
  transform: translateY(-2px);
  text-decoration: none;
}

.popular-article-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
  line-height: 1.4;
  transition: color 0.2s ease;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.popular-article-item:hover .popular-article-title {
  color: #ffc107;
}

.popular-article-meta {
  display: flex;
  gap: 0.75rem;
}

/* ===== Pagination ===== */
.pagination-nav {
  margin-top: 2rem;
}

.pagination {
  display: flex;
  justify-content: center;
  gap: 0.35rem;
  list-style: none;
  padding: 0;
  margin: 0;
}

.page-item {
  margin: 0;
}

.page-link {
  display: block;
  padding: 0.5rem 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background-color: white;
  color: #666;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s ease;
  cursor: pointer;
}

.page-link:hover {
  background-color: #ffc107;
  color: #333;
  border-color: #ffc107;
  text-decoration: none;
}

.page-item.active .page-link {
  background-color: #ffc107;
  color: #333;
  border-color: #ffc107;
}

.page-item.disabled .page-link {
  background-color: #f8f9fa;
  color: #ccc;
  cursor: not-allowed;
  border-color: #eaeaea;
}

.page-item.disabled .page-link:hover {
  background-color: #f8f9fa;
  color: #ccc;
  transform: none;
  border-color: #eaeaea;
}

/* ===== Loading & Empty States ===== */
.loading-state {
  text-align: center;
  padding: 3rem 0;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #f0f0f0;
  border-top: 3px solid #ffc107;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 3rem 2rem;
  background-color: white;
  border-radius: 10px;
  border: 1px solid #eaeaea;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.empty-state-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.empty-state-title {
  color: #333;
  font-size: 1.4rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
}

.empty-state-text {
  color: #666;
  font-size: 1rem;
  margin-bottom: 1.5rem;
}

/* ===== Button Styles ===== */
.btn {
  display: inline-block;
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s ease;
  border: none;
}

.btn-primary {
  background-color: #ffc107;
  color: #333;
  box-shadow: 0 2px 6px rgba(255, 193, 7, 0.3);
}

.btn-primary:hover {
  background-color: #ffb300;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(255, 193, 7, 0.4);
}

/* ===== 响应式设计 ===== */
@media (max-width: 768px) {
  .hero-section {
    padding: 2rem 0 1.5rem;
  }
  
  .hero-title {
    font-size: 1.6rem;
  }
  
  .content-section {
    padding: 1.5rem 0;
  }
  
  .col-md-4 {
    padding-left: 0;
    margin-top: 2rem;
  }
  
  .articles-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .pagination {
    flex-wrap: wrap;
    gap: 0.25rem;
  }
  
  .page-link {
    padding: 0.4rem 0.6rem;
    font-size: 0.85rem;
  }
}
</style>