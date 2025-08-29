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
                    :src="getAuthorAvatarUrl(portfolio.avatarUrl)" 
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
            <div class="custom-select" @click="toggleDropdown" ref="selectContainer">
              <div class="select-display">
                <span class="select-text">{{ sortOptions[currentSort].label }}</span>
                <i class="fas fa-chevron-down select-arrow" :class="{ 'rotated': isDropdownOpen }"></i>
              </div>
              <div class="select-dropdown" v-show="isDropdownOpen">
                <div 
                  v-for="(option, key) in sortOptions" 
                  :key="key"
                  class="select-option"
                  :class="{ 'selected': currentSort === key }"
                  @click.stop="selectOption(key)"
                >
                  {{ option.label }}
                </div>
              </div>
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
          <div class="error-actions">
            <button @click="loadPortfolioDetails" class="btn btn-primary me-2" v-if="portfolioId && portfolioId !== 'undefined'">重试</button>
            <router-link to="/portfolios" class="btn btn-outline-secondary">返回作品集列表</router-link>
          </div>
        </div>
        
        <!-- 文章列表 -->
        <div v-else>
          <div v-if="filteredArticles.length === 0" class="empty-state modern-empty">
            <div class="empty-icon">
              <i class="far fa-file-alt"></i>
            </div>
            <h4>暂无文章</h4>
            <p>{{ searchQuery ? '未找到匹配的文章' : '该作品集中还没有任何文章' }}</p>
            <router-link to="/portfolio" class="btn btn-outline-secondary mt-3">返回作品集列表</router-link>
          </div>
          
          <div v-else class="row">
            <div v-for="(article, index) in filteredArticles" :key="article.id || article.articleId" class="col-lg-4 col-md-6 mb-4">
              <div class="article-card modern-article" :style="{animationDelay: index * 0.1 + 's'}">
                <div class="article-card-inner">
                  <div class="article-badge">文章</div>
                  <router-link :to="`/article/${article.slug || article.id}`">
                    <div class="article-cover">
                      <!-- 文章封面图片 -->
                      <img 
                        v-if="article.coverImg" 
                        :src="article.coverImg" 
                        :alt="`${article.title}的封面图片`"
                        class="article-cover-image"
                        loading="lazy"
                      />
                      <!-- 默认封面 -->
                      <img 
                        v-else 
                        src="/img/default.jpg" 
                        :alt="`${article.title}的默认封面`"
                        class="article-cover-image"
                        loading="lazy"
                      />
                      <div class="article-cover-overlay"></div>
                    </div>
                  </router-link>
                  <div class="article-content">
                    <router-link :to="`/article/${article.slug || article.id}`" class="article-title">
                      {{ article.title }}
                    </router-link>
                    <p class="article-excerpt">{{ article.excerpt || article.description || article.content?.substring(0, 100) || '暂无描述' }}</p>
                    
                    <!-- 作者信息 -->
                    <div class="article-author-info">
                      <div class="author-avatar">
                        <img 
                          :src="getAvatarSrc(article, portfolio)" 
                          :alt="getAuthorName(article, portfolio)"
                          @error="onAvatarError"
                          @load="onAvatarLoad"
                        />
                      </div>
                      <div class="author-details">
                        <span class="author-name">{{ getAuthorName(article, portfolio) }}</span>
                        <span class="publish-date">{{ formatDate(article.gmtCreate || article.gmtModified) }}</span>
                      </div>
                    </div>
                    
                    <div class="article-meta modern-meta">
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
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { portfolioAPI } from '@/api'
import { getAuthorAvatarUrl } from '@/utils/avatar-helper'

const route = useRoute()
const router = useRouter()
const portfolioId = ref(route.params.id || null)

// 响应式数据
const loading = ref(true)
const error = ref(null)
const portfolio = ref(null)
const articles = ref([])
const searchQuery = ref('')
const currentSort = ref('newest')
const isDropdownOpen = ref(false)

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
    // 验证portfolioId
    if (!portfolioId.value || portfolioId.value === 'undefined') {
      error.value = '无效的作品集ID'
      loading.value = false
      return
    }
    
    loading.value = true
    error.value = null
    
    const response = await portfolioAPI.getPortfolioById(portfolioId.value)
    
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
      } else {
        // 如果没有文章，则设置为空数组
        articles.value = []
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
  // 重新排序文章
  const currentArticles = [...articles.value]
  articles.value = currentArticles.sort(sortOptions[sortKey].sorter)
}

const toggleDropdown = (event) => {
  event.stopPropagation()
  isDropdownOpen.value = !isDropdownOpen.value
}

