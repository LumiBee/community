<template>
  <div class="home-page">
    <!-- Hero Section with Modern Carousel -->
    <section class="hero-section">
      <div class="container-fluid" style="max-width: 1400px; width: 100%;">
        <!-- 现代化轮播图 -->
        <div class="modern-carousel-container">
          <div
            id="modernCarousel"
            class="modern-carousel"
            data-bs-ride="carousel"
          >
            <!-- 轮播内容 -->
            <div class="modern-carousel-inner">
              <!-- 特色文章 -->
              <div 
                v-for="(article, index) in featuredArticles" 
                :key="article.articleId"
                :class="['modern-carousel-item', { active: index === 0 }]"
                @click="$router.push(`/article/${article.slug}`)"
              >
                <div class="carousel-image-wrapper">
                  <img 
                    :src="article.backgroundUrl || '/img/demo/1.jpg'" 
                    :alt="article.title"
                    class="carousel-image"
                  />
                  <div class="carousel-overlay"></div>
                </div>
                
                <div class="modern-carousel-content">
                  <div class="carousel-meta">
                    <div class="carousel-author">
                      <img 
                        :src="getAuthorAvatarUrl(article.avatarUrl)" 
                        :alt="article.userName || '匿名'"
                        class="carousel-author-avatar"
                      />
                      <span>{{ article.userName || '匿名' }}</span>
                    </div>
                  </div>
                  
                  <h2 class="carousel-title">{{ article.title }}</h2>
                  <p class="carousel-excerpt">{{ article.excerpt }}</p>
                  
                  <div class="carousel-actions">
                    <button class="carousel-read-btn">阅读文章 <i class="fas fa-arrow-right"></i></button>
                  </div>
                </div>
              </div>
              
              <!-- 默认轮播图 -->
              <div v-if="!featuredArticles.length" class="modern-carousel-item active">
                <div class="carousel-image-wrapper">
                  <img src="/img/demo/1.jpg" alt="欢迎来到Lumi Hive" class="carousel-image" />
                  <div class="carousel-overlay"></div>
                </div>
                
                <div class="modern-carousel-content">
                  <h2 class="carousel-title">欢迎来到Lumi Hive</h2>
                  <p class="carousel-excerpt">您的知识分享社区</p>
                </div>
              </div>
              
              <div v-if="!featuredArticles.length" class="modern-carousel-item">
                <div class="carousel-image-wrapper">
                  <img src="/img/demo/2.jpg" alt="分享您的知识" class="carousel-image" />
                  <div class="carousel-overlay"></div>
                </div>
                
                <div class="modern-carousel-content">
                  <h2 class="carousel-title">分享您的知识</h2>
                  <p class="carousel-excerpt">创建文章，分享您的专业知识和见解</p>
                </div>
              </div>
              
              <div v-if="!featuredArticles.length" class="modern-carousel-item">
                <div class="carousel-image-wrapper">
                  <img src="/img/demo/3.jpg" alt="探索精彩内容" class="carousel-image" />
                  <div class="carousel-overlay"></div>
                </div>
                
                <div class="modern-carousel-content">
                  <h2 class="carousel-title">探索精彩内容</h2>
                  <p class="carousel-excerpt">浏览各种主题的优质文章</p>
                </div>
              </div>
            </div>
            
            <!-- 轮播指示器 -->
            <div class="modern-carousel-indicators">
              <button 
                v-for="(article, index) in featuredArticles.length ? featuredArticles : [1, 2, 3]" 
                :key="index"
                type="button" 
                :class="{ active: index === 0 }"
                @click="changeSlide(index)"
              ></button>
            </div>
            
            <!-- 轮播控制按钮 -->
            <button class="modern-carousel-control prev" @click="prevSlide">
              <i class="fas fa-chevron-left"></i>
            </button>
            <button class="modern-carousel-control next" @click="nextSlide">
              <i class="fas fa-chevron-right"></i>
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Main Content Section -->
    <section class="content-section">
      <div class="container-fluid" style="max-width: 1400px;">
      <div class="row justify-content-between">
        <!-- 主要内容区域 -->
        <div class="col-md-8">
          <h5 class="font-weight-bold spanborder">
            <span>所有文章</span>
          </h5>
          
          <!-- 文章列表 -->
          <ol class="list-unstyled compact-article-list" v-if="articles.length > 0">
            <li
              v-for="(article, index) in articles"
              :key="article.articleId"
              class="compact-article-item"
              data-aos="fade-up"
              :data-aos-delay="index * 50"
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
          
          <!-- 无文章提示 -->
          <div v-else-if="!loading" class="empty-state">
            <div class="empty-state-icon">📝</div>
            <h3 class="empty-state-title">暂时还没有文章哦</h3>
            <p class="empty-state-text">敬请期待精彩内容的到来</p>
          </div>
          
          <!-- 分页 -->
          <nav v-if="pagination.totalPages > 1" aria-label="Page navigation" class="mt-5">
            <ul class="pagination justify-content-center modern-pagination">
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
          <ol class="list-unstyled compact-popular-list" v-if="popularArticles.length > 0">
            <li
              v-for="(article, index) in popularArticles"
              :key="article.articleId"
              class="compact-popular-item"
              data-aos="fade-left"
              :data-aos-delay="index * 100"
            >
              <div class="compact-article-content">
                <h6 class="compact-article-title">
                  <router-link :to="`/article/${article.slug}`" class="text-dark">
                    {{ article.title }}
                  </router-link>
                </h6>
                <div class="compact-article-meta">
                  <div class="compact-author-info">
                    <img
                      v-if="article.avatarUrl"
                      :src="getAuthorAvatarUrl(article.avatarUrl)"
                      alt="作者头像"
                      class="compact-author-avatar"
                    />
                    <span class="compact-author-name">{{ article.userName || '佚名' }}</span>
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

          <!-- 热门标签 -->
          <h5 class="font-weight-bold spanborder">
            <span>热门标签</span>
          </h5>
          <div
            v-if="tags.length > 0"
            id="tagBubbleContainer"
            class="tag-cloud-container"
            style="position: relative; height: 800px; margin-bottom: 20px; overflow: hidden;"
          >
            <router-link
              v-for="tag in tags"
              :key="tag.id"
              :to="`/tags#${tag.name}`"
              class="tag-bubble"
              :data-count="tag.articleCount || 0"
              @click="navigateToTag(tag.name)"
            >
              <span class="tag-content">
                {{ tag.name }}
                <small class="tag-count">({{ tag.articleCount }})</small>
              </span>
            </router-link>
          </div>
          
          <div v-else-if="!loading">
            <p>暂无分类标签。</p>
          </div>
        </div>
      </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { articleAPI, tagAPI } from '@/api'
