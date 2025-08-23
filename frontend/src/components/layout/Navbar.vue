<template>
  <nav class="custom-navbar">
    <div class="navbar-container">
      <!-- 左侧：Logo + 导航菜单 -->
      <div class="navbar-left">
        <!-- Logo -->
        <router-link to="/" class="navbar-logo-link">
          <img alt="LumiHive" src="/img/logo.png" class="navbar-logo" />
        </router-link>
        
        <!-- 导航菜单 - 桌面端 -->
        <nav class="navbar-menu desktop-menu">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/tags" class="nav-link">标签</router-link>
          <router-link to="/portfolio" class="nav-link">作品集</router-link>
          <router-link to="/favorites" class="nav-link">收藏夹</router-link>
          <router-link to="/profile" class="nav-link">个人中心</router-link>
        </nav>
      </div>

      <!-- 右侧：搜索框 + 用户操作 -->
      <div class="navbar-right">
        <!-- 搜索框 -->
        <div class="search-container">
          <div class="search-input-wrapper">
            <input
              type="text"
              v-model="searchQuery"
              @input="handleSearchInput"
              @focus="showSearchResults = true"
              @keyup.enter="performSearch"
              class="search-input"
              placeholder="搜索..."
              autocomplete="off"
            />
            <button class="search-btn" @click="performSearch">
              <i class="fas fa-search"></i>
            </button>
          </div>
          
          <!-- 搜索结果下拉框 -->
          <div v-if="showSearchResults && searchResults.length > 0" class="search-results">
            <router-link
              v-for="article in searchResults"
              :key="article.id"
              :to="`/article/${article.slug}`"
              class="search-result-item"
              @click="showSearchResults = false"
            >
              <div class="search-result-avatar">
                <img v-if="article.avatarUrl" :src="article.avatarUrl" alt="作者头像" />
                <span v-else>{{ (article.userName || '匿名').charAt(0).toUpperCase() }}</span>
              </div>
              <div class="search-result-content">
                <div class="search-result-title">{{ article.title }}</div>
                <div class="search-result-meta">
                  <span>{{ article.userName || '匿名用户' }}</span>
                  <span>👁 {{ article.viewCount || 0 }} • ❤️ {{ article.likes || 0 }}</span>
                </div>
              </div>
            </router-link>
          </div>
          
          <!-- 搜索状态 -->
          <div v-if="showSearchResults && searchLoading" class="search-results">
            <div class="search-status">🔍 正在搜索...</div>
          </div>
          <div v-if="showSearchResults && !searchLoading && searchQuery && searchResults.length === 0" class="search-results">
            <div class="search-status">🥲 未找到相关文章</div>
          </div>
        </div>

        <!-- 用户操作区域 -->
        <div class="user-actions">
          <!-- 未登录状态 -->
          <template v-if="!authStore.isAuthenticated">
            <router-link to="/login" class="btn btn-outline-primary">登录</router-link>
            <router-link to="/signup" class="btn btn-warning">注册</router-link>
          </template>

          <!-- 已登录状态 -->
          <template v-else>
            <!-- 发布文章按钮 -->
            <router-link to="/publish" class="btn btn-warning publish-btn">
              <i class="fas fa-edit"></i>
              <span>发布文章</span>
            </router-link>

            <!-- 用户头像下拉菜单 -->
            <div class="user-dropdown">
              <button 
                class="user-avatar-btn"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                <img
                  :src="authStore.userAvatar || '/img/default01.jpg'"
                  alt="用户头像"
                  class="user-avatar"
                />
              </button>
              <ul class="dropdown-menu dropdown-menu-end">
                <li class="dropdown-header">
                  <div class="user-info">
                    <img :src="authStore.userAvatar || '/img/default01.jpg'" alt="用户头像" />
                    <strong>{{ authStore.userName || '用户' }}</strong>
                  </div>
                </li>
                <li><hr class="dropdown-divider" /></li>
                <li><router-link class="dropdown-item" to="/profile"><i class="fas fa-user"></i>个人中心</router-link></li>
                <li><router-link class="dropdown-item" to="/drafts"><i class="fas fa-file-alt"></i>草稿箱</router-link></li>
                <li><router-link class="dropdown-item" to="/messages"><i class="fas fa-envelope"></i>私信</router-link></li>
                <li><hr class="dropdown-divider" /></li>
                <li><router-link class="dropdown-item" to="/settings"><i class="fas fa-cog"></i>设置</router-link></li>
                <li><a class="dropdown-item text-danger" href="#" @click="handleLogout"><i class="fas fa-sign-out-alt"></i>退出登录</a></li>
              </ul>
            </div>
          </template>
        </div>

        <!-- 移动端菜单按钮 -->
        <button 
          class="mobile-menu-btn"
          @click="toggleMobileMenu"
          :class="{ 'active': mobileMenuOpen }"
        >
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>
    </div>

    <!-- 移动端菜单 -->
    <div class="mobile-menu" :class="{ 'open': mobileMenuOpen }">
      <router-link to="/" class="mobile-nav-link" @click="closeMobileMenu">首页</router-link>
      <router-link to="/tags" class="mobile-nav-link" @click="closeMobileMenu">标签</router-link>
      <router-link to="/portfolio" class="mobile-nav-link" @click="closeMobileMenu">作品集</router-link>
      <router-link to="/favorites" class="mobile-nav-link" @click="closeMobileMenu">收藏夹</router-link>
      <router-link to="/profile" class="mobile-nav-link" @click="closeMobileMenu">个人中心</router-link>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { articleAPI } from '@/api'

