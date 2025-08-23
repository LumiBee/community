<template>
  <div class="main-content-wrapper">
    <div class="container-fluid" style="max-width: 1400px; width: 100%;">
      <!-- 轮播图 -->
      <div
        id="carouselExampleCaptions"
        class="carousel slide"
        data-bs-ride="carousel"
        style="height: 300px; margin-bottom: 20px;"
      >
        <div class="carousel-indicators">
          <button
            v-for="(article, index) in featuredArticles"
            :key="index"
            type="button"
            data-bs-target="#carouselExampleCaptions"
            :data-bs-slide-to="index"
            :class="{ active: index === 0 }"
            aria-current="true"
            :aria-label="`Slide ${index + 1}`"
          ></button>
        </div>
        
        <div class="carousel-inner">
          <div
            v-for="(article, index) in featuredArticles"
            :key="article.id"
            :class="['carousel-item', { active: index === 0 }]"
            @click="$router.push(`/article/${article.slug}`)"
            style="cursor: pointer;"
          >
            <img
              :src="article.backgroundUrl || '/img/demo/1.jpg'"
              class="d-block w-100"
              :alt="article.title"
              style="height: 300px; object-fit: cover;"
            />
            <div class="carousel-caption d-none d-md-block" style="background-color: rgba(0,0,0,0.5); border-radius: 10px; padding: 15px;">
              <h5>{{ article.title }}</h5>
              <p>{{ article.excerpt }}</p>
            </div>
          </div>
          
          <!-- 默认轮播图 -->
          <div v-if="!featuredArticles.length" class="carousel-item active">
            <img src="/img/demo/1.jpg" class="d-block w-100" alt="默认轮播图1" style="height: 300px; object-fit: cover;" />
            <div class="carousel-caption d-none d-md-block">
              <h5>欢迎来到Lumi Hive</h5>
              <p>您的知识分享社区</p>
            </div>
          </div>
          <div v-if="!featuredArticles.length" class="carousel-item">
            <img src="/img/demo/2.jpg" class="d-block w-100" alt="默认轮播图2" style="height: 300px; object-fit: cover;" />
            <div class="carousel-caption d-none d-md-block">
              <h5>分享您的知识</h5>
              <p>创建文章，分享您的专业知识和见解</p>
            </div>
          </div>
          <div v-if="!featuredArticles.length" class="carousel-item">
            <img src="/img/demo/3.jpg" class="d-block w-100" alt="默认轮播图3" style="height: 300px; object-fit: cover;" />
            <div class="carousel-caption d-none d-md-block">
              <h5>探索精彩内容</h5>
              <p>浏览各种主题的优质文章</p>
            </div>
          </div>
        </div>
        
        <button class="carousel-control-prev" type="button" data-bs-target="#carouselExampleCaptions" data-bs-slide="prev">
          <span class="carousel-control-prev-icon" aria-hidden="true"></span>
          <span class="visually-hidden">Previous</span>
        </button>
        <button class="carousel-control-next" type="button" data-bs-target="#carouselExampleCaptions" data-bs-slide="next">
          <span class="carousel-control-next-icon" aria-hidden="true"></span>
          <span class="visually-hidden">Next</span>
        </button>
      </div>
    </div>

    <div class="container-fluid" style="max-width: 1400px; margin-top: 20px;">
      <div class="row justify-content-between">
        <!-- 主要内容区域 -->
        <div class="col-md-8">
          <h5 class="font-weight-bold spanborder">
            <span>所有文章</span>
          </h5>
          
          <!-- 文章列表 -->
          <div v-if="articles.length > 0">
                          <router-link
                v-for="article in articles"
                :key="article.id"
                :to="`/article/${article.slug}`"
                class="article-link"
                style="display: block; text-decoration: none; color: inherit;"
              >
                <div class="card mb-4 box-shadow article-card" data-aos="fade-up">
                  <div class="card-body d-flex flex-column">
                    <div>
                      <h2 class="mb-1 h4 font-weight-bold article-title">
                        <a class="text-dark">{{ article.title }}</a>
                      </h2>
                      <p class="card-text mb-auto article-excerpt">{{ article.excerpt }}</p>
                    </div>
                    <div class="mt-3 d-flex justify-content-between align-items-center w-100 article-meta">
                      <div class="d-flex align-items-center author-info">
                        <img
                          v-if="article.avatarUrl"
                          :src="article.avatarUrl"
                          alt="作者头像"
                          class="author-avatar"
                        />
                        <div class="author-avatar" v-else>
                          {{ (article.userName || '佚名').charAt(0).toUpperCase() }}
                        </div>
                        <small class="text-muted">{{ article.userName || '佚名' }}</small>
                        <small class="text-muted ms-2">
                          {{ formatTime(article.gmtModified) }}
                        </small>
                      </div>
                      <div class="article-stats">
                        <span class="stat-item">
                          <i class="bi bi-eye"></i>
                          <small>{{ article.viewCount || 0 }}</small>
                        </span>
                        <span class="stat-item ms-3">
                          <i class="bi bi-heart"></i>
                          <small>{{ article.likes || 0 }}</small>
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </router-link>
          </div>
          
          <!-- 无文章提示 -->
          <div v-else-if="!loading">
            <p>暂时还没有文章哦，敬请期待！</p>
          </div>
          
          <!-- 分页 -->
          <nav v-if="pagination.totalPages > 1" aria-label="Page navigation" class="mt-4">
            <ul class="pagination justify-content-center">
              <li class="page-item" :class="{ disabled: pagination.current === 1 }">
                <a class="page-link" href="#" @click.prevent="changePage(pagination.current - 1)">上一页</a>
              </li>
              
              <li v-if="startPage > 1" class="page-item">
                <a class="page-link" href="#" @click.prevent="changePage(1)">1</a>
              </li>
              <li v-if="startPage > 2" class="page-item disabled">
                <span class="page-link">...</span>
              </li>
              
              <li
                v-for="i in pageRange"
                :key="i"
                class="page-item"
                :class="{ active: i === pagination.current }"
              >
                <a class="page-link" href="#" @click.prevent="changePage(i)">{{ i }}</a>
              </li>
              
              <li v-if="endPage < pagination.totalPages - 1" class="page-item disabled">
                <span class="page-link">...</span>
              </li>
              <li v-if="endPage < pagination.totalPages" class="page-item">
                <a class="page-link" href="#" @click.prevent="changePage(pagination.totalPages)">{{ pagination.totalPages }}</a>
              </li>
              
              <li class="page-item" :class="{ disabled: pagination.current === pagination.totalPages }">
                <a class="page-link" href="#" @click.prevent="changePage(pagination.current + 1)">下一页</a>
              </li>
            </ul>
          </nav>
        </div>

        <!-- 侧边栏 -->
        <div class="col-md-4 ps-4">
          <!-- 热门阅读 -->
          <h5 class="font-weight-bold spanborder">
            <span>热门阅读</span>
          </h5>
          <ol class="list-unstyled" v-if="popularArticles.length > 0">
            <li
              v-for="(article, index) in popularArticles"
              :key="article.id"
              class="pb-3 pt-3 border-bottom"
              data-aos="fade-left"
              :data-aos-delay="index * 100"
            >
              <div class="d-flex align-items-center">
                <div style="width: 100%;">
                  <h6 class="font-weight-bold mb-0" style="margin-bottom: 2px;">
                    <router-link :to="`/article/${article.slug}`" class="text-dark">
                      {{ article.title }}
                    </router-link>
                  </h6>
                  <small class="text-muted d-flex align-items-center w-100 pt-2" style="justify-content: space-between;">
                    <span class="d-flex align-items-center">
                      <img
                        v-if="article.avatarUrl"
                        :src="article.avatarUrl"
                        alt="作者头像"
                        style="width: 20px; height: 20px; border-radius: 50%; margin-right: 5px; object-fit: cover;"
                      />
                      <small class="text-muted" style="font-size: 13px;">{{ article.userName || '佚名' }}</small>
                    </span>
                    <span style="display: inline-flex; align-items: center;" class="ms-auto">
                      <i class="bi bi-bar-chart me-1"></i>
                      <small>{{ article.viewCount || 0 }}</small>
                      <i class="bi bi-suit-heart ms-2 me-1"></i>
                      <small>{{ article.likes || 0 }}</small>
                    </span>
                  </small>
                </div>
              </div>
            </li>
          </ol>

          <!-- 热门标签 -->
          <h5 class="font-weight-bold spanborder">
            <span>热门标签</span>
          </h5>
          <div
            v-if="tags.length > 0"
            id="tagBubbleContainer"
            class="tag-cloud-container"
            style="position: relative; height: 400px; margin-bottom: 20px;"
          >
            <router-link
              v-for="tag in tags"
              :key="tag.id"
              :to="`/tags#${tag.name}`"
              class="tag-bubble"
              :style="getTagStyle(tag)"
              @click="navigateToTag(tag.name)"
            >
              <span style="display: block; line-height: 1.3; text-align: center; word-break: break-all; max-width: 100%;">
                {{ tag.name }} ({{ tag.articleCount }})
              </span>
            </router-link>
          </div>
          
          <div v-else-if="!loading">
            <p>暂无分类标签。</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleAPI, tagAPI } from '@/api'