import { getAuthorAvatarUrl } from '@/utils/avatar-helper'

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
    
    
    if (homeRes.articles && homeRes.articles.records) {
      articles.value = homeRes.articles.records
      pagination.value = {
        current: homeRes.articles.current,
        size: homeRes.articles.size,
        totalPages: homeRes.articles.pages,
        total: homeRes.articles.total
      }
    } else {
      articles.value = []
    }
    
    // 处理其他数据
    popularArticles.value = homeRes.popularArticles || []
    featuredArticles.value = homeRes.featuredArticles || []
    tags.value = homeRes.tags || []
    
    // 初始化智能标签云
    initSmartTagCloud()
    
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

// 智能泡泡标签云实现
const initSmartTagCloud = () => {
  // 等待DOM渲染完成
  setTimeout(() => {
    const container = document.getElementById('tagBubbleContainer')
    if (!container) return
    
    const bubbles = Array.from(container.getElementsByClassName('tag-bubble'))
    if (bubbles.length === 0) return

    const containerWidth = container.offsetWidth
    const containerHeight = container.offsetHeight

    // 丰富的颜色调色板 - 使用更鲜艳的颜色
    const colorPalette = [
      '#FF6B6B', '#4ECDC4', '#45B7D1', '#FED766', '#2AB7CA',
      '#F0B67F', '#FE4A49', '#547980', '#8A9B0F', '#C3D89F',
      '#FF9E9D', '#3D405B', '#81B29A', '#F2CC8F', '#E07A5F',
      '#D81E5B', '#F4A261', '#2A9D8F', '#E9C46A', '#264653',
      '#6C5CE7', '#A29BFE', '#FD79A8', '#FDCB6E', '#E84393',
      '#FF5733', '#33A8FF', '#33FF57', '#FF33A8', '#A833FF',
      '#FF8333', '#33FFC5', '#FF33C5', '#33FF83', '#C533FF'
    ]

    // 根据文章数计算泡泡大小的参数
    const baseSize = 70 // 最小泡泡的直径 (px)
    const maxSize = 140 // 最大泡泡的直径 (px)
    const countFactor = 1.5 // 每篇文章数增加多少像素直径

    const baseFontSize = 14 // 基础字体大小 (px)
    const maxFontSize = 20 // 最大字体大小 (px)
    const countFactorFont = 0.3 // 每篇文章数增加多少像素字体大小

    // 计算最大文章数
    let maxArticleCount = 0
    bubbles.forEach(bubble => {
      const count = parseInt(bubble.getAttribute('data-count') || '0')
      if (count > maxArticleCount) {
        maxArticleCount = count
      }
    })

    // 用于避免重叠的数组，记录已放置泡泡的区域
    let placedBubbles = []

    // 首先根据文章数量排序泡泡，确保较大的泡泡先放置
    // 同时限制最大显示数量，避免过多标签导致重叠
    const maxBubbles = 20; // 最多显示20个标签
    const sortedBubbles = [...bubbles].sort((a, b) => {
      const countA = parseInt(a.getAttribute('data-count') || '0')
      const countB = parseInt(b.getAttribute('data-count') || '0')
      return countB - countA // 降序排列
    }).slice(0, maxBubbles) // 只取前maxBubbles个

    sortedBubbles.forEach((bubble, index) => {
      const articleCount = parseInt(bubble.getAttribute('data-count') || '0')

      // 1. 设置颜色 - 更鲜艳的颜色
      const colorIndex = (index + Math.floor(Math.random() * 5)) % colorPalette.length
      bubble.style.background = colorPalette[colorIndex]

      // 2. 设置大小 (基于文章数)
      let diameter = baseSize + articleCount * countFactor
      if (maxArticleCount > 0 && articleCount === maxArticleCount) {
        diameter = Math.min(maxSize, diameter + 20)
      }
      diameter = Math.max(baseSize, Math.min(maxSize, diameter))

      bubble.style.width = diameter + 'px'
      bubble.style.height = diameter + 'px'

      // 字体大小也可以根据泡泡大小动态调整
      let fontSize = baseFontSize + (diameter - baseSize) * countFactorFont
      fontSize = Math.max(baseFontSize, Math.min(maxFontSize, fontSize))
      bubble.style.fontSize = fontSize + 'px'

      const paddingValue = Math.max(2, diameter * 0.05)
      bubble.style.padding = paddingValue + 'px'

      // 3. 设置位置 (使用网格布局减少重叠)
      let bestPos = null
      let minOverlap = Infinity
      
      // 尝试更多位置，选择重叠最小的
      for (let i = 0; i < 300; i++) {
        let x, y;
        
        if (i < 100) {
          // 策略1: 使用网格布局
          const gridSize = Math.ceil(Math.sqrt(bubbles.length));
          const cellWidth = containerWidth / gridSize;
          const cellHeight = containerHeight / gridSize;
          
          const gridX = i % gridSize;
          const gridY = Math.floor(i / gridSize) % gridSize;
          
          // 在网格单元内随机位置
          x = gridX * cellWidth + Math.random() * (cellWidth - diameter);
          y = gridY * cellHeight + Math.random() * (cellHeight - diameter);
        } else if (i < 200) {
          // 策略2: 圆形分布
          const angle = Math.random() * Math.PI * 2;
          const maxRadius = Math.min(containerWidth, containerHeight) * 0.4;
          // 使用平方根分布使点更均匀分布
          const distance = Math.sqrt(Math.random()) * maxRadius;
          const centerX = containerWidth / 2;
          const centerY = containerHeight / 2;
          
          x = centerX + Math.cos(angle) * distance - diameter / 2;
          y = centerY + Math.sin(angle) * distance - diameter / 2;
        } else {
          // 策略3: 完全随机，但避开中心区域
          const centerX = containerWidth / 2;
          const centerY = containerHeight / 2;
          const centerRadius = Math.min(containerWidth, containerHeight) * 0.2;
          
          do {
            x = Math.random() * (containerWidth - diameter);
            y = Math.random() * (containerHeight - diameter);
            // 计算到中心的距离
            const dx = x + diameter/2 - centerX;
            const dy = y + diameter/2 - centerY;
            const distanceToCenter = Math.sqrt(dx*dx + dy*dy);
            
            // 如果距离中心够远，接受这个位置
            if (distanceToCenter > centerRadius) {
              break;
            }
          } while (i % 10 !== 0); // 每10次尝试就接受一次，避免无限循环
        }
        
        // 确保不超出容器边界
        const adjustedX = Math.max(0, Math.min(containerWidth - diameter, x));
        const adjustedY = Math.max(0, Math.min(containerHeight - diameter, y));
        
        const pos = {
          x: adjustedX,
          y: adjustedY,
          radius: diameter / 2 + 5, // 增加一点缓冲区，减少视觉上的重叠
          right: adjustedX + diameter,
          bottom: adjustedY + diameter
        }
        
        const overlapAmount = calculateOverlap(pos, placedBubbles)
        if (overlapAmount < minOverlap) {
          minOverlap = overlapAmount
          bestPos = pos
          
          // 如果找到完全不重叠的位置，立即使用
          if (overlapAmount === 0) break
        }
      }
      
      // 使用找到的最佳位置
      if (bestPos) {
        bubble.style.left = bestPos.x + 'px'
        bubble.style.top = bestPos.y + 'px'
        placedBubbles.push(bestPos)
      } else {
        // 如果实在找不到好位置，随机放置
        const x = Math.random() * (containerWidth - diameter)
        const y = Math.random() * (containerHeight - diameter)
        bubble.style.left = x + 'px'
        bubble.style.top = y + 'px'
      }

      // 不添加旋转，保持泡泡正常显示
      bubble.style.transform = ''
    })

    // 为泡泡添加轻微的浮动动效
    bubbles.forEach((bubble, index) => {
      // 根据标签的大小调整浮动幅度
      const diameter = parseFloat(bubble.style.width);
      const floatAmount = Math.max(3, Math.min(8, diameter / 15)); // 浮动幅度在3-8px之间
      
      // 随机浮动方向和时间
      const direction = index % 2 === 0 ? 1 : -1; // 交替上下浮动
      const duration = 3000 + Math.random() * 2000; // 3-5秒
      const delay = Math.random() * 2000; // 随机延迟，使动画错开
      
      // 应用动画
      bubble.animate([
        { transform: 'translateY(0px)' },
        { transform: `translateY(${direction * floatAmount}px)` },
        { transform: 'translateY(0px)' }
      ], {
        duration: duration,
        iterations: Infinity,
        direction: 'alternate',
        easing: 'ease-in-out',
        delay: delay
      });
    })
    
    // 计算重叠程度 - 改进版本，更严格地避免重叠
    function calculateOverlap(newBubble, existingBubbles) {
      let totalOverlap = 0
      for (let existing of existingBubbles) {
        // 计算两个圆心之间的距离
        const dx = (newBubble.x + newBubble.radius) - (existing.x + existing.radius)
        const dy = (newBubble.y + newBubble.radius) - (existing.y + existing.radius)
        const distance = Math.sqrt(dx * dx + dy * dy)
        
        // 两个圆的半径之和
        const minDistance = newBubble.radius + existing.radius
        
        // 如果距离小于两个圆半径之和，则有重叠
        if (distance < minDistance) {
          // 计算重叠程度，并给较大的重叠更高的惩罚
          const overlap = minDistance - distance
          totalOverlap += overlap * overlap // 平方惩罚，使算法更倾向于避免大的重叠
        }
      }
      return totalOverlap
    }

    // 颜色亮化函数
    function lightenColor(color, percent) {
      const num = parseInt(color.replace("#", ""), 16)
      const amt = Math.round(2.55 * percent)
      const R = (num >> 16) + amt
      const G = (num >> 8 & 0x00FF) + amt
      const B = (num & 0x0000FF) + amt
      return "#" + (0x1000000 + (R < 255 ? R < 1 ? 0 : R : 255) * 0x10000 +
        (G < 255 ? G < 1 ? 0 : G : 255) * 0x100 +
        (B < 255 ? B < 1 ? 0 : B : 255)).toString(16).slice(1)
    }
  }, 100)
}

