<template>
  <nav class="custom-navbar" :class="{ 'scrolled': isScrolled }">
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
          <router-link :to="`/profile/${authStore.userName}`" class="nav-link">个人中心</router-link>
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
                                    <img v-if="article.avatarUrl" :src="getAuthorAvatarUrl(article.avatarUrl)" alt="作者头像" />
                <span v-else>{{ (article.userName || '匿名').charAt(0).toUpperCase() }}</span>
              </div>
              <div class="search-result-content">
                <div class="search-result-title">{{ article.title }}</div>
                <div class="search-result-meta">
                  <span class="author-name">{{ article.userName || '匿名用户' }}</span>
                  <div class="stats">
                    <span class="stat-item">
                      <i class="fas fa-eye"></i>
                      <span class="stat-value">{{ formatNumber(article.viewCount || 0) }}</span>
                    </span>
                    <span class="stat-item">
                      <i class="fas fa-heart"></i>
                      <span class="stat-value">{{ formatNumber(article.likes || 0) }}</span>
                    </span>
                  </div>
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
                @click="toggleUserDropdown"
                :aria-expanded="userDropdownOpen"
              >
                <img
                  :src="getAvatarUrl(authStore.userAvatar)"
                  alt="用户头像"
                  class="user-avatar"
                />
              </button>
              <ul class="dropdown-menu dropdown-menu-end" :class="{ 'show': userDropdownOpen }">
                <li class="dropdown-header">
                  <div class="user-info">
                    <img :src="getAvatarUrl(authStore.userAvatar)" alt="用户头像" />
                    <strong>{{ authStore.userName || '用户' }}</strong>
                  </div>
                </li>
                <li><hr class="dropdown-divider" /></li>
                <li><router-link class="dropdown-item" :to="`/profile/${authStore.userName}`" @click="closeUserDropdown"><i class="fas fa-user"></i>个人中心</router-link></li>
                <li><router-link class="dropdown-item" to="/drafts" @click="closeUserDropdown"><i class="fas fa-file-alt"></i>草稿箱</router-link></li>
                <li><router-link class="dropdown-item" to="/messages" @click="closeUserDropdown"><i class="fas fa-envelope"></i>私信</router-link></li>
                <li><hr class="dropdown-divider" /></li>
                <li><router-link class="dropdown-item" to="/settings" @click="closeUserDropdown"><i class="fas fa-cog"></i>设置</router-link></li>
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
      <router-link :to="`/profile/${authStore.userName}`" class="mobile-nav-link" @click="closeMobileMenu">个人中心</router-link>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { articleAPI } from '@/api'
import { getAvatarUrl } from '@/utils/avatar-helper'

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

// 用户头像下拉菜单状态
const userDropdownOpen = ref(false)

// 滚动状态
const isScrolled = ref(false)

// 监听滚动事件
const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

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

// 用户头像下拉菜单控制
const toggleUserDropdown = () => {
  userDropdownOpen.value = !userDropdownOpen.value
}

const closeUserDropdown = () => {
  userDropdownOpen.value = false
}

// 数字格式化方法
const formatNumber = (num) => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}

// 使用全局工具函数，移除本地定义

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

  const userDropdown = event.target.closest('.user-dropdown')
  if (!userDropdown) {
    userDropdownOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  window.removeEventListener('scroll', handleScroll)
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
})
</script>

<style scoped>
/* 页面加载动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.custom-navbar {
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.navbar-left {
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.1s both;
}

.navbar-right {
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.2s both;
}

/* 导航链接的延迟动画 */
.desktop-menu .nav-link {
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.3s both;
}

.desktop-menu .nav-link:nth-child(2) {
  animation-delay: 0.4s;
}

.desktop-menu .nav-link:nth-child(3) {
  animation-delay: 0.5s;
}

.desktop-menu .nav-link:nth-child(4) {
  animation-delay: 0.6s;
}

.desktop-menu .nav-link:nth-child(5) {
  animation-delay: 0.7s;
}

