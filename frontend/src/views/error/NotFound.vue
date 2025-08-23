<template>
  <div class="not-found-container">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-md-8 text-center">
          <div class="error-content" data-aos="fade-up">
            <div class="error-number">404</div>
            <h1 class="error-title">页面未找到</h1>
            <p class="error-description">
              抱歉，您访问的页面不存在或已被移除。
            </p>
            <div class="error-actions">
              <router-link to="/" class="btn btn-warning me-3">
                <i class="fas fa-home me-2"></i>
                返回首页
              </router-link>
              <button @click="goBack" class="btn btn-outline-secondary">
                <i class="fas fa-arrow-left me-2"></i>
                返回上页
              </button>
            </div>
          </div>
          
          <!-- 推荐内容 -->
          <div class="suggestions mt-5" data-aos="fade-up" data-aos-delay="200">
            <h4>您可能感兴趣的内容</h4>
            <div class="row mt-4">
              <div class="col-md-4 mb-3">
                <div class="suggestion-card">
                  <i class="fas fa-home fa-2x text-warning mb-3"></i>
                  <h5>首页</h5>
                  <p>浏览最新的技术文章和精彩内容</p>
                  <router-link to="/" class="btn btn-sm btn-outline-warning">访问</router-link>
                </div>
              </div>
              <div class="col-md-4 mb-3">
                <div class="suggestion-card">
                  <i class="fas fa-tags fa-2x text-warning mb-3"></i>
                  <h5>标签</h5>
                  <p>按标签分类浏览感兴趣的主题</p>
                  <router-link to="/tags" class="btn btn-sm btn-outline-warning">访问</router-link>
                </div>
              </div>
              <div class="col-md-4 mb-3">
                <div class="suggestion-card">
                  <i class="fas fa-briefcase fa-2x text-warning mb-3"></i>
                  <h5>作品集</h5>
                  <p>查看社区成员的优秀作品</p>
                  <router-link to="/portfolio" class="btn btn-sm btn-outline-warning">访问</router-link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.not-found-container {
  min-height: 70vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
}

.error-content {
  margin-bottom: 3rem;
}

.error-number {
  font-size: 8rem;
  font-weight: 700;
  color: #f6d55c;
  line-height: 1;
  margin-bottom: 1rem;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.error-title {
  font-size: 2.5rem;
  font-weight: 600;
  color: #495057;
  margin-bottom: 1rem;
}

.error-description {
  font-size: 1.1rem;
  color: #6c757d;
  margin-bottom: 2rem;
  line-height: 1.6;
}

.error-actions {
  margin-top: 2rem;
}

.suggestions h4 {
  color: #495057;
  font-weight: 600;
  margin-bottom: 2rem;
}

.suggestion-card {
  background: white;
  border-radius: 15px;
  padding: 2rem 1.5rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.suggestion-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.suggestion-card h5 {
  color: #495057;
  font-weight: 600;
  margin-bottom: 1rem;
}

.suggestion-card p {
  color: #6c757d;
  font-size: 0.9rem;
  line-height: 1.5;
  margin-bottom: 1.5rem;
  flex: 1;
}

.btn-warning {
  background: linear-gradient(135deg, #f6d55c 0%, #e8ca0f 100%);
  border: none;
  font-weight: 600;
  padding: 10px 24px;
  border-radius: 10px;
}

.btn-warning:hover {
  background: linear-gradient(135deg, #e8ca0f 0%, #d4af37 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(232, 202, 15, 0.3);
}

.btn-outline-secondary {
  border: 2px solid #6c757d;
  color: #6c757d;
  font-weight: 500;
  padding: 10px 24px;
  border-radius: 10px;
  transition: all 0.2s ease;
}

.btn-outline-secondary:hover {
  background: #6c757d;
  color: white;
  transform: translateY(-1px);
}

.btn-outline-warning {
  border: 2px solid #f6d55c;
  color: #f6d55c;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.btn-outline-warning:hover {
  background: #f6d55c;
  color: white;
}

@media (max-width: 768px) {
  .error-number {
    font-size: 6rem;
  }
  
  .error-title {
    font-size: 2rem;
  }
  
  .error-description {
    font-size: 1rem;
  }
  
  .error-actions {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .error-actions .btn {
    margin: 0;
  }
  
  .suggestion-card {
    padding: 1.5rem 1rem;
  }
}

@media (max-width: 576px) {
  .error-number {
    font-size: 4rem;
  }
  
  .error-title {
    font-size: 1.5rem;
  }
  
  .not-found-container {
    padding: 2rem 0;
  }
}
</style>
