<template>
  <div class="article-page">
    <!-- 回到顶部按钮 -->
    <div class="back-to-top" @click="scrollToTop" v-show="showBackToTop">
      <i class="fas fa-arrow-up"></i>
    </div>
    <div class="article-hero" v-if="article">
      <div class="hero-overlay"></div>
      <div class="container-fluid" style="max-width: 1400px; position: relative; z-index: 2;">
        <div class="article-hero-content">
          <h1 class="article-hero-title">{{ article.title }}</h1>
          <div class="article-hero-meta">
            <div class="d-flex align-items-center">
              <img :src="article.avatarUrl || '/img/default01.jpg'" alt="作者头像" class="hero-avatar me-2">
              <span class="hero-author">{{ article.userName || '匿名' }}</span>
              <span class="hero-date mx-2">·</span>
              <span class="hero-date">{{ formatDate(article.gmtCreate) }}</span>
            </div>
            <div class="article-hero-stats">
              <span class="hero-stat me-3"><i class="fas fa-eye me-1"></i>{{ article.viewCount || 0 }}</span>
              <span class="hero-stat"><i class="fas fa-heart me-1"></i>{{ article.likes || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="container-fluid" style="max-width: 1400px;">
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-warning" role="status">
          <span class="visually-hidden">加载中...</span>
        </div>
      </div>
      
      <div v-else-if="article" class="row">
        <!-- 文章内容 -->
        <div class="col-lg-8 position-relative">
          <!-- 左侧快捷按钮栏 -->
                      <div class="article-quick-actions">
              <div class="quick-action-btn" @click="toggleLike" :class="{ 'active': article.liked }" :title="article.liked ? '取消点赞' : '点赞'">
                <i :class="[article.liked ? 'fas fa-heart' : 'far fa-heart']"></i>
                <span class="action-count">{{ article.likes || 0 }}</span>
              </div>
            <div class="quick-action-btn" @click="toggleFavorite" :class="{ 'active': article.isFavorited }" :title="article.isFavorited ? '取消收藏' : '收藏'">
              <i :class="[article.isFavorited ? 'fas fa-bookmark' : 'far fa-bookmark']"></i>
            </div>
            <div class="quick-action-btn" @click="shareArticle" title="分享">
              <i class="fas fa-share-alt"></i>
            </div>
            <div class="quick-action-btn" @click="scrollToComments" title="查看评论">
              <i class="fas fa-comment-alt"></i>
            </div>
          </div>
          <article class="article-content-wrapper shadow-sm rounded p-4 bg-white">
            <!-- 文章头部 -->
            <header class="article-header mb-4" data-aos="fade-up">
              
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
                    <i class="fas fa-eye me-1"></i>
                    {{ article.viewCount || 0 }}
                  </span>
                  <span class="stat-item">
                    <i class="fas fa-heart me-1"></i>
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
                    :class="['btn', article.liked ? 'btn-danger' : 'btn-outline-danger']"
                    :disabled="!authStore.isAuthenticated"
                  >
                    <i :class="[article.liked ? 'fas fa-heart me-1' : 'far fa-heart me-1']"></i>
                    {{ article.liked ? '已点赞' : '点赞' }} ({{ article.likes || 0 }})
                  </button>
                  
                  <button
                    @click="toggleFavorite"
                    :class="['btn ms-2', article.isFavorited ? 'btn-warning' : 'btn-outline-warning']"
                    :disabled="!authStore.isAuthenticated"
                  >
                    <i :class="[article.isFavorited ? 'fas fa-star me-1' : 'far fa-star me-1']"></i>
                    {{ article.isFavorited ? '已收藏' : '收藏' }}
                  </button>
                </div>
                
                <div class="share-buttons">
                  <button class="btn btn-outline-secondary btn-sm" @click="shareArticle">
                                      <i class="fas fa-share-alt me-1"></i>
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
            <div class="author-card card mb-4 shadow-sm border-0" data-aos="fade-left">
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
            
            <!-- 文章目录 -->
            <div v-if="tableOfContents.length > 0" class="toc-card card mb-4 shadow-sm border-0" data-aos="fade-left">
              <div class="card-header bg-white py-2">
                <h6 class="mb-0"><i class="fas fa-list-ul me-2"></i>文章目录</h6>
              </div>
              <div class="card-body p-0">
                <nav class="article-toc">
                  <ul class="toc-list">
                    <li v-for="(item, index) in tableOfContents" :key="index" :class="['toc-item', `toc-level-${item.level}`]">
                      <a :href="`#${item.id}`" class="toc-link" @click.prevent="scrollToHeading(item.id)">
                        {{ item.text }}
                      </a>
                    </li>
                  </ul>
                </nav>
              </div>
            </div>
            
            <!-- 相关文章 -->
            <div v-if="relatedArticles.length > 0" class="related-articles card shadow-sm border-0" data-aos="fade-left" data-aos-delay="200">
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
                          <i class="fas fa-eye me-1"></i>{{ relatedArticle.viewCount || 0 }}
                          <i class="fas fa-heart ms-2 me-1"></i>{{ relatedArticle.likes || 0 }}
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
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { articleAPI, userAPI, favoriteAPI } from '@/api'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const route = useRoute()
const authStore = useAuthStore()

// 响应式数据
const loading = ref(true)
const article = ref(null)
const relatedArticles = ref([])
const renderedContent = ref('')
const tableOfContents = ref([])
const showBackToTop = ref(false)

  // 获取文章数据
  const loadArticle = async () => {
    try {
      loading.value = true
      const slug = route.params.slug
      
      const response = await articleAPI.getArticleBySlug(slug)
      
      // 检查响应是否为空或undefined
      if (!response || response.status === 404) {
        article.value = null
        return
      }
      
      article.value = response
      
      // 渲染Markdown内容
if (article.value?.content) {
  // 配置marked渲染器，为标题添加ID
  const renderer = new marked.Renderer();
  const headings = [];
  
  // 覆盖heading渲染方法，为标题添加ID
  renderer.heading = (text, level, raw) => {
    // 生成唯一的ID
    const id = `heading-${level}-${raw.toLowerCase().replace(/[^\w\u4e00-\u9fa5]+/g, '-')}`;
    
    // 收集标题信息用于生成目录
    headings.push({
      id,
      text,
      level,
      raw
    });
    
    // 返回带ID的HTML标题
    return `<h${level} id="${id}">${text}</h${level}>`;
  };
  
  // 自定义代码块渲染 - 简洁版
  renderer.code = (code, language) => {
    const langClass = language ? ` class="language-${language}"` : '';
    return `<div class="code-block-wrapper">
              <pre${langClass}>
                <code${langClass} style="color: #333333;">${code}</code>
              </pre>
            </div>`;
  };
  
  // 使用配置好的渲染器渲染Markdown
  const rawHtml = marked.parse(article.value.content, { renderer });
  renderedContent.value = DOMPurify.sanitize(rawHtml);
  
  // 更新目录数据
  tableOfContents.value = headings;
      }
      
      // 获取相关文章
      if (article.value?.articleId) {
        const relatedResponse = await articleAPI.getRelatedArticles(article.value.articleId)
        relatedArticles.value = relatedResponse || []
      }
      
    } catch (error) {
      console.error('加载文章失败:', error)
      article.value = null
    } finally {
      loading.value = false
    }
}

// 点赞功能
const toggleLike = async () => {
  if (!authStore.isAuthenticated) {
    // 如果未登录，提示用户登录
    alert('请先登录后再点赞')
    return
  }
  
  try {
    // 确保使用正确的文章ID
    const response = await articleAPI.toggleLike(article.value.articleId)
    
    // 检查响应是否成功
    if (response) {
      // 更新文章点赞状态和数量
      article.value.liked = response.liked
      article.value.likes = response.likeCount
      
      // 添加点赞动画效果
      const likeButtons = document.querySelectorAll('.quick-action-btn:first-child, .btn-outline-danger, .btn-danger');
      likeButtons.forEach(button => {
        if (response.liked) {
          button.classList.add('like-animation');
          setTimeout(() => {
            button.classList.remove('like-animation');
          }, 1000);
        }
      });
    }
  } catch (error) {
    console.error('点赞失败:', error)
    // 如果是401错误，可能是token过期，尝试刷新登录状态
    if (error.status === 401) {
      // 检查认证状态
      const isAuthenticated = await authStore.checkAuthStatus()
      if (!isAuthenticated) {
        alert('登录已过期，请重新登录')
      }
    }
  }
}

// 收藏功能
const toggleFavorite = async () => {
  if (!authStore.isAuthenticated) {
    // 如果未登录，提示用户登录
    alert('请先登录后再收藏')
    return
  }
  
  try {
    // 如果已收藏，则取消收藏
    if (article.value.isFavorited) {
      const response = await favoriteAPI.removeFromAllFolders(article.value.articleId)
      if (response) {
        article.value.isFavorited = false
      }
    } else {
      // 如果未收藏，则添加到默认收藏夹
      const response = await favoriteAPI.createAndAdd(article.value.articleId, "默认收藏夹")
      if (response) {
        article.value.isFavorited = true
      }
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    // 如果是401错误，可能是token过期，尝试刷新登录状态
    if (error.status === 401) {
      // 检查认证状态
      const isAuthenticated = await authStore.checkAuthStatus()
      if (!isAuthenticated) {
        alert('登录已过期，请重新登录')
      }
    }
  }
}

// 关注功能
const toggleFollow = async () => {
  if (!authStore.isAuthenticated) {
    // 如果未登录，提示用户登录
    alert('请先登录后再关注')
    return
  }
  
  try {
    const response = await userAPI.toggleFollow(article.value.userId)
    article.value.isFollowed = response.data.isFollowed
  } catch (error) {
    console.error('关注失败:', error)
    // 如果是401错误，可能是token过期，尝试刷新登录状态
    if (error.status === 401) {
      // 检查认证状态
      const isAuthenticated = await authStore.checkAuthStatus()
      if (!isAuthenticated) {
        alert('登录已过期，请重新登录')
      }
    }
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

// 滚动到评论区
const scrollToComments = () => {
  const commentsSection = document.getElementById('article-comments');
  if (commentsSection) {
    commentsSection.scrollIntoView({ behavior: 'smooth' });
  } else {
    // 如果评论区不存在，滚动到文章底部
    window.scrollTo({
      top: document.body.scrollHeight,
      behavior: 'smooth'
    });
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

// 滚动到指定标题
const scrollToHeading = (headingId) => {
  const element = document.getElementById(headingId);
  if (element) {
    // 计算偏移量，考虑顶部导航栏的高度
    const offset = 100; // 根据实际导航栏高度调整
    const elementPosition = element.getBoundingClientRect().top + window.pageYOffset;
    const offsetPosition = elementPosition - offset;
    
    // 平滑滚动到目标位置
    window.scrollTo({
      top: offsetPosition,
      behavior: 'smooth'
    });
    
    // 更新URL哈希，不触发浏览器滚动
    history.pushState(null, null, `#${headingId}`);
  }
};

// 检查URL哈希并滚动到对应标题
const scrollToHashOnLoad = async () => {
  await nextTick();
  const hash = window.location.hash.substring(1); // 去除前导#
  if (hash) {
    scrollToHeading(hash);
  }
};

// 高亮当前可见的标题
const highlightCurrentHeading = () => {
  // 如果没有目录项，直接返回
  if (tableOfContents.value.length === 0) return;
  
  // 获取所有标题元素
  const headingElements = tableOfContents.value.map(heading => 
    document.getElementById(heading.id)
  ).filter(Boolean);
  
  // 找到当前可见的标题
  let currentHeadingId = null;
  
  // 考虑顶部偏移量
  const scrollPosition = window.scrollY + 150;
  
  // 从后向前遍历，找到第一个在可视区域上方的标题
  for (let i = headingElements.length - 1; i >= 0; i--) {
    const element = headingElements[i];
    if (element.offsetTop <= scrollPosition) {
      currentHeadingId = element.id;
      break;
    }
  }
  
  // 如果没有找到可见标题，默认选中第一个
  if (!currentHeadingId && headingElements.length > 0) {
    currentHeadingId = headingElements[0].id;
  }
  
  // 移除所有活动类
  document.querySelectorAll('.toc-link').forEach(link => {
    link.classList.remove('active');
  });
  
  // 添加活动类到当前标题的链接
  if (currentHeadingId) {
    const activeLink = document.querySelector(`.toc-link[href="#${currentHeadingId}"]`);
    if (activeLink) {
      activeLink.classList.add('active');
    }
  }
};

// 回到顶部方法
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  });
};

// 检测滚动位置，控制回到顶部按钮的显示
const handleScroll = () => {
  showBackToTop.value = window.scrollY > 300;
  highlightCurrentHeading();
};

// 生命周期
// 移除了复制代码功能
const setupCodeCopyButtons = () => {
  // 功能已移除
};

onMounted(async () => {
  await loadArticle();
  await nextTick();
  scrollToHashOnLoad();
  
  // 添加滚动监听器
  window.addEventListener('scroll', handleScroll);
  
  // 初始化高亮
  handleScroll();
  
  // 设置代码复制按钮
  setupCodeCopyButtons();
});

// 在组件卸载时移除滚动监听器
onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll);
})
</script>

<style scoped>
/* 代码块样式 */
.code-block-wrapper {
  position: relative;
  margin: 1.5rem 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #eaeaea;
}

/* 移除了代码语言标签样式 */

/* 移除了复制按钮相关样式 */

.markdown-content pre {
  position: relative;
  margin: 0;
  padding: 1.2rem;
  background: #f8f9fa;
  border-radius: 8px;
  overflow-x: auto;
}

.markdown-content pre code {
  padding: 0;
  background: transparent;
  margin-top: 0.5rem;
  display: block;
}
.article-page {
  padding: 0 0 2rem 0;
  background-color: #f8f9fa;
}

.article-hero {
  background-color: #2d3748;
  background-image: linear-gradient(135deg, #2d3748 0%, #1a202c 100%);
  padding: 4rem 0;
  margin-bottom: 2rem;
  position: relative;
  overflow: hidden;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('/img/bg.jpg') center center;
  background-size: cover;
  opacity: 0.15;
  z-index: 1;
}

.article-hero-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 1rem;
}

.article-hero-title {
  color: white;
  font-size: 2.8rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
  line-height: 1.3;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.article-hero-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: rgba(255,255,255,0.9);
  font-size: 0.95rem;
}

.hero-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255,255,255,0.3);
}

