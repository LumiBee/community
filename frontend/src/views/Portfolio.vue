<template>
  <div class="portfolio-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="container-fluid" style="max-width: 1200px;">
        <div class="hero-content">
          <h1 class="hero-title">作品集</h1>
          <p class="hero-subtitle">展示优秀的项目和创意作品</p>
        </div>
      </div>
    </section>

    <!-- Main Content -->
    <section class="content-section">
      <div class="container-fluid" style="max-width: 1200px;">
        <!-- Search and Filter Section -->
        <div class="filter-section">
          <div class="search-container">
            <i class="fas fa-search search-icon"></i>
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="搜索作品集..."
              class="search-input"
              @input="handleSearch"
            />
          </div>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>正在加载作品集...</p>
        </div>

        <!-- Error State -->
        <div v-else-if="error" class="error-state">
          <div class="error-state-icon">❌</div>
          <h3 class="error-state-title">加载失败</h3>
          <p class="error-state-text">{{ error }}</p>
          <button @click="loadPortfolios" class="btn btn-primary">重试</button>
        </div>

        <!-- Content when loaded -->
        <template v-else>
          <!-- Portfolios Grid -->
          <div class="portfolios-grid-container">
            <h3 class="section-title">所有作品集</h3>
            
            <div class="portfolios-grid" v-if="filteredPortfolios.length > 0">
              <router-link
                v-for="portfolio in filteredPortfolios"
                :key="portfolio.id"
                :to="{ name: 'PortfolioDetail', params: { id: portfolio.id } }"
                class="portfolio-card"
              >
                <div class="portfolio-image-container">
                  <img 
                    :src="portfolio.coverImgUrl || '/img/demo/demo1.jpg'" 
                    :alt="portfolio.name"
                    class="portfolio-image"
                  />
                </div>
                <div class="portfolio-card-header">
                  <h4 class="portfolio-title">{{ portfolio.name }}</h4>
                </div>
                <div class="portfolio-card-footer">
                  <div class="portfolio-stats">
                    <span class="stat-item">
                      <i class="fas fa-file-alt"></i>
                      {{ portfolio.articlesCount || 0 }} 篇文章
                    </span>
                    <span class="stat-item">
                      <i class="fas fa-calendar-alt"></i>
                      {{ formatDate(portfolio.gmtModified) }}
                    </span>
                  </div>
                  <div class="author-info">
                    <router-link :to="`/profile/${portfolio.userName}`" class="author-avatar-link">
                      <img
                        v-if="portfolio.avatarUrl"
                        :src="portfolio.avatarUrl"
                        alt="作者头像"
                        class="author-avatar"
                      />
                      <div class="author-avatar-fallback" v-else>
                        {{ (portfolio.userName || '佚名').charAt(0).toUpperCase() }}
                      </div>
                    </router-link>
                    <span class="author-name">{{ portfolio.userName || '佚名' }}</span>
                  </div>
                </div>
              </router-link>
            </div>
            
            <!-- Empty State -->
            <div v-else class="empty-state">
              <div class="empty-state-icon">🎨</div>
              <h3 class="empty-state-title">{{ searchQuery ? '未找到匹配的作品集' : '暂无作品集' }}</h3>
              <p class="empty-state-text">
                {{ searchQuery ? '尝试使用其他关键词搜索' : '还没有创建任何作品集' }}
              </p>
              <router-link to="/" class="btn btn-primary">返回首页</router-link>
            </div>
          </div>
        </template>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { portfolioAPI } from '@/api'

const router = useRouter()

// 响应式数据
const loading = ref(true)
const error = ref(null)
const portfolios = ref([])
const searchQuery = ref('')

// 计算属性
const filteredPortfolios = computed(() => {
  if (!searchQuery.value) return portfolios.value
  const query = searchQuery.value.toLowerCase()
  return portfolios.value.filter(portfolio => 
    portfolio.name.toLowerCase().includes(query) || 
    (portfolio.description && portfolio.description.toLowerCase().includes(query))
  )
})

const totalPortfolios = computed(() => portfolios.value.length)