// 轮播图相关方法
const currentSlide = ref(0);
const totalSlides = computed(() => featuredArticles.value.length || 3);

// 切换到指定幻灯片
const changeSlide = (index) => {
  // 移除所有幻灯片的激活状态
  const carouselItems = document.querySelectorAll('.modern-carousel-item');
  carouselItems.forEach(item => item.classList.remove('active'));
  
  // 移除所有指示器的激活状态
  const indicators = document.querySelectorAll('.modern-carousel-indicators button');
  indicators.forEach(indicator => indicator.classList.remove('active'));
  
  // 激活当前幻灯片和指示器
  // 添加安全检查，确保元素存在
  if (index < carouselItems.length && carouselItems[index]) {
    carouselItems[index].classList.add('active');
  }
  
  if (index < indicators.length && indicators[index]) {
    indicators[index].classList.add('active');
  }
  
  // 更新当前幻灯片索引
  currentSlide.value = index;
};

// 下一张幻灯片
const nextSlide = () => {
  const next = (currentSlide.value + 1) % totalSlides.value;
  changeSlide(next);
};

// 上一张幻灯片
const prevSlide = () => {
  const prev = (currentSlide.value - 1 + totalSlides.value) % totalSlides.value;
  changeSlide(prev);
};

// 自动轮播
let autoplayInterval = null;
const startAutoplay = () => {
  autoplayInterval = setInterval(() => {
    nextSlide();
  }, 8000); // 8秒切换一次
};

