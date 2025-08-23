<template>
  <div class="auth-page-wrapper">
    <div class="auth-container">
      <div class="auth-form-section">
        <div class="logo">
          <img src="/img/logo.png" alt="Hive Logo" />
        </div>

        <!-- 成功提示 -->
        <div v-if="showSuccessMessage" class="alert alert-success" role="alert">
          <i class="fas fa-check-circle me-2"></i>
          {{ successMessage }}
        </div>

        <!-- 错误提示 -->
        <div v-if="errorMessage" class="alert alert-danger" role="alert">
          <i class="fas fa-exclamation-circle me-2"></i>
          {{ errorMessage }}
        </div>

        <!-- 登录表单 -->
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="accountInput">账号 (邮箱或用户名)</label>
            <div class="input-group">
              <div class="input-group-text">
                <i class="fas fa-user"></i>
              </div>
              <input
                type="text"
                class="form-control"
                id="accountInput"
                v-model="loginForm.account"
                placeholder="请输入您的邮箱或用户名"
                required
                :class="{ 'is-invalid': errorMessage }"
              />
            </div>
          </div>

          <div class="form-group">
            <label for="password">密码</label>
            <div class="input-group">
              <div class="input-group-text">
                <i class="fas fa-lock"></i>
              </div>
              <input
                :type="showPassword ? 'text' : 'password'"
                class="form-control"
                id="password"
                v-model="loginForm.password"
                placeholder="请输入您的密码"
                required
                :class="{ 'is-invalid': errorMessage }"
              />
              <button
                class="btn"
                type="button"
                @click="showPassword = !showPassword"
                style="border: 1px solid #ced4da; border-left: none;"
              >
                <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
              </button>
            </div>
            <div v-if="errorMessage" class="invalid-feedback d-block">
              邮箱/用户名或密码不正确。
            </div>
          </div>

          <div class="form-check d-flex justify-content-end mb-3">
            <input
              type="checkbox"
              class="form-check-input"
              id="remember-me"
              v-model="loginForm.rememberMe"
            />
            <label class="form-check-label" for="remember-me">记住我</label>
          </div>

          <button type="submit" class="btn btn-warning w-100" :disabled="isLoading">
            <span v-if="isLoading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            <i v-else class="fas fa-sign-in-alt me-1"></i>
            {{ isLoading ? '登录中...' : '立即登录' }}
          </button>
        </form>

        <!-- 跳转注册页面 -->
        <div class="auth-links">
          <p>还没有账户？ <router-link to="/signup">立即注册</router-link></p>
        </div>

        <!-- OAuth2登录 -->
        <div class="social-login">
          <p>或通过以下方式登录</p>
          <div>
            <a href="/oauth2/authorization/github" class="btn btn-outline-dark me-2">
              <i class="fab fa-github me-1"></i> Github
            </a>
            <button type="button" class="btn btn-outline-danger" disabled>
              <i class="fab fa-qq me-1"></i> QQ (即将开放)
            </button>
          </div>
        </div>
      </div>

      <!-- 欢迎词 -->
      <div class="auth-info-section">
        <h2>欢迎来到 LumiHive!</h2>
        <p>
          LumiHive 是一个个人知识学习与交流平台，专注于各种技术分享与学习心得。
        </p>
        <p>
          我们正在构建一个小众社区，大家在这里相互启发、以平等、自由、开放的精神进行分享交流。最终，每位大家庭成员找到与自己志同道合的伙伴，共同成长。
        </p>
        <p>
          最后请大家共同爱护这个交流环境，一个好的交流环境离不开大家的努力😊
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 响应式数据
const loginForm = ref({
  account: '',
  password: '',
  rememberMe: false
})

const showPassword = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')
const showSuccessMessage = ref(false)
const successMessage = ref('')

