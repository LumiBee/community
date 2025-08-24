<template>
  <div class="auth-page-wrapper">
    <div class="auth-container">
      <div class="auth-form-section">
        <div class="logo">
          <img src="/img/logo.png" alt="Hive Logo" />
        </div>

        <!-- 错误提示 -->
        <div v-if="errorMessage" class="alert alert-danger" role="alert">
          <i class="fas fa-exclamation-circle me-2"></i>
          {{ errorMessage }}
        </div>

        <!-- 注册表单 -->
        <form @submit.prevent="handleSignup">
          <div class="form-group">
            <label for="username">用户名 *</label>
            <div class="input-group">
              <div class="input-group-text">
                <i class="fas fa-user"></i>
              </div>
              <input
                type="text"
                class="form-control"
                id="username"
                v-model="signupForm.username"
                placeholder="请输入用户名"
                required
                :class="{ 'is-invalid': fieldErrors.username }"
              />
            </div>
            <div v-if="fieldErrors.username" class="invalid-feedback d-block">
              {{ fieldErrors.username }}
            </div>
          </div>

          <div class="form-group">
            <label for="email">邮箱 *</label>
            <div class="input-group">
              <div class="input-group-text">
                <i class="fas fa-envelope"></i>
              </div>
              <input
                type="email"
                class="form-control"
                id="email"
                v-model="signupForm.email"
                placeholder="请输入邮箱地址"
                required
                :class="{ 'is-invalid': fieldErrors.email }"
              />
            </div>
            <div v-if="fieldErrors.email" class="invalid-feedback d-block">
              {{ fieldErrors.email }}
            </div>
          </div>

          <div class="form-group">
            <label for="password">密码 *</label>
            <div class="input-group">
              <div class="input-group-text">
                <i class="fas fa-lock"></i>
              </div>
              <input
                :type="showPassword ? 'text' : 'password'"
                class="form-control"
                id="password"
                v-model="signupForm.password"
                placeholder="请输入密码"
                required
                :class="{ 'is-invalid': fieldErrors.password }"
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
            <div v-if="fieldErrors.password" class="invalid-feedback d-block">
              {{ fieldErrors.password }}
            </div>
            <small class="form-text text-muted">
              密码长度至少6位，建议包含字母和数字
            </small>
          </div>

          <div class="form-group">
            <label for="confirmPassword">确认密码 *</label>
            <div class="input-group">
              <div class="input-group-text">
                <i class="fas fa-lock"></i>
              </div>
              <input
                :type="showConfirmPassword ? 'text' : 'password'"
                class="form-control"
                id="confirmPassword"
                v-model="signupForm.confirmPassword"
                placeholder="请再次输入密码"
                required
                :class="{ 'is-invalid': fieldErrors.confirmPassword }"
              />
              <button
                class="btn"
                type="button"
                @click="showConfirmPassword = !showConfirmPassword"
                style="border: 1px solid #ced4da; border-left: none;"
              >
                <i :class="showConfirmPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
              </button>
            </div>
            <div v-if="fieldErrors.confirmPassword" class="invalid-feedback d-block">
              {{ fieldErrors.confirmPassword }}
            </div>
          </div>

          <div class="form-check mb-3">
            <input
              type="checkbox"
              class="form-check-input"
              id="agreeTerms"
              v-model="signupForm.agreeTerms"
              required
            />
            <label class="form-check-label" for="agreeTerms">
              我已阅读并同意 <a href="#" class="text-primary">用户协议</a> 和 <a href="#" class="text-primary">隐私政策</a>
            </label>
          </div>

          <button type="submit" class="btn btn-warning w-100" :disabled="isLoading || !isFormValid">
            <span v-if="isLoading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            <i v-else class="fas fa-user-plus me-1"></i>
            {{ isLoading ? '注册中...' : '立即注册' }}
          </button>
        </form>

        <!-- 跳转登录页面 -->
        <div class="auth-links">
          <p>已有账户？ <router-link to="/login">立即登录</router-link></p>
        </div>
      </div>

      <!-- 欢迎词 -->
      <div class="auth-info-section">
        <h2>加入 LumiHive!</h2>
        <p>
          创建您的账户，开始分享您的知识和见解。
        </p>
        <div class="feature-list">
          <div class="feature-item">
            <i class="fas fa-pencil-alt"></i>
            <span>发布原创文章</span>
          </div>
          <div class="feature-item">
            <i class="fas fa-users"></i>
            <span>加入技术社区</span>
          </div>
          <div class="feature-item">
            <i class="fas fa-star"></i>
            <span>收藏精彩内容</span>
          </div>
          <div class="feature-item">
            <i class="fas fa-comments"></i>
            <span>交流学习心得</span>
          </div>
        </div>
        <p class="mt-3">
          免费注册，立即开始您的知识分享之旅！
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const signupForm = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreeTerms: false
})

const showPassword = ref(false)
const showConfirmPassword = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')
const fieldErrors = ref({})

// 计算属性
const isFormValid = computed(() => {
  return signupForm.value.username &&
         signupForm.value.email &&
         signupForm.value.password &&
         signupForm.value.confirmPassword &&
         signupForm.value.agreeTerms &&
         signupForm.value.password === signupForm.value.confirmPassword &&
         signupForm.value.password.length >= 6
})