const stopAutoplay = () => {
  if (autoplayInterval) {
    clearInterval(autoplayInterval);
  }
};

// 生命周期
onMounted(() => {
  loadHomeData();
  
  // 初始化轮播图
  nextTick(() => {
    // 等待DOM渲染完成后初始化轮播图
    setTimeout(() => {
      const firstSlide = document.querySelector('.modern-carousel-item');
      const firstIndicator = document.querySelector('.modern-carousel-indicators button');
      
      if (firstSlide) firstSlide.classList.add('active');
      if (firstIndicator) firstIndicator.classList.add('active');
      
      // 启动自动轮播
      startAutoplay();
      
      // 鼠标悬停时暂停自动轮播
      const carousel = document.querySelector('.modern-carousel');
      if (carousel) {
        carousel.addEventListener('mouseenter', stopAutoplay);
        carousel.addEventListener('mouseleave', startAutoplay);
      }
    }, 500);
  });
});

// 组件卸载时清除定时器
onBeforeUnmount(() => {
  stopAutoplay();
  
  // 移除事件监听器
  const carousel = document.querySelector('.modern-carousel');
  if (carousel) {
    carousel.removeEventListener('mouseenter', stopAutoplay);
    carousel.removeEventListener('mouseleave', startAutoplay);
  }
})
</script>

