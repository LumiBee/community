<template>
  <div id="app">
    <!-- 导航栏 -->
    <Navbar />
    
    <!-- 主要内容区域 -->
    <main class="main-content">
      <router-view />
    </main>
    
    <!-- 页脚 -->
    <Footer />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/store/auth'
import Navbar from '@/components/layout/Navbar.vue'
import Footer from '@/components/layout/Footer.vue'

const authStore = useAuthStore()

onMounted(() => {
  // 初始化时检查用户登录状态
  authStore.checkAuthStatus()
})
</script>

<style>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding-top: 80px; /* 为固定导航栏留出空间 */
}

/* 全局样式重置 */
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #f8f9fa;
}

/* 链接样式 */
a {
  color: #f6d55c;
  text-decoration: none;
}

a:hover {
  color: #e8ca0f;
  text-decoration: none;
}

/* 按钮样式优化 */
.btn-warning {
  background: linear-gradient(135deg, #f6d55c 0%, #e8ca0f 100%);
  border: none;
  font-weight: 600;
}

.btn-warning:hover {
  background: linear-gradient(135deg, #e8ca0f 0%, #d4af37 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(232, 202, 15, 0.3);
}

/* 卡片样式 */
.card {
  border: none;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* 响应式容器 */
.container-fluid {
  max-width: 1400px;
  margin: 0 auto;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
