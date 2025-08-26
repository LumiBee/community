import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

// 页面组件导入
import Home from '@/views/Home.vue'
import Login from '@/views/auth/Login.vue'
import Signup from '@/views/auth/Signup.vue'
import Article from '@/views/Article.vue'
import Publish from '@/views/Publish.vue'
import Profile from '@/views/Profile.vue'
import Portfolio from '@/views/Portfolio.vue'
import PortfolioDetail from '@/views/PortfolioDetail.vue'
import Favorites from '@/views/Favorites.vue'
import Tags from '@/views/Tags.vue'
import TagArticles from '@/views/TagArticles.vue'
import Search from '@/views/Search.vue'
import Settings from '@/views/Settings.vue'
import Drafts from '@/views/Drafts.vue'
import Messages from '@/views/Messages.vue'
import NotFound from '@/views/error/NotFound.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { title: 'Lumi Hive' }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录', requiresGuest: true }
  },
  {
    path: '/signup', 
    name: 'Signup',
    component: Signup,
    meta: { title: '注册', requiresGuest: true }
  },
  {
    path: '/article/:slug',
    name: 'Article',
    component: Article,
    meta: { title: '文章详情' },
    props: true
  },
  {
    path: '/publish',
    name: 'Publish',
    component: Publish,
    meta: { title: '发布文章', requiresAuth: true }
  },
  {
    path: '/profile/:name?',
    name: 'Profile',
    component: Profile,
    meta: { title: '个人中心' },
    props: true
  },
  {
    path: '/portfolio',
    name: 'Portfolio',
    component: Portfolio,
    meta: { title: '作品集' }
  },
  {
    path: '/portfolio/:id',
    name: 'PortfolioDetail',
    component: PortfolioDetail,
    meta: { title: '作品集详情' },
    props: true
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: Favorites,
    meta: { title: '收藏夹', requiresAuth: true }
  },
  {
    path: '/tags',
    name: 'Tags',
    component: Tags,
    meta: { title: '标签' }
  },
  {
    path: '/tags/:tagName',
    name: 'TagArticles',
    component: TagArticles,
    meta: { title: '标签文章' },
    props: true
  },
  {
    path: '/search',
    name: 'Search',
    component: Search,
    meta: { title: '搜索结果' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings,
    meta: { title: '设置', requiresAuth: true }
  },
  {
    path: '/drafts',
    name: 'Drafts',
    component: Drafts,
    meta: { title: '草稿箱', requiresAuth: true }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: Messages,
    meta: { title: '私信', requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else if (to.hash) {
      return { el: to.hash }
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - Lumi Hive` : 'Lumi Hive'
  
  // 检查当前路由是否是登录或注册页面
  const isAuthPage = to.name === 'Login' || to.name === 'Signup'
  
  // 检查用户是否已认证
  const isAuthenticated = authStore.isAuthenticated
  
  // 如果用户未认证且需要认证的页面，先尝试检查认证状态
  if (!isAuthenticated && to.meta.requiresAuth) {
    // 避免重复检查，如果已经在检查中，直接重定向
    if (authStore.isLoading) {
      next({ 
        name: 'Login', 
        query: { redirect: to.fullPath } 
      })
      return
    }
    
    try {
      // 尝试从后端检查认证状态
      const authStatus = await authStore.checkAuthStatus()
      if (authStatus) {
        // 认证成功，继续导航
        next()
        return
      }
    } catch (error) {
      console.error('检查认证状态失败:', error)
    }
    
    // 认证失败，重定向到登录页
    if (!isAuthPage) {
      next({ 
        name: 'Login', 
        query: { redirect: to.fullPath } 
      })
      return
    }
  }
  
  // 如果是游客页面(登录/注册)，但用户已认证，则重定向到首页
  if (to.meta.requiresGuest && isAuthenticated) {
    next({ name: 'Home' })
    return
  }
  
  next()
})

export default router