.hero-author {
  font-weight: 600;
}

.hero-date {
  color: rgba(255,255,255,0.7);
}

.article-hero-stats {
  display: flex;
  align-items: center;
}

.hero-stat {
  display: flex;
  align-items: center;
}

/* 文章目录样式 */
.article-toc {
  /* 移除高度限制和滚动条 */
  width: 100%;
}

.toc-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.toc-item {
  padding: 0;
  margin: 0 0 0.3rem 0;
  line-height: 1.4;
}

.toc-link {
  display: block;
  padding: 0.5rem 1rem;
  color: #4a5568;
  text-decoration: none;
  border-left: 2px solid transparent;
  transition: all 0.2s ease;
  font-size: 0.9rem;
  /* 允许文本换行显示 */
  white-space: normal;
  word-break: break-word;
  line-height: 1.5;
  margin-bottom: 0.2rem;
  border-radius: 0 4px 4px 0;
}

.toc-link:hover {
  background-color: rgba(246, 213, 92, 0.1);
  color: #f6d55c;
  border-left-color: #f6d55c;
}

.toc-link.active {
  background-color: rgba(246, 213, 92, 0.15);
  color: #f6d55c;
  border-left-color: #f6d55c;
  font-weight: 600;
}

/* 目录层级缩进 */
.toc-level-1 {
  font-weight: 600;
}

