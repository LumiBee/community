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
          <!-- 文章标题 -->
          <h1 class="article-hero-title">{{ article.title }}</h1>
          
          <!-- 文章标签和作品集信息 -->
          <div class="hero-tags-portfolio" v-if="article.tags && article.tags.length > 0 || article.portfolio">
            <!-- 标签 -->
            <div class="hero-tags" v-if="article.tags && article.tags.length > 0">
              <div class="tags-container">
                <router-link
                  v-for="tag in article.tags"
                  :key="tag.tagId"
                  :to="`/tags#${tag.name}`"
                  class="hero-tag"
                >
                  <i class="fas fa-tag"></i>
                  {{ tag.name }}
                </router-link>
              </div>
            </div>
            
            <!-- 作品集 -->
            <div class="hero-portfolio" v-if="article.portfolio">
              <router-link
                :to="`/portfolio/${article.portfolio.id}`"
                class="portfolio-link"
              >
                <i class="fas fa-book-open"></i>
                <span>{{ article.portfolio.name }}</span>
              </router-link>
            </div>
          </div>
          
          <!-- 文章基本信息 -->
          <div class="article-hero-meta">
            <div class="meta-left">
              <div class="author-info">
                <router-link :to="`/profile/${article.userName}`" class="hero-avatar-link">
                  <img :src="getAuthorAvatarUrl(article.avatarUrl)" alt="作者头像" class="hero-avatar">
                </router-link>
                <div class="author-details">
                  <span class="hero-author">{{ article.userName || '匿名' }}</span>
                  <span class="hero-date">{{ formatDate(article.gmtCreate) }}</span>
                </div>
              </div>
            </div>
            <div class="meta-right">
              <button
                v-if="authStore.isAuthenticated && article.userId !== authStore.user?.id"
                @click="toggleFollow"
                :class="['btn btn-sm follow-btn', article.isFollowed ? 'btn-secondary' : 'btn-warning']"
              >
                <i class="fas fa-user-plus me-1"></i>
                {{ article.isFollowed ? '已关注' : '关注' }}
              </button>
            </div>
          </div>
          
          <!-- 作者详细信息 -->
          <div class="hero-author-details">
            <div class="author-bio-hero">{{ article.userBio || '这个用户很懒，什么都没有留下...' }}</div>
            <div class="author-stats-hero">
              <span class="stat-item-hero">
                <i class="fas fa-file-alt"></i>
                <span class="stat-number">{{ article.userArticleCount || 0 }}</span>
                <span class="stat-label">文章</span>
              </span>
              <span class="stat-item-hero">
                <i class="fas fa-users"></i>
                <span class="stat-number">{{ article.userFollowersCount || 0 }}</span>
                <span class="stat-label">粉丝</span>
              </span>
              <span class="stat-item-hero">
                <i class="fas fa-user-plus"></i>
                <span class="stat-number">{{ article.userFollowingCount || 0 }}</span>
                <span class="stat-label">关注</span>
              </span>
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
                  <router-link :to="`/profile/${article.userName}`" class="author-avatar-link">
                    <img
                      :src="getAuthorAvatarUrl(article.avatarUrl)"
                      alt="作者头像"
                      class="author-avatar me-3"
                    />
                  </router-link>
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
            <!-- 文章目录 - 移到最上面 -->
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
    
    <!-- 收藏选择模态框 -->
    <FavoriteModal
      v-if="article?.articleId"
      :visible="showFavoriteModal"
      :article-id="article.articleId"
      @close="closeFavoriteModal"
      @success="handleFavoriteSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, getCurrentInstance } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { articleAPI, userAPI, favoriteAPI } from '@/api'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import FavoriteModal from '@/components/FavoriteModal.vue'
import { ensureBigIntAsString, debugId } from '@/utils/bigint-helper'
import { getAuthorAvatarUrl } from '@/utils/avatar-helper'

const route = useRoute()
const authStore = useAuthStore()
const { proxy } = getCurrentInstance()

