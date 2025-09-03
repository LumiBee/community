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
                <div class="custom-select" @click="toggleDropdown" ref="selectContainer">
                  <div class="select-display">
                    <span class="select-text">{{ getSortText(sortBy) }}</span>
                    <i class="fas fa-chevron-down select-arrow" :class="{ 'rotated': isDropdownOpen }"></i>
                  </div>
                  <div class="select-dropdown" v-show="isDropdownOpen">
                    <div 
                      v-for="option in sortOptions" 
                      :key="option.value"
                      class="select-option"
                      :class="{ 'selected': sortBy === option.value }"
                      @click.stop="selectOption(option.value)"
                    >
                      {{ option.label }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Loading State -->
            <div v-if="loading" class="loading-state">
              <div class="loading-spinner"></div>
              <p>正在加载文章...</p>
            </div>

            <!-- Articles List -->
            <ol v-else-if="articles.length > 0" class="list-unstyled compact-article-list">
              <li
                v-for="article in articles"
                :key="article.id"
                class="compact-article-item"
                @click="$router.push(`/article/${article.slug}`)"
              >
                <div class="compact-article-content">
                  <h6 class="compact-article-title">
                    <span class="text-dark">
                      {{ article.title }}
                    </span>
                  </h6>
                  <p class="compact-article-excerpt">{{ article.excerpt }}</p>
                  <div class="compact-article-meta">
                    <div class="compact-author-info">
                      <router-link 
                        :to="`/profile/${article.userName}`" 
                        class="author-avatar-link"
                        @click.stop
                      >
                        <img
                          v-if="article.avatarUrl"
                          :src="getAuthorAvatarUrl(article.avatarUrl)"
                          alt="作者头像"
                          class="compact-author-avatar"
                        />
                        <div class="compact-author-avatar" v-else>
                          {{ (article.userName || '佚名').charAt(0).toUpperCase() }}
                        </div>
                      </router-link>
                      <span class="compact-author-name">{{ article.userName || '佚名' }}</span>
                      <span class="compact-time">{{ formatTime(article.gmtModified) }}</span>
                    </div>
                    <div class="compact-stats">
                      <span class="compact-stat-item">
                        <i class="fas fa-eye"></i>
                        <span>{{ article.viewCount || 0 }}</span>
                      </span>
                      <span class="compact-stat-item">
                        <i class="fas fa-heart"></i>
                        <span>{{ article.likes || 0 }}</span>
                      </span>
                    </div>
                  </div>
                </div>
              </li>
            </ol>

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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { tagAPI, articleAPI } from '@/api'
import { getAuthorAvatarUrl } from '@/utils/avatar-helper'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(true)
const articles = ref([])
const relatedTags = ref([])
const popularArticles = ref([])
const sortBy = ref('latest')
const isDropdownOpen = ref(false)
const pagination = ref({
  current: 1,
  size: 10,
  totalPages: 1,
  total: 0
})

// 排序选项
const sortOptions = [
  { value: 'latest', label: '最新发布' },
  { value: 'popular', label: '最受欢迎' },
  { value: 'views', label: '浏览最多' }
]

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
    const response = await tagAPI.getArticlesByTagSlug(tagName.value, page, pagination.value.size)
    
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

const toggleDropdown = (event) => {
  event.stopPropagation()
  isDropdownOpen.value = !isDropdownOpen.value
}

const selectOption = (value) => {
  sortBy.value = value
  isDropdownOpen.value = false
  handleSortChange()
}

const getSortText = (value) => {
  const option = sortOptions.find(opt => opt.value === value)
  return option ? option.label : '最新发布'
}

const changePage = (page) => {
  if (page >= 1 && page <= pagination.value.totalPages && page !== pagination.value.current) {
    loadArticles(page)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const formatTime = (dateString) => {
  if (!dateString) return '日期未知'
  
  const now = new Date()
  const date = new Date(dateString)
  const diff = now - date
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days > 0) return `${days} 天前`
  if (hours > 0) return `${hours} 小时前`
  if (minutes > 0) return `${minutes} 分钟前`
  return '刚刚'
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
  
  // 添加点击外部关闭下拉框的监听器
  document.addEventListener('click', handleClickOutside)
})

// 组件卸载时移除监听器
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

const handleClickOutside = (event) => {
  const selectContainer = document.querySelector('.custom-select')
  if (selectContainer && !selectContainer.contains(event.target)) {
    isDropdownOpen.value = false
  }
}
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

.sort-controls {
  position: relative;
}

/* 自定义下拉框样式 */
.custom-select {
  position: relative;
  min-width: 140px;
  cursor: pointer;
  user-select: none;
  /* 重置Bootstrap可能的影响 */
  margin: 0;
  border: none;
  background: none;
  padding: 0;
}

.select-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.6rem 1.2rem;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  background-color: white;
  color: #333;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  /* 确保没有双重边框 */
  box-sizing: border-box;
  width: 100%;
}