const router = useRouter()
const authStore = useAuthStore()

// 搜索相关状态
const searchQuery = ref('')
const searchResults = ref([])
const searchLoading = ref(false)
const showSearchResults = ref(false)
let searchTimeout = null

// 移动端菜单状态
const mobileMenuOpen = ref(false)

// 搜索输入处理
const handleSearchInput = () => {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
  
  if (!searchQuery.value.trim()) {
    searchResults.value = []
    showSearchResults.value = false
    return
  }
  
  searchTimeout = setTimeout(async () => {
    try {
      searchLoading.value = true
      const response = await articleAPI.searchArticles(searchQuery.value)
      searchResults.value = response || []
      showSearchResults.value = true
    } catch (error) {
      console.error('搜索失败:', error)
      searchResults.value = []
    } finally {
      searchLoading.value = false
    }
  }, 300)
}

// 执行搜索
const performSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ name: 'Search', query: { query: searchQuery.value } })
    showSearchResults.value = false
  }
}

// 处理登出
const handleLogout = async () => {
  try {
    await authStore.logout()
    router.push('/')
  } catch (error) {
    console.error('登出失败:', error)
  }
}

// 移动端菜单控制
const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

const closeMobileMenu = () => {
  mobileMenuOpen.value = false
}

// 点击外部隐藏搜索结果和移动端菜单
const handleClickOutside = (event) => {
  const searchContainer = event.target.closest('.search-container')
  if (!searchContainer) {
    showSearchResults.value = false
  }
  
  const mobileMenu = event.target.closest('.mobile-menu, .mobile-menu-btn')
  if (!mobileMenu) {
    mobileMenuOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
})
</script>

<style scoped>
/* 自定义导航栏 - 完全重写 */
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1030;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid #e9ecef;
  padding: 0;
  min-height: 70px;
}

/* 导航栏容器 */
.navbar-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1rem;
  height: 70px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 左侧区域：Logo + 导航菜单 */
.navbar-left {
  display: flex;
  align-items: center;
  flex: 1;
}

/* Logo样式 */
.navbar-logo-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  margin-right: 0.5rem;
}

.navbar-logo {
  height: 45px;
  width: auto;
  transition: all 0.2s ease;
}

.navbar-logo:hover {
  transform: scale(1.05);
}

/* 桌面端导航菜单 */
.desktop-menu {
  display: flex;
  align-items: center;
  margin: 0;
  padding: 0;
}

.desktop-menu .nav-link {
  display: block;
  padding: 0.75rem 1rem;
  margin: 0 0.125rem;
  font-weight: 500;
  color: #495057;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.desktop-menu .nav-link:first-child {
  margin-left: 0;
  padding-left: 0.5rem;
}

.desktop-menu .nav-link:hover {
  background: #f8f9fa;
  color: #ffda58;
  text-decoration: none;
}

.desktop-menu .nav-link.router-link-active {
  background: #ffda58;
  color: #333;
  font-weight: 600;
}

/* 右侧区域 */
.navbar-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

/* 搜索框 */
.search-container {
  position: relative;
  width: 280px;
}