// 方法
const loadPortfolios = async () => {
  try {
    loading.value = true
    error.value = null
    
    const response = await portfolioAPI.getAllPortfolios()
    console.log('获取到的作品集数据:', response)
    
    if (response && Array.isArray(response)) {
      // 确保每个作品集有必要的属性
      portfolios.value = response.map(portfolio => ({
        ...portfolio,
        id: portfolio.id || portfolio.portfolioId || Math.random().toString(36).substr(2, 9),
        name: portfolio.name || portfolio.title || '未命名作品集',
        articlesCount: portfolio.articlesCount || 0,
        gmtModified: portfolio.gmtModified || portfolio.createdAt || new Date().toISOString(),
        coverImgUrl: portfolio.coverImgUrl || portfolio.coverImg || null,
        userName: portfolio.userName || '佚名',
        avatarUrl: portfolio.avatarUrl || null
      }))
      console.log('处理后的作品集数据:', portfolios.value)
    } else {
      console.error('服务器返回的数据格式不正确:', response)
      portfolios.value = []
      error.value = '服务器返回的数据格式不正确'
    }
  } catch (err) {
    console.error('加载作品集失败:', err)
    portfolios.value = []
    error.value = err.message || '加载作品集失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
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

// 生命周期
onMounted(() => {
  loadPortfolios()
})
</script>

<style scoped>
/* ===== 整体布局 ===== */
.portfolio-page {
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

/* ===== Filter Section ===== */
.filter-section {
  background: white;
  border-radius: 1.5rem;
  padding: 2rem;
  margin-bottom: 3rem;
  box-shadow: 0 4px 20px rgba(255, 193, 7, 0.08);
  border: 1px solid rgba(255, 193, 7, 0.1);
  position: relative;
  overflow: hidden;
}

.filter-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #ffc107 0%, #ffda58 100%);
  border-radius: 1.5rem 1.5rem 0 0;
}

/* ===== Search ===== */
.search-container {
  position: relative;
  margin-bottom: 2rem;
}

.search-input {
  width: 100%;
  padding: 1rem 1rem 1rem 3rem;
  border: 2px solid #e2e8f0;
  border-radius: 1rem;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: white;
  box-shadow: 0 2px 8px rgba(255, 193, 7, 0.04);
}

.search-input:focus {
  outline: none;
  border-color: #ffc107;
  box-shadow: 0 0 0 3px rgba(255, 193, 7, 0.25), 0 4px 16px rgba(255, 193, 7, 0.05);
  transform: translateY(-1px);
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #ffc107;
  font-size: 1.2rem;
}

/* ===== Section Title ===== */
.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 2rem;
  position: relative;
  padding-bottom: 0.75rem;
  font-family: 'Playfair Display', Georgia, serif;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 3rem;
  height: 3px;
  background: linear-gradient(90deg, #ffc107 0%, #ffda58 100%);
  border-radius: 1.5px;
}

/* ===== Portfolios Grid ===== */
.portfolios-grid-container {
  margin-bottom: 3rem;
}

.portfolios-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  margin-bottom: 2rem;
}

/* ===== Portfolio Card ===== */
.portfolio-card {
  background: white;
  border-radius: 1.25rem;
  text-decoration: none;
  color: inherit;
  border: 1px solid rgba(255, 193, 7, 0.12);
  box-shadow: 0 4px 20px rgba(255, 193, 7, 0.08), 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.portfolio-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #ffc107 0%, #ffda58 100%);
  transform: scaleX(0);
  transition: transform 0.3s ease;
  transform-origin: left;
}

.portfolio-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(255, 193, 7, 0.15), 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: rgba(255, 193, 7, 0.3);
}

.portfolio-card:hover::before {
  transform: scaleX(1);
}

/* ===== Portfolio Image ===== */
.portfolio-image-container {
  width: 100%;
  height: 180px;
  overflow: hidden;
  border-radius: 1.25rem 1.25rem 0 0;
}

.portfolio-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.portfolio-card:hover .portfolio-image {
  transform: scale(1.05);
}

/* ===== Portfolio Card Content ===== */
.portfolio-card-header {
  padding: 1.5rem 1.5rem 0.5rem;
}

.portfolio-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  line-height: 1.3;
  transition: color 0.3s ease;
}

.portfolio-card:hover .portfolio-title {
  color: #ffc107;
}

.portfolio-card-footer {
  padding: 0.5rem 1.5rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.portfolio-stats {
  display: flex;
  gap: 1rem;
}

.portfolio-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #64748b;
  font-size: 0.875rem;
}

.portfolio-stats .stat-item i {
  color: #ffc107;
  font-size: 0.875rem;
}

/* ===== Author Info ===== */
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

.author-avatar-link {
  text-decoration: none;
  transition: transform 0.2s ease;
  display: inline-block;
}

.author-avatar-link:hover {
  transform: scale(1.05);
}

.author-avatar-fallback {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffc107 0%, #ffda58 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.author-name {
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 500;
}

/* ===== Loading State ===== */
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

/* ===== Error State ===== */
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

/* ===== Empty State ===== */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 1.5rem;
  border: 1px solid rgba(255, 193, 7, 0.08);
  margin: 2rem 0;
  position: relative;
  overflow: hidden;
}

.empty-state::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #ffc107 0%, #ffda58 100%);
  border-radius: 1.5rem 1.5rem 0 0;
}

.empty-state-icon {
  font-size: 4rem;
  margin-bottom: 1.5rem;
  opacity: 0.8;
}

.empty-state-title {
  color: #2c3e50;
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  line-height: 1.4;
}

.empty-state-text {
  color: #64748b;
  font-size: 1.1rem;
  margin-bottom: 2rem;
  line-height: 1.6;
}

/* ===== Button Styles ===== */
.btn {
  display: inline-block;
  padding: 0.75rem 1.5rem;
  border-radius: 0.75rem;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
  cursor: pointer;
  border: none;
}

.btn-primary {
  background: linear-gradient(135deg, #ffc107 0%, #ffda58 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 193, 7, 0.4);
  color: white;
  text-decoration: none;
}

/* ===== 响应式设计 ===== */
@media (max-width: 768px) {
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
  
  .filter-section {
    padding: 1.5rem;
    margin-bottom: 2rem;
  }
  
  .portfolios-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
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

.portfolio-card {
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
}

.portfolio-card:nth-child(1) { animation-delay: 0.1s; }
.portfolio-card:nth-child(2) { animation-delay: 0.2s; }
.portfolio-card:nth-child(3) { animation-delay: 0.3s; }
.portfolio-card:nth-child(4) { animation-delay: 0.4s; }
.portfolio-card:nth-child(5) { animation-delay: 0.5s; }
.portfolio-card:nth-child(6) { animation-delay: 0.6s; }
</style>