const router = useRouter()

// 响应式数据
const loading = ref(true)
const articles = ref([])
const featuredArticles = ref([])
const popularArticles = ref([])
const tags = ref([])
const pagination = ref({
  current: 1,
  size: 8,
  totalPages: 1,
  total: 0
})

// 计算属性
const startPage = computed(() => Math.max(1, pagination.value.current - 1))
const endPage = computed(() => Math.min(pagination.value.totalPages, pagination.value.current + 1))
const pageRange = computed(() => {
  const range = []
  for (let i = startPage.value; i <= endPage.value; i++) {
    range.push(i)
  }
  return range
})

// 方法
const loadHomeData = async (page = 1) => {
  try {
    loading.value = true
    
    // 使用统一的首页API获取数据
    const homeRes = await articleAPI.getHomeArticles(page, pagination.value.size)
    
    // 处理首页数据
    if (homeRes.articles && homeRes.articles.records) {
      articles.value = homeRes.articles.records
      pagination.value = {
        current: homeRes.articles.current,
        size: homeRes.articles.size,
        totalPages: homeRes.articles.pages,
        total: homeRes.articles.total
      }
    }
    
    // 处理其他数据
    popularArticles.value = homeRes.popularArticles || []
    featuredArticles.value = homeRes.featuredArticles || []
    tags.value = homeRes.tags || []
    
    // 初始化标签云
    initTagCloud()
    
  } catch (error) {
    console.error('加载首页数据失败:', error)
  } finally {
    loading.value = false
  }
}

