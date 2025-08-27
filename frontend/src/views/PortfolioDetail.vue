<template>
  <div class="portfolio-detail-page">
    <!-- 标题区域 -->
    <div class="hero-section">
      <div class="container-fluid" style="max-width: 1200px;">
        <div class="hero-content">
          <h1 class="hero-title">{{ portfolio?.name || '作品集详情' }}</h1>
          <p class="hero-subtitle">{{ portfolio?.description || '查看该作品集中的所有文章' }}</p>
        </div>
      </div>
    </div>
    
    <!-- 作品集详情区域 -->
    <section class="content-section">
      <div class="container-fluid" style="max-width: 1200px;">
        <!-- 作品集封面和信息 -->
        <div class="portfolio-header" v-if="portfolio">
          <img 
            :src="portfolio.coverImgUrl || '/img/demo/demo1.jpg'" 
            alt="作品集封面" 
            class="portfolio-cover"
          />
          <div class="portfolio-overlay"></div>
          <div class="portfolio-info">
            <h2 class="portfolio-title">{{ portfolio.name }}</h2>
            <p class="portfolio-description">{{ portfolio.description || '暂无描述' }}</p>
            <div class="portfolio-meta">
              <div class="portfolio-author">
                <router-link :to="`/profile/${portfolio.userName}`" class="author-avatar-link">
                  <img 
                    :src="portfolio.avatarUrl || '/img/default.jpg'" 
                    alt="作者头像" 
                    class="author-avatar"
                  />
                </router-link>
                <span>{{ portfolio.userName || '未知作者' }}</span>
              </div>
              <div class="portfolio-date">
                <i class="fas fa-calendar-alt"></i>
                <span>{{ formatDate(portfolio.gmtCreate || portfolio.gmtModified) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 搜索和排序区域 -->
        <div class="section-header">
          <h3 class="section-title">文章列表</h3>
          <div class="section-actions">
            <div class="search-container">
              <input 
                type="text" 
                id="articleSearchInput" 
                placeholder="搜索文章..." 
                v-model="searchQuery"
                @input="handleSearch"
              />
            </div>
            <div class="sort-dropdown dropdown">
              <button 
                class="btn dropdown-toggle" 
                type="button" 
                id="sortDropdown" 
                data-bs-toggle="dropdown" 
                aria-expanded="false"
              >
                <i class="fas fa-sort"></i> {{ sortOptions[currentSort].label }}
              </button>
              <ul class="dropdown-menu" aria-labelledby="sortDropdown">
                <li v-for="(option, key) in sortOptions" :key="key">
                  <a 
                    class="dropdown-item" 
                    href="#" 
                    @click.prevent="changeSort(key)"
                    :class="{ active: currentSort === key }"
                  >
                    {{ option.label }}
                  </a>
                </li>
              </ul>
            </div>
          </div>
        </div>
        
        <!-- 加载中状态 -->
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>正在加载文章...</p>
        </div>
        
        <!-- 错误状态 -->
        <div v-else-if="error" class="error-state">
          <div class="error-state-icon">❌</div>
          <h3 class="error-state-title">加载失败</h3>
          <p class="error-state-text">{{ error }}</p>
          <button @click="loadPortfolioDetails" class="btn btn-primary">重试</button>
        </div>
        
        <!-- 文章列表 -->
        <div v-else>
          <!-- 文章网格 -->
          <div class="article-grid stagger-children" v-if="filteredArticles.length > 0">
                          <router-link
                v-for="(article, index) in filteredArticles"
                :key="article.id"
                :to="{ name: 'Article', params: { slug: article.slug || article.id } }"
                class="article-card"
                :style="{ '--child-index': index }"
                @mouseenter="article.hover = true"
                @mouseleave="article.hover = false"
              >
              <div class="article-cover">
                <img 
                  :src="article.coverImg || '/img/demo/blog5.jpg'" 
                  :alt="article.title"
                />
              </div>
              <div class="card-content">
                <h4 class="article-title">{{ article.title }}</h4>
                <p class="article-excerpt">{{ article.description || article.content?.substring(0, 150) || '暂无描述' }}</p>
                <div class="article-meta">
                  <div class="article-author">
                    <img 
                      :src="article.user?.avatarUrl || '/img/default.jpg'" 
                      :alt="article.user?.name || '未知作者'" 
                    />
                    <span class="author-name">{{ article.user?.name || '未知作者' }}</span>
                  </div>
                  <div class="article-date">
                    <i class="fas fa-calendar-alt"></i>
                    <span>{{ formatDate(article.gmtCreate || article.gmtModified) }}</span>
                  </div>
                </div>
              </div>
            </router-link>
          </div>
          
          <!-- 空状态 -->
          <div v-else class="empty-state">
            <div class="empty-icon">
              <i class="fas fa-file-alt"></i>
            </div>
            <h3 class="empty-title">{{ searchQuery ? '未找到匹配的文章' : '暂无文章' }}</h3>
            <p class="empty-description">
              {{ searchQuery ? '尝试使用其他关键词搜索' : '该作品集中还没有添加任何文章' }}
            </p>
            <router-link to="/portfolio" class="btn btn-primary mt-3">返回作品集列表</router-link>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { portfolioAPI } from '@/api'

const route = useRoute()
const router = useRouter()
const portfolioId = computed(() => route.params.id)

// 响应式数据
const loading = ref(true)
const error = ref(null)
const portfolio = ref(null)
const articles = ref([])
const searchQuery = ref('')
const currentSort = ref('newest')

// 排序选项
const sortOptions = {
  newest: { label: '最新发布', sorter: (a, b) => new Date(b.gmtCreate || b.gmtModified) - new Date(a.gmtCreate || a.gmtModified) },
  oldest: { label: '最早发布', sorter: (a, b) => new Date(a.gmtCreate || a.gmtModified) - new Date(b.gmtCreate || b.gmtModified) },
  title: { label: '按标题排序', sorter: (a, b) => a.title.localeCompare(b.title) }
}

// 计算属性
const filteredArticles = computed(() => {
  // 先过滤
  let result = searchQuery.value
    ? articles.value.filter(article => 
        article.title?.toLowerCase().includes(searchQuery.value.toLowerCase()) || 
        article.description?.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
        article.content?.toLowerCase().includes(searchQuery.value.toLowerCase())
      )
    : [...articles.value]
  
  // 再排序
  return result.sort(sortOptions[currentSort.value].sorter)
})

// 方法
const loadPortfolioDetails = async () => {
  try {
    loading.value = true
    error.value = null
    
    const response = await portfolioAPI.getPortfolioById(portfolioId.value)
    console.log('获取到的作品集详情:', response)
    
    if (response) {
      portfolio.value = response
      
      // 如果作品集中包含文章列表
      if (response.articles && Array.isArray(response.articles)) {
        articles.value = response.articles.map(article => ({
          ...article,
          // 确保文章有 slug 属性，如果没有则使用 id 作为 slug
          slug: article.slug || `article-${article.id}`,
          hover: false // 添加hover状态用于动画
        }))
        console.log('处理后的文章数据:', articles.value)
      } else {
        // 如果没有文章，则设置为空数组
        articles.value = []
        console.log('作品集中没有文章')
      }
    } else {
      error.value = '未找到该作品集'
    }
  } catch (err) {
    console.error('加载作品集详情失败:', err)
    error.value = err.message || '加载作品集详情失败，请稍后重试'
  } finally {
    loading.value = false
    initAnimations()
  }
}

const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

const changeSort = (sortKey) => {
  currentSort.value = sortKey
}

const formatDate = (dateString) => {
  if (!dateString) return '未知'
  try {
    const date = new Date(dateString)
    return date.getFullYear() + '年' + (date.getMonth() + 1) + '月' + date.getDate() + '日'
  } catch (e) {
    console.error('日期格式化错误:', e)
    return '未知'
  }
}

const initAnimations = () => {
  setTimeout(() => {
    // 为文章卡片添加淡入动画
    const cards = document.querySelectorAll('.article-card')
    cards.forEach(card => {
      card.classList.add('fade-in')
    })
  }, 100)
}

// 监听路由参数变化，重新加载数据
watch(() => route.params.id, (newId) => {
  if (newId) {
    loadPortfolioDetails()
  }
}, { immediate: true })

// 生命周期
onMounted(() => {
  if (portfolioId.value) {
    loadPortfolioDetails()
  }
})
</script>

<style scoped>
@import '/css/portfolio-detail.css';

/* ===== 整体布局 ===== */
.portfolio-detail-page {
  padding: 0 0 2rem 0;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
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
  padding: 3rem 0;
  background: white;
  margin-top: -1rem;
  border-radius: 2rem 2rem 0 0;
  box-shadow: 0 -8px 32px rgba(255, 193, 7, 0.08);
}

/* ===== 加载状态 ===== */
.loading-state {
  text-align: center;
  padding: 4rem 2rem;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 1.5rem;
  border: 1px solid rgba(255, 193, 7, 0.08);
  margin: 2rem 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #ffc107;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1.5rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* ===== 错误状态 ===== */
.error-state {
  text-align: center;
  padding: 4rem 2rem;
  background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
  border-radius: 1.5rem;
  border: 1px solid rgba(229, 62, 62, 0.1);
  margin: 2rem 0;
}

.error-state-icon {
  font-size: 4rem;
  margin-bottom: 1.5rem;
  color: #e53e3e;
}

.error-state-title {
  color: #e53e3e;
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
}

.error-state-text {
  color: #64748b;
  font-size: 1.1rem;
  margin-bottom: 2rem;
}

/* ===== 头像样式 ===== */
.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.author-avatar-link {
  text-decoration: none;
  transition: transform 0.2s ease;
  display: inline-block;
}

.author-avatar-link:hover {
  transform: scale(1.05);
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

.article-card {
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
  opacity: 0;
}

.article-card.fade-in {
  opacity: 1;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .hero-section {
    padding: 2rem 0;
  }
  
  .hero-title {
    font-size: 2rem;
  }
  
  .hero-subtitle {
    font-size: 1rem;
  }
  
  .content-section {
    padding: 2rem 0;
    border-radius: 1rem 1rem 0 0;
  }
  
  .portfolio-header {
    height: 220px;
  }
}
</style>