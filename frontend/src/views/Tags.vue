<template>
  <div class="tags-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="container-fluid" style="max-width: 1200px;">
        <div class="hero-content">
          <h1 class="hero-title">标签库</h1>
          <p class="hero-subtitle">探索不同主题的文章，发现感兴趣的内容</p>
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
              placeholder="搜索标签..."
              class="search-input"
              @input="handleSearch"
            />
          </div>
        </div>

        <!-- Popular Tags -->
        <div class="popular-tags-section" v-if="popularTags.length > 0">
          <h3 class="section-title">热门标签</h3>
          <div class="popular-tags">
            <router-link
              v-for="tag in popularTags"
              :key="tag.id"
              :to="{ name: 'TagArticles', params: { tagName: tag.name } }"
              class="popular-tag"
              :class="getTagColorClass(tag)"
            >
              {{ tag.name }}
            </router-link>
          </div>
        </div>

        <!-- Tags Grid -->
        <div class="tags-grid-container">
          <h3 class="section-title">所有标签</h3>
          
          <div class="tags-grid" v-if="filteredTags.length > 0">
            <router-link
              v-for="tag in filteredTags"
              :key="tag.id"
              :to="{ name: 'TagArticles', params: { tagName: tag.name } }"
              class="tag-card"
              @click="selectTag(tag)"
            >
              <div class="tag-card-inner">
                <div class="tag-name-container">
                  <h4 class="tag-name">{{ tag.name }}</h4>
                  <span class="tag-count">{{ tag.articleCount }}</span>
                </div>
                <div class="tag-stats">
                  <span class="stat-item">
                    <i class="fas fa-file-alt"></i>
                    {{ tag.articleCount }} 篇文章
                  </span>
                  <span class="stat-item">
                    <i class="fas fa-calendar-alt"></i>
                    {{ formatDate(tag.createdAt) }}
                  </span>
                </div>
              </div>
            </router-link>
          </div>
          
          <!-- Empty State -->
          <div v-else class="empty-state">
            <div class="empty-state-icon">🏷️</div>
            <h3 class="empty-state-title">{{ searchQuery ? '未找到匹配的标签' : '暂无标签' }}</h3>
            <p class="empty-state-text">
              {{ searchQuery ? '尝试使用其他关键词搜索' : '还没有创建任何标签' }}
            </p>
            <router-link to="/" class="btn btn-primary">返回首页</router-link>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { tagAPI } from '@/api'

const router = useRouter()

// 响应式数据
const loading = ref(true)
const tags = ref([])
const searchQuery = ref('')
const activeFilters = ref([])

// 计算属性
const filteredTags = computed(() => {
  if (!searchQuery.value) return tags.value
  return tags.value.filter(tag => 
    tag.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    (tag.description && tag.description.toLowerCase().includes(searchQuery.value.toLowerCase()))
  )
})

const totalTags = computed(() => tags.value.length)
const totalArticles = computed(() => tags.value.reduce((sum, tag) => sum + (tag.articleCount || 0), 0))
const popularTags = computed(() => 
  [...tags.value]
    .sort((a, b) => (b.articleCount || 0) - (a.articleCount || 0))
    .slice(0, 12)
)

// 方法
const loadTags = async () => {
  try {
    loading.value = true
    const response = await tagAPI.getAllTags()
    
    if (response && Array.isArray(response)) {
      // 确保每个标签有必要的属性
      tags.value = response.map(tag => ({
        ...tag,
        id: tag.tagId || tag.id || Math.random().toString(36).substr(2, 9),
        name: tag.name || '未命名标签',
        articleCount: tag.articleCount || 0,
        description: tag.description || '',
        createdAt: tag.gmtCreate || tag.createdAt || new Date().toISOString()
      }))
    } else {
      console.error('服务器返回的数据格式不正确:', response)
      tags.value = []
    }
  } catch (error) {
    console.error('加载标签失败:', error)
    tags.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

const selectTag = (tag) => {
  // 可以添加标签选择逻辑
}

const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.getFullYear() + '年' + (date.getMonth() + 1) + '月' + date.getDate() + '日'
}

const getTagColorClass = (tag) => {
  const colorClasses = ['tag-java', 'tag-elasticsearch', 'tag-kabana', 'tag-redis', 'tag-database'];
  return colorClasses[tag.id % colorClasses.length];
}

// 生命周期
onMounted(() => {
  loadTags()
})
</script>

<style scoped>
/* ===== 整体布局 ===== */
.tags-page {
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

/* ===== Filter Section ===== */
.filter-section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
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
  border-radius: 12px 12px 0 0;
}

/* ===== Search ===== */
.search-container {
  position: relative;
}

.search-input {
  width: 100%;
  padding: 0.8rem 1rem 0.8rem 2.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.2s ease;
  background: white;
}

.search-input:focus {
  outline: none;
  border-color: #ffc107;
  box-shadow: 0 0 0 3px rgba(255, 193, 7, 0.15);
}

.search-icon {
  position: absolute;
  left: 0.8rem;
  top: 50%;
  transform: translateY(-50%);
  color: #aaa;
  font-size: 1rem;
}

/* ===== Section Title ===== */
.section-title {
  font-size: 1.4rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1.5rem;
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
  background: #ffc107;
  border-radius: 1px;
}

/* ===== Popular Tags ===== */
.popular-tags-section {
  margin-bottom: 2.5rem;
}

.popular-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
}