<style scoped>
/* ===== 整体布局 ===== */
.home-page {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  min-height: 100vh;
}

.hero-section {
  padding: 2rem 0;
  background: white;
}

.content-section {
  padding: 3rem 0;
  background: white;
  margin-top: -1rem;
  border-radius: 2rem 2rem 0 0;
  box-shadow: 0 -8px 32px rgba(0, 0, 0, 0.05);
}

/* ===== 文章卡片 ===== */
.article-link {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.article-link:hover {
  transform: translateY(-8px);
}

.article-card {
  background: white;
  border-radius: 1.2rem;
  border: 1px solid rgba(255, 193, 7, 0.08);
  box-shadow: 0 3px 15px rgba(255, 193, 7, 0.05), 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;
  margin-bottom: 1rem;
}

.article-card::before {
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

.article-link:hover .article-card {
  box-shadow: 0 12px 40px rgba(255, 193, 7, 0.12), 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: rgba(255, 193, 7, 0.2);
}

.article-link:hover .article-card::before {
  transform: scaleX(1);
}

.article-title {
  color: #2c3e50;
  font-weight: 700;
  line-height: 1.3;
  margin-bottom: 0.3rem;
  transition: color 0.3s ease;
}

.article-link:hover .article-title {
  color: #ffc107;
}

.article-excerpt {
  color: #64748b;
  line-height: 1.5;
  margin-bottom: 0.3rem;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.1rem 0;
}

.author-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  background: linear-gradient(135deg, #ffc107 0%, #ffda58 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 11px;
  box-shadow: 0 2px 6px rgba(255, 193, 7, 0.25);
  transition: transform 0.3s ease;
}

.author-avatar-link {
  text-decoration: none;
  transition: transform 0.2s ease;
  display: inline-block;
}

.author-avatar-link:hover {
  transform: scale(1.05);
}

.article-link:hover .author-avatar {
  transform: scale(1.1);
}

.article-meta {
  padding-top: 0.2rem;
  border-top: 1px solid #f1f5f9;
  margin-top: 0.2rem;
  padding-bottom: 0.1rem;
}

.article-stats {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #64748b;
  font-size: 0.875rem;
  transition: color 0.3s ease;
}

.stat-item i {
  opacity: 0.8;
  color: #ffc107;
  font-size: 0.875rem;
  transition: all 0.3s ease;
}

.article-link:hover .stat-item i {
  opacity: 1;
  transform: scale(1.1);
}

.article-link:hover .stat-item {
  color: #ffc107;
}

/* ===== 智能泡泡标签样式 ===== */
.tag-bubble {
  position: absolute;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  text-decoration: none;
  box-sizing: border-box;
  overflow: hidden;
  font-weight: 600;
  color: white;
  cursor: pointer;
  z-index: 1;
  
  /* 简洁的泡泡效果 */
  border: none;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  
  transition: all 0.3s ease;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  
  /* 防止文字选中 */
  user-select: none;
  -webkit-user-select: none;
  
  /* 为奇数和偶数标签添加不同的浮动动画 */
  will-change: transform;
}

.tag-bubble:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
  color: white;
  text-decoration: none;
  z-index: 100;
  filter: brightness(1.1);
}

/* 点击效果 */
.tag-bubble:active {
  transform: scale(1.05);
  transition: all 0.1s ease;
}

/* 标签内容样式 */
.tag-content {
  display: block;
  line-height: 1.3;
  text-align: center;
  word-break: break-all;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 0 8px;
}

.tag-count {
  display: block;
  font-size: 0.85em;
  opacity: 0.9;
  margin-top: 4px;
  font-weight: normal;
}

/* 轻微的浮动动画效果 */
@keyframes float-up {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

@keyframes float-down {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(5px); }
}

/* ===== 现代化轮播图样式 ===== */
.modern-carousel-container {
  margin-bottom: 2rem;
  position: relative;
  overflow: hidden;
  border-radius: 20px;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.1);
}

.modern-carousel {
  position: relative;
  width: 100%;
  height: 450px;
  overflow: hidden;
}

.modern-carousel-inner {
  position: relative;
  width: 100%;
  height: 100%;
}

.modern-carousel .modern-carousel-item {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  opacity: 0;
  visibility: hidden;
  transition: opacity 1.5s ease, visibility 1.5s ease;
  cursor: pointer;
}

