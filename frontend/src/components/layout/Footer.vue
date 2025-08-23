<template>
  <footer class="custom-footer">
    <div class="footer-container">
      <!-- 主要内容区域 -->
      <div class="footer-content">
        <!-- 快速链接 -->
        <div class="footer-links">
          <router-link to="/" class="footer-link">首页</router-link>
          <router-link to="/tags" class="footer-link">标签</router-link>
          <router-link to="/portfolio" class="footer-link">作品集</router-link>
          <router-link to="/about" class="footer-link">关于</router-link>
        </div>
        
        <!-- 联系方式 -->
        <div class="footer-social">
          <a href="#" class="social-link" title="GitHub">
            <i class="fab fa-github"></i>
          </a>
          <a href="#" class="social-link" title="Email">
            <i class="fas fa-envelope"></i>
          </a>
        </div>
      </div>
      
      <!-- 版权信息 -->
      <div class="footer-copyright">
        <span>© 2024 LumiHive</span>
        <span class="divider">•</span>
        <span>Vue.js & Spring Boot</span>
      </div>
    </div>
  </footer>
</template>

<script setup>
// 页脚组件不需要响应式数据
</script>

<style scoped>
/* 精简的页脚样式 */
.custom-footer {
  margin-top: auto;
  background: #343a40;
  color: #ffffff;
  padding: 2rem 0 1rem;
  border-top: 1px solid #495057;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #495057;
}

/* 快速链接 */
.footer-links {
  display: flex;
  gap: 2rem;
  align-items: center;
}

.footer-link {
  color: #adb5bd;
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.2s ease;
  padding: 0.25rem 0;
}

.footer-link:hover,
.footer-link.router-link-active {
  color: #ffda58;
  text-decoration: none;
}

/* 社交媒体链接 */
.footer-social {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.social-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #495057;
  color: #adb5bd;
  text-decoration: none;
  transition: all 0.2s ease;
  font-size: 1rem;
}

.social-link:hover {
  background: #ffda58;
  color: #343a40;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(255, 218, 88, 0.3);
}

/* 版权信息 */
.footer-copyright {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.75rem;
  color: #6c757d;
  font-size: 0.85rem;
  text-align: center;
}

.divider {
  color: #495057;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .custom-footer {
    padding: 1.5rem 0 1rem;
  }
  
  .footer-content {
    flex-direction: column;
    gap: 1.5rem;
    text-align: center;
  }
  
  .footer-links {
    gap: 1.5rem;
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .footer-social {
    gap: 1.5rem;
  }
  
  .footer-copyright {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .divider {
    display: none;
  }
}

@media (max-width: 576px) {
  .footer-container {
    padding: 0 0.75rem;
  }
  
  .footer-links {
    gap: 1rem;
    font-size: 0.85rem;
  }
  
  .social-link {
    width: 32px;
    height: 32px;
    font-size: 0.9rem;
  }
  
  .footer-copyright {
    font-size: 0.8rem;
  }
}
</style>