/* 自定义导航栏 - 完全重写 */
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1030;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(233, 236, 239, 0.3);
  padding: 0;
  min-height: 70px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.custom-navbar.scrolled {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(30px);
  border-bottom: 1px solid rgba(233, 236, 239, 0.5);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  min-height: 65px;
}

.custom-navbar.scrolled .navbar-container {
  height: 65px;
}

.custom-navbar.scrolled .navbar-logo {
  height: 38px;
}

.custom-navbar.scrolled .user-avatar {
  width: 38px;
  height: 38px;
}

/* 导航栏容器 */
.navbar-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  height: 70px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 左侧区域：Logo + 导航菜单 */
.navbar-left {
  display: flex;
  align-items: center;
  flex: 1;
  margin-right: auto;
  padding-right: 20px;
}

/* Logo样式 */
.navbar-logo-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  margin-right: 2rem;
  margin-left: -0.5rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background-color: white;
}

.navbar-logo {
  height: 42px;
  width: auto;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.navbar-logo:hover {
  transform: scale(1.05) translateY(-1px);
}

/* 桌面端导航菜单 */
.desktop-menu {
  display: flex;
  align-items: center;
  margin: 0;
  padding: 0;
  margin-left: -10px;
}

.desktop-menu .nav-link {
  display: block;
  padding: 0.75rem 1.25rem;
  margin: 0 0.25rem;
  font-weight: 500;
  font-size: 1.05rem;
  color: #495057;
  text-decoration: none;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.desktop-menu .nav-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #ffc107 0%, #ffda58 100%);
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: -1;
}

.desktop-menu .nav-link:hover::before {
  opacity: 1;
  transform: scale(1);
}

.desktop-menu .nav-link:hover {
  color: #2c3e50;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.3);
}

.desktop-menu .nav-link.router-link-active {
  color: #2c3e50;
  background: linear-gradient(135deg, #ffc107 0%, #ffda58 100%);
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.3);
  transform: translateY(-2px);
}

/* 右侧区域 */
.navbar-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

/* 搜索容器 */
.search-container {
  position: relative;
  width: 280px;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  border: 2px solid transparent;
  border-radius: 25px;
  background: rgba(248, 249, 250, 0.8);
  font-size: 1rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
}

.search-input:focus {
  outline: none;
  border-color: #ffc107;
  background: white;
  box-shadow: 0 4px 20px rgba(255, 193, 7, 0.2);
  transform: translateY(-1px);
}

.search-btn {
  position: absolute;
  left: 0.75rem;
  background: none;
  border: none;
  color: #6c757d;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-btn:hover {
  color: #ffc107;
  transform: scale(1.1);
}

/* 搜索结果 */
.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-top: 0.5rem;
  overflow: hidden;
  animation: slideDown 0.2s ease-out;
  z-index: 1000;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.search-result-item {
  display: flex;
  align-items: center;
  padding: 0.875rem 1rem;
  text-decoration: none;
  color: inherit;
  transition: all 0.15s ease;
  border-bottom: 1px solid #f8f9fa;
}

.search-result-item:hover {
  background: #f1f3f4;
  transform: translateX(1px);
  box-shadow: inset 2px 0 0 #6c757d;
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6c757d;
  font-weight: 500;
  font-size: 0.75rem;
  margin-right: 0.75rem;
  flex-shrink: 0;
  border: 1px solid #e9ecef;
}

.search-result-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.search-result-content {
  flex: 1;
  min-width: 0;
}

.search-result-title {
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 0.375rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 0.875rem;
  line-height: 1.3;
}

.search-result-meta {
  font-size: 0.75rem;
  color: #6c757d;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}

.author-name {
  font-weight: 500;
  color: #495057;
}

.stats {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #ffc107;
}

.stat-item i {
  font-size: 0.7rem;
  opacity: 0.8;
}

.stat-value {
  font-size: 0.7rem;
  font-weight: 500;
  color: #ffc107;
}

.search-status {
  padding: 1rem;
  text-align: center;
  color: #6c757d;
  font-size: 0.8rem;
  font-weight: 500;
}

/* 用户操作区域 */
.user-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

/* 按钮涟漪效果 */
.btn {
  position: relative;
  overflow: hidden;
  padding: 0.6rem 1.2rem;
  border-radius: 12px;
  font-weight: 500;
  font-size: 1.05rem;
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.btn:active::after {
  width: 300px;
  height: 300px;
}

/* 搜索框聚焦动画 */
.search-input:focus + .search-btn {
  color: #ffc107;
  transform: scale(1.1) rotate(5deg);
}

/* 用户头像悬停时的光晕效果 */
.user-avatar-btn::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #ffc107, #ffda58, #ffc107);
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: -1;
}

.user-avatar-btn:hover::before {
  opacity: 1;
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 下拉菜单项的波纹效果 */
.dropdown-item {
  position: relative;
  overflow: hidden;
}

.dropdown-item::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 193, 7, 0.2);
  transform: translate(-50%, -50%);
  transition: width 0.4s, height 0.4s;
}

.dropdown-item:active::after {
  width: 200px;
  height: 200px;
}

.btn-outline-primary {
  color: #0d6efd;
  background: transparent;
  border: 2px solid #0d6efd;
}

.btn-outline-primary:hover {
  background: #0d6efd;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.3);
}

.btn-warning {
  background: linear-gradient(135deg, #ffc107 0%, #ffda58 100%);
  color: #2c3e50;
  border: none;
}

.btn-warning:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.3);
  filter: brightness(1.05);
}