.modern-carousel .modern-carousel-item.active {
  opacity: 1;
  visibility: visible;
  z-index: 2;
}

.carousel-image-wrapper {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 8s ease;
}

.modern-carousel .modern-carousel-item.active .carousel-image {
  transform: scale(1.05);
}

.carousel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    to right,
    rgba(0, 0, 0, 0.8) 0%,
    rgba(0, 0, 0, 0.6) 30%,
    rgba(0, 0, 0, 0.4) 60%,
    rgba(0, 0, 0, 0.2) 100%
  );
  z-index: 2;
}

.modern-carousel-content {
  position: relative;
  z-index: 3;
  width: 100%;
  max-width: 800px;
  padding: 3rem 6rem 3rem 4rem;
  margin-left: 50px;
  color: white;
}

.carousel-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.carousel-author {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.95rem;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
}

.carousel-author-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.carousel-stats {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.8);
}

.carousel-stats i {
  margin-right: 0.35rem;
  color: #ffda58;
}

.carousel-title {
  font-size: 2.75rem;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 1.25rem;
  color: white;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  letter-spacing: -0.5px;
  max-width: 90%;
}

.carousel-excerpt {
  font-size: 1.15rem;
  line-height: 1.6;
  margin-bottom: 2rem;
  color: rgba(255, 255, 255, 0.85);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  max-width: 80%;
}

.carousel-actions {
  margin-top: 1rem;
}

.carousel-read-btn {
  background: #ffda58;
  color: #2d3748;
  border: none;
  border-radius: 30px;
  padding: 0.85rem 2rem;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 218, 88, 0.3);
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
}

.carousel-read-btn:hover {
  background: #ffd333;
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 218, 88, 0.4);
}

.carousel-read-btn i {
  font-size: 0.85rem;
  transition: transform 0.3s ease;
}

.carousel-read-btn:hover i {
  transform: translateX(4px);
}

/* 轮播图控制按钮 */
.modern-carousel-control {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  backdrop-filter: blur(5px);
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.modern-carousel-control.prev {
  left: 20px;
}

.modern-carousel-control.next {
  right: 20px;
}

.modern-carousel-control i {
  color: white;
  font-size: 1rem;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  transition: transform 0.3s ease;
}

.modern-carousel-control:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-50%) scale(1.1);
}

.modern-carousel-control.prev:hover i {
  transform: translateX(-2px);
}

.modern-carousel-control.next:hover i {
  transform: translateX(2px);
}

/* 轮播图指示器 */
.modern-carousel-indicators {
  position: absolute;
  bottom: 30px;
  right: 50px;
  z-index: 10;
  display: flex;
  gap: 0.75rem;
}

.modern-carousel-indicators button {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 0;
}

.modern-carousel-indicators button.active {
  background: #ffda58;
  transform: scale(1.2);
  box-shadow: 0 0 10px rgba(255, 218, 88, 0.5);
}

/* 响应式调整 */
@media (max-width: 992px) {
  .modern-carousel {
    height: 400px;
  }
  
  .modern-carousel-content {
    padding: 2rem 3rem;
  }
  
  .carousel-title {
    font-size: 2.25rem;
  }
  
  .carousel-excerpt {
    font-size: 1.05rem;
  }
}

@media (max-width: 768px) {
  .modern-carousel {
    height: 350px;
  }
  
  .modern-carousel-content {
    padding: 1.5rem 2rem;
  }
  
  .carousel-title {
    font-size: 1.75rem;
    margin-bottom: 1rem;
  }
  
  .carousel-excerpt {
    font-size: 1rem;
    margin-bottom: 1.5rem;
    max-width: 100%;
  }
  
  .carousel-meta {
    margin-bottom: 1rem;
  }
  
  .carousel-overlay {
    background: linear-gradient(
      to right,
      rgba(0, 0, 0, 0.85) 0%,
      rgba(0, 0, 0, 0.7) 50%,
      rgba(0, 0, 0, 0.5) 100%
    );
  }
  
  .modern-carousel-indicators {
    right: auto;
    left: 50%;
    transform: translateX(-50%);
  }
}

@media (max-width: 576px) {
  .modern-carousel {
    height: 300px;
  }
  
  .modern-carousel-content {
    padding: 1rem 1.5rem;
  }
  
  .carousel-title {
    font-size: 1.5rem;
  }
  
  .carousel-excerpt {
    font-size: 0.95rem;
    margin-bottom: 1rem;
  }
  
  .carousel-read-btn {
    padding: 0.75rem 1.5rem;
    font-size: 0.9rem;
  }
  
  .carousel-author-avatar {
    width: 30px;
    height: 30px;
  }
  
  .modern-carousel-control {
    width: 40px;
    height: 40px;
  }
  
  .modern-carousel-control.prev {
    left: 10px;
  }
  
  .modern-carousel-control.next {
    right: 10px;
  }
}