const selectOption = (key) => {
  currentSort.value = key
  isDropdownOpen.value = false
  changeSort(key)
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

// 获取作者姓名
const getAuthorName = (article, portfolio) => {
  return article.user?.name || portfolio?.userName || portfolio?.user?.name || '未知作者'
}

// 获取头像源
const getAvatarSrc = (article, portfolio) => {
  const avatarUrl = article.user?.avatarUrl || portfolio?.avatarUrl || portfolio?.user?.avatarUrl
  
  if (!avatarUrl) {
    return '/img/default01.jpg'
  }
  
  // 如果是相对路径，直接返回
  if (avatarUrl.startsWith('/')) {
    return avatarUrl
  }
  
  // 如果是完整URL，使用getAuthorAvatarUrl处理
  return getAuthorAvatarUrl(avatarUrl)
}

// 头像加载处理
const onAvatarLoad = (event) => {
  // 头像加载成功
  event.target.style.opacity = '1'
}

const onAvatarError = (event) => {
  // 头像加载失败，使用默认头像
  event.target.src = '/img/default01.jpg'
  event.target.style.opacity = '1'
}

// 监听路由参数变化，重新加载数据
watch(() => route.params.id, (newId) => {
  if (newId && newId !== 'undefined') {
    portfolioId.value = newId
    loadPortfolioDetails()
  }
}, { immediate: true })

// 生命周期
onMounted(() => {
  if (portfolioId.value && portfolioId.value !== 'undefined') {
    loadPortfolioDetails()
  }
  
  // 添加点击外部关闭下拉框的监听器
  document.addEventListener('click', handleClickOutside)
})

// 组件卸载时移除监听器
onUnmounted(() => {
  if (window.contentCheckIntervalRef) {
    clearInterval(window.contentCheckIntervalRef)
  }
  if (window.handleResizeRef) {
    window.removeEventListener('resize', window.handleResizeRef)
  }
  
  // 移除点击外部关闭下拉框的监听器
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

/* ===== 现代文章卡片样式 ===== */
.modern-article {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border: 1px solid rgba(0,0,0,0.03);
  box-shadow: 0 5px 20px rgba(0,0,0,0.06);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border-radius: 1rem;
  overflow: hidden;
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
  height: 140px;
  background-size: cover;
  background-position: center;
  transition: all 0.5s ease;
  position: relative;
  overflow: hidden;
}

.article-cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: opacity 0.3s ease;
}

.article-cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.3) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.article-card:hover .article-cover-overlay {
  opacity: 1;
}

.article-content {
  padding: 1.25rem;
}

.article-title {
  color: #1a202c;
  text-decoration: none;
  font-size: 1.1rem;
  font-weight: 600;
  line-height: 1.4;
  margin-bottom: 0.75rem;
  display: block;
  transition: color 0.3s ease;
}

.article-title:hover {
  color: #f6d55c;
}

.article-excerpt {
  color: #64748b;
  font-size: 0.9rem;
  line-height: 1.5;
  margin-bottom: 1rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 作者信息样式 */
.article-author-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
  padding: 0.75rem;
  background: rgba(248, 249, 250, 0.8);
  border-radius: 0.75rem;
  border: 1px solid rgba(0,0,0,0.05);
}

.article-author-info .author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  position: relative;
  background: #f1f5f9;
  border: 2px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.article-author-info .author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 50%;
  min-width: 100%;
  min-height: 100%;
}

.article-author-info .author-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
}

.article-author-info .author-name {
  font-size: 0.85rem;
  font-weight: 600;
  color: #1a202c;
}

.article-author-info .publish-date {
  font-size: 0.75rem;
  color: #94a3b8;
}

.modern-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.article-date {
  color: #94a3b8;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.article-stats {
  display: flex;
  gap: 1rem;
}

.stat-item {
  color: #94a3b8;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

/* ===== 搜索和排序区域 ===== */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: white;
  border-radius: 1rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0;
}

.section-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.search-container {
  position: relative;
}

.search-container input {
  padding: 0.6rem 1.2rem;
  border: 2px solid #e2e8f0;
  border-radius: 20px;
  background-color: white;
  color: #333;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  min-width: 200px;
}

.search-container input:focus {
  outline: none;
  border-color: #ffc107;
  box-shadow: 0 4px 8px rgba(255, 193, 7, 0.15);
  transform: translateY(-1px);
}

/* 自定义下拉框样式 */
.custom-select {
  position: relative;
  min-width: 140px;
  cursor: pointer;
  user-select: none;
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

/* ===== 空状态样式 ===== */
.modern-empty {
  text-align: center;
  padding: 4rem 2rem;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 1.5rem;
  border: 1px solid rgba(0,0,0,0.05);
  margin: 2rem 0;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1.5rem;
  color: #cbd5e1;
}

.empty-icon i {
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
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

.modern-article {
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
  opacity: 0;
}

.modern-article.fade-in {
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
  
  .article-content {
    padding: 1rem;
  }
  
  .article-title {
    font-size: 1rem;
  }
  
  .article-excerpt {
    font-size: 0.85rem;
  }
  
  .article-author-info {
    padding: 0.5rem;
    gap: 0.5rem;
  }
  
  .article-author-info .author-avatar {
    width: 28px;
    height: 28px;
  }
  
  .article-author-info .author-name {
    font-size: 0.8rem;
  }
  
  .article-author-info .publish-date {
    font-size: 0.7rem;
  }
  
  .modern-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
}
</style>