.publish-btn {
  font-weight: 600;
  padding: 0.7rem 1.4rem;
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
  border-radius: 50%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.user-avatar-btn:hover {
  transform: scale(1.05);
}

.user-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.user-avatar-btn:hover .user-avatar {
  border-color: #ffc107;
  box-shadow: 0 6px 20px rgba(255, 193, 7, 0.3);
  transform: scale(1.05);
}

/* 下拉菜单 */
.dropdown-menu {
  min-width: 240px;
  border-radius: 16px;
  border: none;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  padding: 0.75rem 0;
  margin-top: 0.75rem;
  display: none;
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  z-index: 1000;
  backdrop-filter: blur(20px);
  animation: dropdownFade 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 添加箭头 */
.dropdown-menu::before {
  content: '';
  position: absolute;
  top: -10px;
  left: 50%;
  transform: translateX(-50%);
  border-left: 10px solid transparent;
  border-right: 10px solid transparent;
  border-bottom: 10px solid white;
}

@keyframes dropdownFade {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-10px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0) scale(1);
  }
}

.dropdown-menu.show {
  display: block;
}

.dropdown-header {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid #f8f9fa;
  margin-bottom: 0.5rem;
}

.dropdown-header .user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.dropdown-header .user-info img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #ffc107;
}

.dropdown-header .user-info strong {
  color: #2c3e50;
  font-weight: 600;
}

.dropdown-divider {
  margin: 0.5rem 0;
  border: none;
  border-top: 1px solid #f8f9fa;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  color: #495057;
  text-decoration: none;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px;
  margin: 0 0.5rem;
  font-weight: 500;
}

.dropdown-item:hover {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  text-decoration: none;
  transform: translateX(4px);
  color: #2c3e50;
}

.dropdown-item i {
  width: 16px;
  text-align: center;
  color: #6c757d;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.dropdown-item:hover i {
  color: #ffc107;
  transform: scale(1.1);
}

.dropdown-item.text-danger {
  color: #dc3545;
}

.dropdown-item.text-danger:hover {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe6e6 100%);
  color: #dc3545;
}