.carousel-control-prev,
.carousel-control-next {
  width: 50px;
  height: 50px;
  background: transparent;
  border: none;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0.8;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.carousel-control-prev {
  left: 1.5rem;
}

.carousel-control-next {
  right: 1.5rem;
}

.carousel-control-prev:hover,
.carousel-control-next:hover {
  transform: translateY(-50%) scale(1.15);
  opacity: 1;
}

.carousel-control-prev:hover .carousel-control-prev-icon,
.carousel-control-next:hover .carousel-control-next-icon {
  filter: drop-shadow(0 2px 5px rgba(0, 0, 0, 0.7));
}

.carousel-control-prev-icon,
.carousel-control-next-icon {
  width: 30px;
  height: 30px;
  background-color: white;
  filter: drop-shadow(0 1px 3px rgba(0, 0, 0, 0.5));
  opacity: 0.9;
}

/* ===== 章节标题 ===== */
.spanborder {
  position: relative;
  margin-bottom: 2.5rem;
  padding-bottom: 1.5rem;
}

.spanborder::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, #dee2e6 0%, transparent 100%);
}

.spanborder span {
  display: inline-block;
  background: white;
  padding-right: 2rem;
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  position: relative;
}

.spanborder span::before {
  content: '';
  position: absolute;
  bottom: -1.5rem;
  left: 0;
  width: 3rem;
  height: 3px;
  background: linear-gradient(90deg, #ffc107 0%, #ffda58 100%);
  border-radius: 1.5px;
}

/* ===== 侧边栏美化 ===== */
.col-md-4 {
  padding-left: 2rem;
}

.col-md-4 .spanborder span::before {
  width: 2.5rem;
  background: linear-gradient(90deg, #ffda58 0%, #ffc107 100%);
}

/* 热门阅读列表 - 紧凑样式 */
.compact-popular-list {
  margin: 0;
  padding: 0;
}

.compact-popular-item {
  padding: 0.8rem 0;
  border-bottom: 1px solid #f1f5f9;
  transition: all 0.3s ease;
  margin-bottom: 0.3rem;
  position: relative;
}

.compact-popular-item:last-child {
  border-bottom: none;
}

.compact-popular-item:hover {
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.02) 0%, rgba(255, 218, 88, 0.02) 100%);
  transform: translateX(4px);
  border-color: rgba(255, 193, 7, 0.1);
  padding-left: 0.5rem;
  border-radius: 0.5rem;
}

.compact-article-content {
  width: 100%;
}

.compact-article-title {
  margin: 0 0 0.25rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.compact-article-title a {
  color: #2c3e50;
  text-decoration: none;
  transition: color 0.3s ease;
}

.compact-popular-item:hover .compact-article-title a {
  color: #ffc107;
}

.compact-article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-top: 8px;
}

.compact-author-info {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.compact-author-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(255, 193, 7, 0.2);
  margin-right: 4px;
}

.compact-author-name {
  font-size: 0.95rem;
  color: #64748b;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100px;
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
  gap: 0.2rem;
  font-size: 0.9rem;
  color: #64748b;
  margin-left: 8px;
}

.compact-stat-item i {
  color: #ffc107;
  font-size: 0.85rem;
  opacity: 0.8;
}

.compact-popular-item:hover .compact-stat-item i {
  opacity: 1;
  transform: scale(1.1);
}

.compact-popular-item:hover .compact-stat-item {
  color: #ffc107;
}

/* 所有文章区域紧凑布局 */
.compact-article-list {
  margin: 0;
  padding: 0;
}