// 响应式数据
const loading = ref(true)
const article = ref(null)
const relatedArticles = ref([])
const renderedContent = ref('')
const tableOfContents = ref([])
const showBackToTop = ref(false)
const showFavoriteModal = ref(false)

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

      
      // 确保收藏状态正确设置
      if (article.value) {
        // 确保布尔值字段正确设置
        article.value.isFavorited = Boolean(article.value.favorited)
        article.value.liked = Boolean(article.value.liked)
        article.value.isFollowed = Boolean(article.value.followed)


        // 如果用户已登录且不是自己的文章，检查关注状态
        if (authStore.isAuthenticated && article.value.userId !== authStore.user?.id) {
          try {
            const followStatus = await userAPI.isFollowing(article.value.userId)
            article.value.isFollowed = Boolean(followStatus.isFollowing)
          } catch (error) {
            console.error('检查关注状态失败:', error)
            // 如果检查失败，保持原有状态
          }
        }
      }
      
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
    if (window.$toast) {
      window.$toast.warning('请先登录后再点赞')
    } else {
      console.error('Toast not available')
      alert('请先登录后再点赞')
    }
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
      
      // 显示点赞状态提示
      if (window.$toast) {
        if (response.liked) {
          window.$toast.success('点赞成功！')
        } else {
          window.$toast.info('已取消点赞')
        }
      }
      
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
        if (window.$toast) {
          window.$toast.error('登录已过期，请重新登录')
        } else {
          alert('登录已过期，请重新登录')
        }
      }
    } else {
      if (window.$toast) {
        window.$toast.error('点赞失败，请稍后重试')
      } else {
        alert('点赞失败，请稍后重试')
      }
    }
  }
}

// 收藏功能
const toggleFavorite = async () => {
  if (!authStore.isAuthenticated) {
    // 如果未登录，提示用户登录
    if (window.$toast) {
      window.$toast.warning('请先登录后再收藏')
    } else {
      alert('请先登录后再收藏')
    }
    return
  }
  
  // 如果已收藏，则取消收藏
  if (article.value.isFavorited) {
    try {
      const response = await favoriteAPI.removeFromAllFolders(article.value.articleId)
      if (response) {
        article.value.isFavorited = false
        if (window.$toast) {
          window.$toast.info('已取消收藏')
        }
      }
    } catch (error) {
      console.error('取消收藏失败:', error)
      if (window.$toast) {
        window.$toast.error('取消收藏失败，请稍后重试')
      } else {
        alert('取消收藏失败，请稍后重试')
      }
    }
  } else {
    // 如果未收藏，显示收藏选择模态框
    showFavoriteModal.value = true
  }
}

// 处理收藏成功
const handleFavoriteSuccess = (result) => {
  article.value.isFavorited = true
  showFavoriteModal.value = false

  // 显示成功提示
  if (window.$toast) {
    if (result.type === 'add') {
      if (result.folderCount > 1) {
        if (result.alreadyFavoritedCount > 0) {
          window.$toast.success(`文章已添加到 ${result.folderCount} 个收藏夹（其中 ${result.alreadyFavoritedCount} 个已存在）`)
        } else {
          window.$toast.success(`文章已添加到 ${result.folderCount} 个收藏夹`)
        }
      } else {
        if (result.alreadyFavoritedCount > 0) {
          window.$toast.info('文章已在收藏夹中')
        } else {
          window.$toast.success('文章已添加到收藏夹')
        }
      }
    } else if (result.type === 'create') {
      window.$toast.success(`文章已添加到新创建的收藏夹"${result.folderName}"`)
    }
  }
}

// 关闭收藏模态框
const closeFavoriteModal = () => {
  showFavoriteModal.value = false
}