.custom-select:hover .select-display {
  border-color: #ffc107;
  box-shadow: 0 4px 8px rgba(255, 193, 7, 0.15);
  transform: translateY(-1px);
}

.select-text {
  flex: 1;
}

.select-arrow {
  margin-left: 0.5rem;
  transition: transform 0.3s ease;
  color: #666;
  font-size: 0.8rem;
}

.select-arrow.rotated {
  transform: rotate(180deg);
}

.select-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background-color: white;
  border: 2px solid #ffc107;
  border-top: none;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  margin-top: -2px;
  overflow: hidden;
}

.select-option {
  padding: 0.8rem 1.2rem;
  font-size: 0.9rem;
  font-weight: 500;
  color: #333;
  background-color: white;
  transition: all 0.2s ease;
  border-bottom: 1px solid #f0f0f0;
}

.select-option:last-child {
  border-bottom: none;
}

.select-option:hover {
  background-color: #fff8e1;
  color: #333;
}

.select-option.selected {
  background-color: #ffc107;
  color: #333;
  font-weight: 600;
}

.select-option.selected:hover {
  background-color: #ffb300;
}

/* ===== Compact Articles List ===== */
.compact-article-list {
  margin: 0;
  padding: 0;
}

.compact-article-item {
  padding: 1.5rem;
  border: 1px solid #eaedf1;
  border-radius: 12px;
  transition: all 0.3s ease;
  margin-bottom: 1.5rem;
  position: relative;
  background-color: #ffffff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
  cursor: pointer;
}

.compact-article-item:last-child {
  border-bottom: none;
}

.compact-article-item:hover {
  background: linear-gradient(135deg, rgba(255, 255, 255, 1) 0%, rgba(255, 252, 240, 1) 100%);
  transform: translateY(-5px);
  border-color: rgba(255, 193, 7, 0.2);
  box-shadow: 0 10px 20px rgba(255, 193, 7, 0.1);
}

.compact-article-content {
  width: 100%;
}

.compact-article-title {
  margin: 0 0 0.8rem 0;
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.compact-article-title span {
  color: #2c3e50;
  text-decoration: none;
  transition: color 0.3s ease;
}

.compact-article-item:hover .compact-article-title span {
  color: #ffc107;
}

.compact-article-excerpt {
  color: #64748b;
  font-size: 1.15rem;
  line-height: 1.6;
  margin: 0.8rem 0 1.2rem 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.compact-article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f1f5f9;
}

.compact-author-info {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.compact-author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 193, 7, 0.2);
  margin-right: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 0.9rem;
  background-color: #ffc107;
}

.compact-author-name {
  font-size: 1rem;
  color: #4b5563;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}

.compact-time {
  font-size: 0.9rem;
  color: #9ca3af;
  margin-left: 0.5rem;
}

.compact-stats {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-left: auto;
}

.compact-stat-item {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 1rem;
  color: #64748b;
  margin-left: 10px;
  background-color: transparent;
  padding: 0.3rem 0.6rem;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.compact-stat-item i {
  color: #ffc107;
  font-size: 0.95rem;
  opacity: 0.9;
}

.compact-article-item:hover .compact-stat-item i {
  opacity: 1;
  transform: scale(1.1);
}

.compact-article-item:hover .compact-stat-item {
  color: #ffc107;
  background-color: transparent;
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