.search-input-wrapper {
  display: flex;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-input {
  flex: 1;
  border: none;
  padding: 0.6rem 1rem;
  font-size: 0.9rem;
  background: #f8f9fa;
  outline: none;
}

.search-input:focus {
  background: white;
}

.search-btn {
  border: none;
  background: #f8f9fa;
  color: #6c757d;
  padding: 0.6rem 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.search-btn:hover {
  background: #ffda58;
  color: #333;
}

/* 搜索结果下拉 */
.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  max-height: 400px;
  overflow-y: auto;
  z-index: 1050;
  margin-top: 4px;
  padding: 8px;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.search-result-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  text-decoration: none;
  color: #2d3748;
  transition: all 0.2s ease;
  margin-bottom: 4px;
}

.search-result-item:hover {
  background: linear-gradient(135deg, #ffda58 0%, #ffc107 100%);
  color: white;
  text-decoration: none;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 218, 88, 0.3);
}

.search-result-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 12px;
  background: linear-gradient(135deg, #f6d55c 0%, #e8ca0f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
  overflow: hidden;
}

.search-result-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.search-result-content {
  flex: 1;
  min-width: 0;
}

.search-result-title {
  font-weight: 600;
  font-size: 14px;
  line-height: 1.4;
  margin-bottom: 4px;
}

.search-result-meta {
  font-size: 12px;
  opacity: 0.7;
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-status {
  padding: 16px;
  text-align: center;
  color: #718096;
  font-size: 14px;
}

/* 用户操作区域 */
.user-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1.25rem;
  border-radius: 20px;
  font-weight: 500;
  text-decoration: none;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-outline-primary {
  color: #03a87c;
  border-color: #03a87c;
  background: white;
}

.btn-outline-primary:hover {
  background: #03a87c;
  color: white;
  text-decoration: none;
}

.btn-warning {
  background: linear-gradient(135deg, #ffda58 0%, #ffc107 100%);
  color: #333;
  border: none;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(255, 218, 88, 0.3);
}

.btn-warning:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 218, 88, 0.4);
  background: linear-gradient(135deg, #ffd333 0%, #e8ca0f 100%);
  color: #333;
  text-decoration: none;
}

/* 用户头像下拉 */
.user-dropdown {
  position: relative;
}

.user-avatar-btn {
  border: none;
  background: none;
  padding: 0;
  cursor: pointer;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 下拉菜单 */
.dropdown-menu {
  min-width: 220px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  padding: 0.5rem 0;
  margin-top: 0.5rem;
}

.dropdown-header .user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.dropdown-header .user-info img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  color: #495057;
  text-decoration: none;
  transition: all 0.2s ease;
}

.dropdown-item:hover {
  background: #f8f9fa;
  text-decoration: none;
}

.dropdown-item.text-danger:hover {
  background: #fff5f5;
  color: #dc3545;
}

/* 移动端菜单按钮 */
.mobile-menu-btn {
  display: none;
  flex-direction: column;
  gap: 4px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
}

.mobile-menu-btn span {
  width: 24px;
  height: 2px;
  background: #495057;
  transition: all 0.3s ease;
}

.mobile-menu-btn.active span:nth-child(1) {
  transform: rotate(45deg) translate(6px, 6px);
}

.mobile-menu-btn.active span:nth-child(2) {
  opacity: 0;
}

.mobile-menu-btn.active span:nth-child(3) {
  transform: rotate(-45deg) translate(6px, -6px);
}

/* 移动端菜单 */
.mobile-menu {
  display: none;
  background: white;
  border-top: 1px solid #e9ecef;
  padding: 1rem;
  flex-direction: column;
  gap: 0.5rem;
}

.mobile-menu.open {
  display: flex;
}

.mobile-nav-link {
  padding: 0.75rem 1rem;
  color: #495057;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.mobile-nav-link:hover,
.mobile-nav-link.router-link-active {
  background: #ffda58;
  color: #333;
  text-decoration: none;
}

/* 响应式设计 */
@media (max-width: 991px) {
  .desktop-menu {
    display: none;
  }
  
  .mobile-menu-btn {
    display: flex;
  }
  
  .search-container {
    width: 200px;
  }
  
  .user-actions .btn span {
    display: none;
  }
}

@media (max-width: 768px) {
  .navbar-container {
    padding: 0 0.75rem;
  }
  
  .navbar-logo {
    height: 40px;
  }
  
  .search-container {
    width: 150px;
  }
  
  .navbar-right {
    gap: 0.5rem;
  }
  
  .btn {
    padding: 0.4rem 1rem;
    font-size: 0.9rem;
  }
}

@media (max-width: 576px) {
  .search-container {
    width: 120px;
  }
  
  .search-input {
    font-size: 0.8rem;
    padding: 0.5rem 0.75rem;
  }
  
  .search-btn {
    padding: 0.5rem 0.75rem;
  }
}
</style>