// 关注功能
const toggleFollow = async () => {
  if (!authStore.isAuthenticated) {
    // 如果未登录，提示用户登录
    if (window.$toast) {
      window.$toast.warning('请先登录后再关注')
    } else {
      alert('请先登录后再关注')
    }
    return
  }
  
  try {
    // 确保用户ID作为字符串处理，避免JavaScript大整数精度丢失
    const userIdStr = ensureBigIntAsString(article.value.userId);
    debugId(article.value.userId, '文章作者ID');
    console.log('关注操作 - 文章作者ID (字符串):', userIdStr, '类型:', typeof userIdStr)
    const response = await userAPI.toggleFollow(userIdStr)
    console.log('关注响应:', response)
    
    // 检查响应格式 - 响应数据直接是对象，不是包装在 response.data 中
    if (response && response.isFollowing !== undefined) {
      // 后端返回的是 isFollowing，我们需要更新为 isFollowed
      article.value.isFollowed = response.isFollowing
      
      // 显示关注状态提示
      if (window.$toast) {
        if (response.isFollowing) {
          window.$toast.success(response.message || '关注成功！')
        } else {
          window.$toast.info(response.message || '已取消关注')
        }
      }
    } else {
      console.error('响应格式错误:', response)
      if (window.$toast) {
        window.$toast.error('关注操作失败，请重试')
      }
    }
  } catch (error) {
    console.error('关注失败:', error)
    // 如果是401错误，可能是token过期，尝试刷新登录状态
    if (error.status === 401) {
      // 检查认证状态
      const isAuthenticated = await authStore.checkAuthStatus()
      if (!isAuthenticated) {
        if (window.$toast) {
          window.$toast.error('登录已过期，请重新登录')
        } else {
          alert('登录已过期，请重新登录')
        }
      }
    } else {
      if (window.$toast) {
        window.$toast.error('关注失败，请稍后重试')
      } else {
        alert('关注失败，请稍后重试')
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
    }).then(() => {
      if (window.$toast) {
        window.$toast.success('分享成功！')
      }
    }).catch((error) => {
      if (error.name !== 'AbortError') {
        if (window.$toast) {
          window.$toast.error('分享失败，请重试')
        }
      }
    })
  } else {
    // 复制链接到剪贴板
    navigator.clipboard.writeText(window.location.href)
      .then(() => {
        if (window.$toast) {
          window.$toast.success('链接已复制到剪贴板')
        }
      })
      .catch(() => {
        if (window.$toast) {
          window.$toast.error('复制失败，请手动复制链接')
        }
      })
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
  padding: 2.5rem 0 2rem;
  margin-bottom: 2rem;
  position: relative;
  overflow: hidden;
  color: white;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('/img/bg.jpg') center center;
  background-size: cover;
  opacity: 0.1;
  z-index: 1;
}

.article-hero-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 1rem;
  position: relative;
  z-index: 2;
}