const changePage = (page) => {
  if (page >= 1 && page <= pagination.value.totalPages) {
    loadHomeData(page)
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

const navigateToTag = (tagName) => {
  router.push({ name: 'Tags', hash: `#${encodeURIComponent(tagName)}` })
}

const getTagStyle = (tag) => {
  // 根据文章数量计算标签大小和位置
  const count = tag.articleCount || 1
  const maxCount = Math.max(...tags.value.map(t => t.articleCount || 1))
  const minSize = 60
  const maxSize = 120
  const size = minSize + (maxSize - minSize) * (count / maxCount)
  
  // 随机位置
  const top = Math.random() * 60 + 5 // 5% - 65%
  const left = Math.random() * 80 + 10 // 10% - 90%
  
  // 随机颜色
  const colors = [
    'linear-gradient(135deg, #f6d55c 0%, #e8ca0f 100%)',
    'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
    'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
    'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
    'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)',
  ]
  const randomColor = colors[Math.floor(Math.random() * colors.length)]
  
  return {
    position: 'absolute',
    top: `${top}%`,
    left: `${left}%`,
    width: `${size}px`,
    height: `${size}px`,
    background: randomColor,
    borderRadius: '50%',
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    textAlign: 'center',
    textDecoration: 'none',
    padding: '5px 10px',
    boxSizing: 'border-box',
    overflow: 'hidden',
    transition: 'transform 0.3s ease, box-shadow 0.3s ease',
    fontSize: `${Math.max(12, size * 0.15)}px`,
    fontWeight: '500',
    color: 'white'
  }
}

const initTagCloud = () => {
  // 标签云初始化逻辑
  // 可以在这里添加更复杂的标签云动画
}

// 生命周期
onMounted(() => {
  loadHomeData()
})
</script>

<style scoped>
.main-content-wrapper {
  padding-top: 20px;
}

.article-link:hover .card {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.author-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  margin-right: 8px;
  object-fit: cover;
  background: linear-gradient(135deg, #f6d55c 0%, #e8ca0f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 12px;
}

.tag-bubble:hover {
  transform: scale(1.1);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
  color: white;
  text-decoration: none;
}

.carousel {
  border-radius: 1rem;
  overflow: hidden;
}

.spanborder {
  border-bottom: 1px solid #dee2e6;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
}

.spanborder span {
  background: white;
  padding-right: 1rem;
  font-size: 1.1rem;
  font-weight: 600;
  color: #495057;
}

@media (max-width: 768px) {
  .container-fluid {
    padding-left: 15px;
    padding-right: 15px;
  }
  
  .carousel {
    height: 200px !important;
  }
  
  .carousel .carousel-item img {
    height: 200px !important;
  }
  
  .col-md-4 {
    margin-top: 2rem;
    padding-left: 0 !important;
  }
  
  .tag-cloud-container {
    height: 300px !important;
  }
  
  .article-meta {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 8px;
  }
}
</style>