// 表单验证
const validateForm = () => {
  fieldErrors.value = {}
  let isValid = true

  // 验证用户名
  if (!signupForm.value.username) {
    fieldErrors.value.username = '请输入用户名'
    isValid = false
  } else if (signupForm.value.username.length < 3) {
    fieldErrors.value.username = '用户名至少需要3个字符'
    isValid = false
  } else if (signupForm.value.username.length > 20) {
    fieldErrors.value.username = '用户名不能超过20个字符'
    isValid = false
  }

  // 验证邮箱
  if (!signupForm.value.email) {
    fieldErrors.value.email = '请输入邮箱地址'
    isValid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(signupForm.value.email)) {
    fieldErrors.value.email = '请输入有效的邮箱地址'
    isValid = false
  }

  // 验证密码
  if (!signupForm.value.password) {
    fieldErrors.value.password = '请输入密码'
    isValid = false
  } else if (signupForm.value.password.length < 6) {
    fieldErrors.value.password = '密码长度至少需要6位'
    isValid = false
  } else if (signupForm.value.password.length > 50) {
    fieldErrors.value.password = '密码长度不能超过50位'
    isValid = false
  }

  // 验证确认密码
  if (!signupForm.value.confirmPassword) {
    fieldErrors.value.confirmPassword = '请确认密码'
    isValid = false
  } else if (signupForm.value.password !== signupForm.value.confirmPassword) {
    fieldErrors.value.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  return isValid
}

// 处理注册
const handleSignup = async () => {
  if (!validateForm()) {
    return
  }

  try {
    isLoading.value = true
    errorMessage.value = ''
    fieldErrors.value = {}

    console.log('提交注册表单:', {
      username: signupForm.value.username,
      email: signupForm.value.email,
      password: signupForm.value.password.length,
      confirmPassword: signupForm.value.confirmPassword.length
    })

    const result = await authStore.register({
      username: signupForm.value.username,
      email: signupForm.value.email,
      password: signupForm.value.password,
      confirmPassword: signupForm.value.confirmPassword
    })

    // 检查结果类型
    if (result === true) {
      // 注册成功，跳转到登录页面
      router.push({ name: 'Login', query: { registered: 'true' } })
    } else if (result && typeof result === 'object' && result.fieldErrors) {
      // 处理字段级别的错误
      console.log('注册表单字段错误:', result.fieldErrors)
      fieldErrors.value = result.fieldErrors
    } else {
      // 处理一般错误
      const error = authStore.error
      console.error('注册失败，错误信息:', error)
      
      // 尝试从错误消息中识别特定字段错误
      if (error && error.toLowerCase().includes('用户名')) {
        fieldErrors.value.username = '该用户名已被注册'
      } else if (error && error.toLowerCase().includes('邮箱')) {
        fieldErrors.value.email = '该邮箱已被注册'
      } else {
        errorMessage.value = error || '注册失败，请稍后重试'
      }
    }
  } catch (error) {
    console.error('注册过程发生异常:', error)
    errorMessage.value = '注册失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

// 监听密码变化，实时验证确认密码
watch(() => signupForm.value.password, () => {
  if (signupForm.value.confirmPassword && fieldErrors.value.confirmPassword) {
    if (signupForm.value.password === signupForm.value.confirmPassword) {
      delete fieldErrors.value.confirmPassword
    }
  }
})

watch(() => signupForm.value.confirmPassword, () => {
  if (signupForm.value.password && signupForm.value.confirmPassword) {
    if (signupForm.value.password !== signupForm.value.confirmPassword) {
      fieldErrors.value.confirmPassword = '两次输入的密码不一致'
    } else {
      delete fieldErrors.value.confirmPassword
    }
  }
})
</script>

<style scoped>
.auth-page-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  padding: 2rem 0;
}

.auth-container {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: flex;
  max-width: 1000px;
  width: 100%;
  margin: 0 1rem;
}

.auth-form-section {
  flex: 1.2;
  padding: 3rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth-info-section {
  flex: 0.8;
  background: linear-gradient(135deg, #f6d55c 0%, #e8ca0f 100%);
  color: white;
  padding: 3rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
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

.input-group.is-invalid,
.input-group:has(.is-invalid) {
  border-color: #dc3545;
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
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.auth-links {
  text-align: center;
  margin: 1.5rem 0 0 0;
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

.auth-info-section h2 {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
}

.feature-list {
  margin: 2rem 0;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
  opacity: 0.95;
}

.feature-item i {
  width: 24px;
  margin-right: 12px;
  font-size: 1.1rem;
}

.feature-item span {
  font-weight: 500;
}

.alert {
  border-radius: 10px;
  border: none;
  padding: 12px 16px;
  margin-bottom: 1.5rem;
}

.alert-danger {
  background: rgba(220, 53, 69, 0.1);
  color: #721c24;
}

.form-text {
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.form-check {
  margin-bottom: 1.5rem;
}

.form-check-label {
  font-size: 0.9rem;
  line-height: 1.4;
}

.text-primary {
  color: #f6d55c !important;
}

.text-primary:hover {
  color: #e8ca0f !important;
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
  
  .feature-item {
    font-size: 0.9rem;
  }
}
</style>