.toc-level-2 {
  padding-left: 0.4rem;
}

.toc-level-3 {
  padding-left: 0.8rem;
}

.toc-level-4 {
  padding-left: 1.2rem;
}

.toc-level-5, .toc-level-6 {
  padding-left: 1.6rem;
  font-size: 0.85em;
}

.toc-card .card-header {
  border-bottom: 1px solid rgba(0,0,0,0.05);
}

.toc-card .card-header h6 {
  color: #4a5568;
  font-size: 0.95rem;
}

/* 文章标题移到hero区域 */

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
  font-size: 1.05rem;
}

.article-content-wrapper {
  border-radius: 8px;
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
  background: #edf2f7;
  padding: 0.2rem 0.4rem;
  border-radius: 4px;
  font-family: 'Fira Code', Consolas, Monaco, 'Andale Mono', monospace;
  font-size: 0.875rem;
  color: #2d3748;
  border: 1px solid #e2e8f0;
}

.markdown-content pre {
  background: #f8f9fa;
  color: #212529;
  padding: 1rem;
  border-radius: 8px;
  overflow-x: auto;
  position: relative;
  border: 1px solid #e9ecef;
}

.markdown-content pre code {
  background: none;
  color: #212529;
  padding: 0;
  font-family: 'Fira Code', Consolas, Monaco, 'Andale Mono', monospace;
  font-size: 0.95rem;
  line-height: 1.6;
  text-shadow: none;
  font-weight: 500;
}