.compact-article-item {
  padding: 1.5rem;
  border: 1px solid #eaedf1;
  border-radius: 12px;
  transition: all 0.3s ease;
  margin-bottom: 1.5rem; /* 增加底部间距，使卡片之间的空间更大 */
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

.compact-article-item .compact-article-title {
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

.compact-article-item .compact-article-title span {
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
  -webkit-line-clamp: 3; /* 从2行增加到3行 */
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 5.5rem; /* 确保有足够的高度显示3行内容 */
}

.compact-time {
  font-size: 0.9rem;
  color: #94a3b8;
  margin-left: 10px;
  background-color: transparent;
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
}

/* 所有文章区域的作者信息和统计信息样式 */
.compact-article-item .compact-article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f1f5f9;
}

.compact-article-item .compact-author-info {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.compact-article-item .compact-author-avatar {
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
  font-size: 14px;
  background: linear-gradient(135deg, #ffc107 0%, #ffda58 100%);
  box-shadow: 0 2px 6px rgba(255, 193, 7, 0.15);
  position: relative;
  z-index: 2;
}

.compact-article-item .compact-author-name {
  font-size: 1rem;
  color: #4b5563;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}

.compact-article-item .compact-stats {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-left: auto;
}

.compact-article-item .compact-stat-item {
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

.compact-article-item .compact-stat-item i {
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

/* 保留原有的通用样式作为备用 */
.list-unstyled li h6 a {
  color: #2c3e50;
  font-weight: 600;
  font-size: 0.9rem;
  line-height: 1.2;
  transition: color 0.3s ease;
}

.list-unstyled li:hover h6 a {
  color: #ffc107;
}

/* 智能标签云容器 */
.tag-cloud-container {
  background: #ffffff;
  border-radius: 1rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
  padding: 1.5rem;
  position: relative;
  overflow: hidden;
  min-height: 500px;
}

.tag-cloud-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #ffc107 0%, #ffda58 100%);
  border-radius: 1.5rem 1.5rem 0 0;
}

/* ===== 响应式设计 ===== */
@media (max-width: 768px) {
  .hero-section {
    padding: 1rem 0;
  }
  
  .content-section {
    padding: 2rem 0;
    margin-top: 0;
    border-radius: 1rem 1rem 0 0;
  }
  
  .container-fluid {
    padding-left: 1rem;
    padding-right: 1rem;
  }
  
  .carousel {
    height: 250px !important;
    border-radius: 1rem;
  }
  
  .carousel .carousel-item img {
    height: 250px !important;
  }
  
  .carousel-caption {
    padding: 1.5rem 1rem 1rem;
  }
  
  .carousel-caption h5 {
    font-size: 1.25rem;
  }
  
  .carousel-caption p {
    font-size: 0.95rem;
  }
  
  .col-md-4 {
    margin-top: 3rem;
    padding-left: 0 !important;
  }
  
  .tag-cloud-container {
    min-height: 400px !important;
    height: 400px !important;
    padding: 1rem;
  }
  
  .article-meta {
    flex-direction: row;
    justify-content: space-between !important;
    align-items: center !important;
    gap: 0;
  }
  
  .article-card {
    border-radius: 1rem;
  }
  
  .spanborder span {
    font-size: 1.25rem;
  }
  
  .tag-bubble {
    min-width: 45px !important;
    min-height: 45px !important;
    font-size: 10px !important; /* 在移动设备上统一字体大小 */
    padding: 4px 6px !important;
  }
  
  /* 移动端使用更紧凑的网格 */
  .tag-cloud-container {
    overflow-x: hidden;
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

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.article-card {
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
}

.list-unstyled li {
  animation: slideInLeft 0.5s ease-out;
  animation-fill-mode: both;
}

.tag-cloud-container {
  animation: fadeInUp 0.7s ease-out;
  animation-fill-mode: both;
  animation-delay: 0.2s;
}

/* 为不同位置的元素添加延迟 */
.article-card:nth-child(1) { animation-delay: 0.1s; }
.article-card:nth-child(2) { animation-delay: 0.2s; }
.article-card:nth-child(3) { animation-delay: 0.3s; }
.article-card:nth-child(4) { animation-delay: 0.4s; }

.list-unstyled li:nth-child(1) { animation-delay: 0.1s; }
.list-unstyled li:nth-child(2) { animation-delay: 0.2s; }
.list-unstyled li:nth-child(3) { animation-delay: 0.3s; }
.list-unstyled li:nth-child(4) { animation-delay: 0.4s; }
.list-unstyled li:nth-child(5) { animation-delay: 0.5s; }

/* ===== 分页样式 ===== */
.modern-pagination {
  margin: 3rem 0 1rem;
}

.modern-pagination .page-item {
  margin: 0 0.25rem;
}

.modern-pagination .page-link {
  border: 2px solid transparent;
  border-radius: 0.75rem;
  padding: 0.75rem 1rem;
  color: #64748b;
  background: white;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.modern-pagination .page-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, #ffc107 0%, #ffda58 100%);
  transition: left 0.3s ease;
  z-index: -1;
}

.modern-pagination .page-link:hover {
  color: white;
  border-color: transparent;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.2);
}

.modern-pagination .page-link:hover::before {
  left: 0;
}

.modern-pagination .page-item.active .page-link {
  background: linear-gradient(135deg, #ffc107 0%, #ffda58 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.3);
}

.modern-pagination .page-item.active .page-link::before {
  left: 0;
}

.modern-pagination .page-item.disabled .page-link {
  color: #cbd5e1;
  background: #f8f9fa;
  border-color: #e2e8f0;
  cursor: not-allowed;
  box-shadow: none;
}

.modern-pagination .page-item.disabled .page-link:hover {
  color: #cbd5e1;
  background: #f8f9fa;
  transform: none;
  box-shadow: none;
}

.modern-pagination .page-item.disabled .page-link::before {
  display: none;
}

/* ===== 空状态样式 ===== */
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
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
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
  margin-bottom: 0;
  line-height: 1.6;
}
</style>