// 处理登录
const handleLogin = async () => {
  if (!loginForm.value.account || !loginForm.value.password) {
    errorMessage.value = '请填写账号和密码'
    return
  }

  try {
    isLoading.value = true
    errorMessage.value = ''

    const success = await authStore.login(loginForm.value)

    if (success) {
      // 登录成功，跳转到目标页面或首页
      const redirectTo = route.query.redirect || '/'
      router.push(redirectTo)
    } else {
      errorMessage.value = authStore.error || '登录失败，请检查用户名和密码'
    }
  } catch (error) {
    console.error('登录失败:', error)
    errorMessage.value = '登录失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

// 组件挂载时检查URL参数
onMounted(() => {
  // 检查是否是注册成功后跳转过来的
  if (route.query.registered === 'true') {
    showSuccessMessage.value = true
    successMessage.value = '注册成功！现在您可以使用新账户登录了。'
  }
  
  // 检查是否有登录错误参数
  if (route.query.error) {
    errorMessage.value = '登录失败，请检查用户名和密码'
  }
})
</script>

<style scoped>
.auth-page-wrapper {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 0;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.auth-container {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: flex;
  max-width: 900px;
  width: 100%;
  margin: 0 1rem;
}

.auth-form-section {
  flex: 1;
  padding: 3rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth-info-section {
  flex: 1;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 3rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #495057;
  position: relative;
  overflow: hidden;
}

.logo {
  text-align: center;
  margin-bottom: 2rem;
}

.logo img {
  height: 60px;
  width: auto;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  font-weight: 600;
  color: #495057;
  margin-bottom: 0.5rem;
  display: block;
}

.input-group {
  border-radius: 10px;
  overflow: hidden;
  border: 2px solid #e9ecef;
  transition: all 0.2s ease;
}

.input-group:focus-within {
  border-color: #f6d55c;
  box-shadow: 0 0 0 0.2rem rgba(246, 213, 92, 0.25);
}

.input-group-text {
  background: #f8f9fa;
  border: none;
  color: #6c757d;
}

.form-control {
  border: none;
  box-shadow: none;
}

.form-control:focus {
  border: none;
  box-shadow: none;
}

.form-control.is-invalid {
  border: none;
}

.btn-warning {
  background: linear-gradient(135deg, #f6d55c 0%, #e8ca0f 100%);
  border: none;
  padding: 12px;
  font-weight: 600;
  border-radius: 10px;
  transition: all 0.2s ease;
}

.btn-warning:hover:not(:disabled) {
  background: linear-gradient(135deg, #e8ca0f 0%, #d4af37 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(232, 202, 15, 0.3);
}

.btn-warning:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.auth-links {
  text-align: center;
  margin: 1.5rem 0;
}

.auth-links a {
  color: #f6d55c;
  text-decoration: none;
  font-weight: 600;
}

.auth-links a:hover {
  color: #e8ca0f;
  text-decoration: underline;
}

.social-login {
  text-align: center;
  margin-top: 1rem;
}

.social-login p {
  color: #6c757d;
  margin-bottom: 1rem;
  position: relative;
}

.social-login p::before,
.social-login p::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 35%;
  height: 1px;
  background: #dee2e6;
}

.social-login p::before {
  left: 0;
}

.social-login p::after {
  right: 0;
}

.btn-outline-dark,
.btn-outline-danger {
  border-radius: 10px;
  padding: 8px 16px;
  font-weight: 500;
}

.auth-info-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: #ffda58;
}

.auth-info-section h2 {
  color: #333;
  font-weight: 600;
  margin-bottom: 1.5rem;
  position: relative;
  display: inline-block;
  font-size: 2rem;
  font-family: 'Playfair Display', Georgia, serif;
}

.auth-info-section h2::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 60px;
  height: 4px;
  background: #ffda58;
  border-radius: 2px;
}

.auth-info-section p {
  line-height: 1.7;
  margin-bottom: 1rem;
  opacity: 0.95;
}

.alert {
  border-radius: 10px;
  border: none;
  padding: 12px 16px;
  margin-bottom: 1.5rem;
}

.alert-success {
  background: rgba(40, 167, 69, 0.1);
  color: #155724;
}

.alert-danger {
  background: rgba(220, 53, 69, 0.1);
  color: #721c24;
}

@media (max-width: 768px) {
  .auth-container {
    flex-direction: column;
    margin: 0 1rem;
  }
  
  .auth-form-section,
  .auth-info-section {
    padding: 2rem;
  }
  
  .auth-info-section {
    order: -1;
  }
  
  .auth-info-section h2 {
    font-size: 1.5rem;
  }
}

@media (max-width: 576px) {
  .auth-form-section,
  .auth-info-section {
    padding: 1.5rem;
  }
  
  .social-login div {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .btn-outline-dark,
  .btn-outline-danger {
    margin: 0;
  }
}
</style>