.author-card, .toc-card, .related-articles {
  border: none;
  border-radius: 8px;
  overflow: hidden;
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

/* 左侧快捷按钮样式 */
.article-quick-actions {
  position: fixed;
  left: 3rem;
  top: 45%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  z-index: 100;
}

.quick-action-btn {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.quick-action-btn i {
  font-size: 1.2rem;
  transition: all 0.3s ease;
}

/* 各个按钮的颜色 */
.quick-action-btn:nth-child(1) i {
  color: #e53e3e; /* 点赞按钮红色 */
}

.quick-action-btn:nth-child(2) i {
  color: #f6d55c; /* 收藏按钮黄色 */
}

.quick-action-btn:nth-child(3) i {
  color: #38a169; /* 分享按钮绿色 */
}

.quick-action-btn:nth-child(4) i {
  color: #3182ce; /* 评论按钮蓝色 */
}

.quick-action-btn .action-count {
  font-size: 0.7rem;
  margin-top: 0.1rem;
  color: #718096;
}

.quick-action-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}

.quick-action-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}

/* 激活状态颜色增强 */
.quick-action-btn:nth-child(1).active i {
  color: #e53e3e;
  filter: drop-shadow(0 0 3px rgba(229, 62, 62, 0.3));
}

.quick-action-btn:nth-child(2).active i {
  color: #f6d55c;
  filter: drop-shadow(0 0 3px rgba(246, 213, 92, 0.3));
}

.quick-action-btn:nth-child(3).active i {
  color: #38a169;
  filter: drop-shadow(0 0 3px rgba(56, 161, 105, 0.3));
}

.quick-action-btn:nth-child(4).active i {
  color: #3182ce;
  filter: drop-shadow(0 0 3px rgba(49, 130, 206, 0.3));
}

/* 回到顶部按钮样式 */
.back-to-top {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #f6d55c;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 100;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.back-to-top:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.back-to-top i {
  font-size: 1.5rem;
}

/* 点赞动画效果 */
@keyframes likeAnimation {
  0% { transform: scale(1); }
  25% { transform: scale(1.2); }
  50% { transform: scale(0.95); }
  75% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.like-animation {
  animation: likeAnimation 0.5s ease-in-out;
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
  
  .article-quick-actions {
    position: fixed;
    left: 0;
    bottom: 0;
    top: auto;
    transform: none;
    flex-direction: row;
    width: 100%;
    background: white;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
    padding: 0.5rem;
    justify-content: space-around;
  }
  
  .quick-action-btn {
    width: 40px;
    height: 40px;
    box-shadow: none;
  }
  
  .back-to-top {
    bottom: 5rem;
    right: 1rem;
    width: 40px;
    height: 40px;
  }
}
</style>