.dropdown-item.text-danger:hover i {
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
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.mobile-menu-btn:hover {
  background: rgba(255, 193, 7, 0.1);
  transform: scale(1.05);
}

.mobile-menu-btn span {
  width: 24px;
  height: 2px;
  background: #495057;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 1px;
}

.mobile-menu-btn.active span:nth-child(1) {
  transform: rotate(45deg) translate(6px, 6px);
  background: #ffc107;
}

.mobile-menu-btn.active span:nth-child(2) {
  opacity: 0;
  transform: scale(0);
}

.mobile-menu-btn.active span:nth-child(3) {
  transform: rotate(-45deg) translate(6px, -6px);
  background: #ffc107;
}

/* 移动端菜单 */
.mobile-menu {
  display: none;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(233, 236, 239, 0.3);
  padding: 1.5rem;
  flex-direction: column;
  gap: 0.75rem;
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 1020;
  animation: mobileMenuSlide 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

@keyframes mobileMenuSlide {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.mobile-menu.open {
  display: flex;
}

.mobile-nav-link {
  padding: 1rem 1.25rem;
  color: #495057;
  text-decoration: none;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
  position: relative;
  overflow: hidden;
}

.mobile-nav-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #ffc107 0%, #ffda58 100%);
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: -1;
}

.mobile-nav-link:hover::before {
  opacity: 1;
  transform: scale(1);
}

.mobile-nav-link:hover,
.mobile-nav-link.router-link-active {
  color: #2c3e50;
  transform: translateX(8px);
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.3);
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
    width: 240px;
  }
  
  .user-actions .btn span {
    display: none;
  }
  
  .navbar-left {
    padding-right: 10px;
  }
  
  .navbar-right {
    padding-left: 10px;
  }
  
  .publish-btn {
    padding: 0.6rem 1rem;
  }
  
  .publish-btn span {
    display: none;
  }
}

@media (max-width: 768px) {
  .navbar-container {
    padding: 0 1rem;
  }
  
  .navbar-logo {
    height: 38px;
  }
  
  .navbar-logo-link {
    margin-left: -0.25rem;
    margin-right: 1.5rem;
  }
  
  .search-container {
    width: 200px;
    margin-right: 8px;
  }
  
  .navbar-right {
    gap: 0.5rem;
  }
  
  .btn {
    padding: 0.5rem 0.9rem;
    font-size: 0.85rem;
  }
  
  .user-avatar {
    width: 38px;
    height: 38px;
  }
  
  .mobile-menu {
    padding: 1.25rem;
  }
}

@media (max-width: 576px) {
  .navbar-container {
    padding: 0 0.75rem;
  }
  
  .navbar-logo {
    height: 36px;
  }
  
  .search-container {
    width: 160px;
  }
  
  .search-input {
    padding: 0.6rem 0.75rem 0.6rem 2rem;
    font-size: 0.85rem;
  }
  
  .search-btn {
    left: 0.5rem;
  }
  
  .btn {
    padding: 0.45rem 0.8rem;
    font-size: 0.8rem;
  }
  
  .user-avatar {
    width: 36px;
    height: 36px;
  }
  
  .search-results {
    border-radius: 6px;
    margin-top: 0.25rem;
  }
  
  .search-result-item {
    padding: 0.75rem;
  }
  
  .search-result-item:hover {
    background: #f1f3f4;
    transform: translateX(0);
    box-shadow: inset 1px 0 0 #6c757d;
  }
  
  .search-result-avatar {
    width: 24px;
    height: 24px;
    font-size: 0.7rem;
    margin-right: 0.5rem;
  }
  
  .search-result-title {
    font-size: 0.8rem;
    margin-bottom: 0.25rem;
  }
  
  .search-result-meta {
    font-size: 0.7rem;
  }
  
  .stats {
    gap: 0.5rem;
  }
  
  .stat-item {
    gap: 0.2rem;
  }
  
  .stat-item i {
    font-size: 0.65rem;
  }
  
  .stat-value {
    font-size: 0.65rem;
  }
  
  .mobile-menu {
    padding: 1rem;
  }
  
  .mobile-nav-link {
    padding: 0.875rem 1rem;
    font-size: 0.95rem;
  }
}
</style>