.article-hero-title {
  font-size: 2.4rem;
  font-weight: 800;
  margin-bottom: 1.25rem;
  line-height: 1.2;
  text-shadow: 0 4px 8px rgba(0,0,0,0.15);
  background: linear-gradient(45deg, #ffffff, #f8fafc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 标签和作品集样式 */
.hero-tags-portfolio {
  margin-bottom: 1.25rem;
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: center;
}

.hero-tags {
  display: flex;
  align-items: center;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.hero-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0.75rem;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  color: white;
  text-decoration: none;
  font-size: 0.85rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.hero-tag:hover {
  background: rgba(255, 255, 255, 0.25);
  color: white;
  text-decoration: none;
}

.hero-tag i {
  font-size: 0.8rem;
  color: #f6d55c;
}

.hero-portfolio {
  display: flex;
  align-items: center;
}

.portfolio-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0.75rem;
  background: rgba(246, 213, 92, 0.25);
  border: 1px solid rgba(246, 213, 92, 0.4);
  border-radius: 4px;
  color: #f6d55c;
  text-decoration: none;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.portfolio-link:hover {
  background: rgba(246, 213, 92, 0.3);
  color: #f6d55c;
  text-decoration: none;
}

.portfolio-link i {
  font-size: 0.9rem;
}

/* 文章基本信息样式 */
.article-hero-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
  color: rgba(255, 255, 255, 0.9);
}

.meta-left {
  display: flex;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.hero-avatar-link {
  display: block;
  transition: transform 0.3s ease;
}

.hero-avatar-link:hover {
  transform: scale(1.05);
}

.hero-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.hero-avatar-link:hover .hero-avatar {
  border-color: rgba(246, 213, 92, 0.6);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.hero-author {
  font-weight: 700;
  font-size: 1.1rem;
  color: white;
}

.hero-date {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
}

.meta-right {
  display: flex;
  align-items: center;
}

.article-stats {
  display: flex;
  gap: 1.5rem;
}

.hero-stat {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: rgba(255, 255, 255, 0.9);
}

.hero-stat i {
  color: #f6d55c;
  font-size: 1rem;
}

.stat-number {
  font-weight: 600;
  font-size: 1rem;
}

/* Hero区域作者详细信息样式 */
.hero-author-details {
  margin-top: 1.25rem;
  padding-top: 1.25rem;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.9);
}

.author-bio-hero {
  font-size: 0.9rem;
  line-height: 1.6;
  margin-bottom: 1rem;
  color: rgba(255, 255, 255, 0.9);
  font-style: italic;
}

.author-stats-hero {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  gap: 1.25rem;
}

.stat-item-hero {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.9rem;
}

.stat-item-hero i {
  color: #f6d55c;
  font-size: 1rem;
}

.stat-item-hero .stat-number {
  font-weight: 700;
  font-size: 1.1rem;
  color: white;
}

.stat-item-hero .stat-label {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.85rem;
}

.follow-btn {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-weight: 600;
  transition: all 0.3s ease;
  border: none;
}

.follow-btn:hover {
  transform: translateY(-1px);
}

/* 文章目录样式 - 更紧凑的设计 */
.article-toc {
  width: 100%;
}

.toc-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.toc-item {
  padding: 0;
  margin: 0 0 0.15rem 0; /* 减少项目间距 */
  line-height: 1.3; /* 更紧凑的行高 */
}

.toc-link {
  display: block;
  padding: 0.35rem 0.75rem; /* 减少内边距 */
  color: #4a5568;
  text-decoration: none;
  border-left: 2px solid transparent;
  transition: all 0.2s ease;
  font-size: 0.85rem; /* 稍微减小字体 */
  white-space: normal;
  word-break: break-word;
  line-height: 1.4; /* 更紧凑的行高 */
  margin-bottom: 0.1rem; /* 减少底部间距 */
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

/* 目录层级缩进 - 更紧凑 */
.toc-level-1 {
  font-weight: 600;
}

.toc-level-2 {
  padding-left: 0.3rem; /* 减少缩进 */
}

.toc-level-3 {
  padding-left: 0.6rem; /* 减少缩进 */
}

.toc-level-4 {
  padding-left: 0.9rem; /* 减少缩进 */
}

.toc-level-5, .toc-level-6 {
  padding-left: 1.2rem; /* 减少缩进 */
  font-size: 0.8em; /* 更小的字体 */
}

.toc-card .card-header {
  border-bottom: 1px solid rgba(0,0,0,0.05);
  padding: 0.75rem 1rem; /* 减少头部内边距 */
}

.toc-card .card-header h6 {
  color: #4a5568;
  font-size: 0.9rem; /* 稍微减小标题字体 */
  margin: 0;
}

/* 文章标题移到hero区域 */

.author-avatar {
  width: 48px;
  height: 48px;
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
  top: 20px; /* 减少顶部距离，让目录更早显示 */
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
  .article-hero {
    padding: 2rem 0 1.5rem;
  }
  
  .article-hero-title {
    font-size: 1.8rem;
    margin-bottom: 1rem;
  }
  
  .hero-tags-portfolio {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
  
  .article-hero-meta {
    flex-direction: row;
    gap: 1rem;
    padding: 1rem;
    align-items: center;
  }
  
  .meta-left {
    flex: 1;
  }
  
  .meta-right {
    flex-shrink: 0;
  }
  
  .author-info {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }
  
  .article-stats {
    justify-content: flex-start;
    gap: 1rem;
  }
  
  .hero-author-details {
    padding: 1.5rem;
  }
  
  .author-stats-hero {
    gap: 1rem;
  }
  
  .stat-item-hero {
    padding: 0.5rem 0.75rem;
  }
  
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
    border: 1px solid rgba(0, 0, 0, 0.15);
    box-shadow: none;
  }
  
  .hero-avatar {
    width: 40px;
    height: 40px;
  }
  
  .hero-author {
    font-size: 1rem;
  }
  
  .hero-date {
    font-size: 0.8rem;
  }
  
  .follow-btn {
    padding: 0.25rem 0.5rem;
    font-size: 0.8rem;
  }
  
  .back-to-top {
    bottom: 5rem;
    right: 1rem;
    width: 40px;
    height: 40px;
  }
}

@media (max-width: 576px) {
  .article-hero-meta {
    padding: 0.75rem;
    gap: 0.75rem;
  }
  
  .author-info {
    gap: 0.5rem;
  }
  
  .hero-avatar {
    width: 36px;
    height: 36px;
  }
  
  .hero-author {
    font-size: 0.9rem;
  }
  
  .hero-date {
    font-size: 0.75rem;
  }
  
  .follow-btn {
    padding: 0.2rem 0.4rem;
    font-size: 0.75rem;
  }
  
  .follow-btn i {
    font-size: 0.7rem;
  }
}
</style>