.popular-tag {
  display: inline-block;
  padding: 0.5rem 1.2rem;
  border-radius: 2rem;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s ease;
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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

.popular-tag:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

/* ===== Tags Grid ===== */
.tags-grid-container {
  margin-bottom: 3rem;
}

.tags-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.25rem;
}

/* ===== Tag Card ===== */
.tag-card {
  text-decoration: none;
  color: inherit;
  transition: transform 0.2s ease;
}

.tag-card-inner {
  background: white;
  border-radius: 1.25rem;
  padding: 1.25rem;
  height: 100%;
  box-shadow: 0 4px 20px rgba(255, 193, 7, 0.08), 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 193, 7, 0.12);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
}

.tag-card-inner::before {
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

.tag-card:hover .tag-card-inner {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(255, 193, 7, 0.15), 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: rgba(255, 193, 7, 0.3);
}

.tag-card:hover .tag-card-inner::before {
  transform: scaleX(1);
}

.tag-name-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.tag-name {
  font-size: 1.15rem;
  font-weight: 600;
  color: #333;
  margin: 0;
  line-height: 1.3;
  transition: color 0.3s ease;
}

.tag-card:hover .tag-name {
  color: #ffc107;
}

.tag-count {
  background-color: #fff8e6;
  color: #ffa000;
  padding: 0.25rem 0.6rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  border: 1px solid #ffe0b2;
}

.tag-stats {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  color: #666;
  font-size: 0.85rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.stat-item i {
  color: #ffc107;
  font-size: 0.8rem;
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
    font-size: 1.8rem;
  }
  
  .hero-subtitle {
    font-size: 1rem;
  }
  
  .content-section {
    padding: 1.5rem 0;
  }
  
  .tags-grid {
    grid-template-columns: 1fr;
  }
  
  .popular-tags {
    gap: 0.6rem;
  }
  
  .popular-tag {
    padding: 0.4rem 1rem;
    font-size: 0.9rem;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .tags-grid {
    grid-template-columns: repeat(2, 1fr);
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

.tag-card {
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
}

.tag-card:nth-child(1) { animation-delay: 0.1s; }
.tag-card:nth-child(2) { animation-delay: 0.2s; }
.tag-card:nth-child(3) { animation-delay: 0.3s; }
.tag-card:nth-child(4) { animation-delay: 0.4s; }
.tag-card:nth-child(5) { animation-delay: 0.5s; }
.tag-card:nth-child(6) { animation-delay: 0.6s; }
.tag-card:nth-child(7) { animation-delay: 0.7s; }
.tag-card:nth-child(8) { animation-delay: 0.8s; }
.tag-card:nth-child(9) { animation-delay: 0.9s; }

/* 搜索框动画 */
.search-container {
  animation: fadeInUp 0.6s ease-out 0.3s both;
}

/* 热门标签动画 */
.popular-tags-section {
  animation: fadeInUp 0.6s ease-out 0.1s both;
}

.popular-tag {
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
}

.popular-tag:nth-child(1) { animation-delay: 0.1s; }
.popular-tag:nth-child(2) { animation-delay: 0.15s; }
.popular-tag:nth-child(3) { animation-delay: 0.2s; }
.popular-tag:nth-child(4) { animation-delay: 0.25s; }
.popular-tag:nth-child(5) { animation-delay: 0.3s; }
.popular-tag:nth-child(6) { animation-delay: 0.35s; }
.popular-tag:nth-child(7) { animation-delay: 0.4s; }
.popular-tag:nth-child(8) { animation-delay: 0.45s; }
.popular-tag:nth-child(9) { animation-delay: 0.5s; }
.popular-tag:nth-child(10) { animation-delay: 0.55s; }
.popular-tag:nth-child(11) { animation-delay: 0.6s; }
.popular-tag:nth-child(12) { animation-delay: 0.65s; }
</style>