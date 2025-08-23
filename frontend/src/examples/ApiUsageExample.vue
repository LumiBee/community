<template>
  <div class="api-usage-example">
    <h2>API使用示例</h2>
    
    <!-- 认证示例 -->
    <div class="example-section">
      <h3>1. 用户认证</h3>
      <div class="example-code">
        <h4>登录示例：</h4>
        <button @click="handleLogin" class="btn btn-primary">测试登录</button>
        <pre><code>{{ loginCode }}</code></pre>
      </div>
      
      <div class="example-code">
        <h4>注册示例：</h4>
        <button @click="handleRegister" class="btn btn-secondary">测试注册</button>
        <pre><code>{{ registerCode }}</code></pre>
      </div>
    </div>

    <!-- 文章API示例 -->
    <div class="example-section">
      <h3>2. 文章相关API</h3>
      <div class="example-code">
        <h4>获取文章列表：</h4>
        <button @click="fetchArticles" class="btn btn-info">获取文章</button>
        <pre><code>{{ articleCode }}</code></pre>
        <div v-if="articles.length > 0" class="result">
          <h5>结果：</h5>
          <ul>
            <li v-for="article in articles" :key="article.id">
              {{ article.title }}
            </li>
          </ul>
        </div>
      </div>

      <div class="example-code">
        <h4>文章点赞：</h4>
        <button @click="toggleLike" class="btn btn-warning">切换点赞</button>
        <pre><code>{{ likeCode }}</code></pre>
      </div>
    </div>

    <!-- 用户API示例 -->
    <div class="example-section">
      <h3>3. 用户相关API</h3>
      <div class="example-code">
        <h4>获取用户信息：</h4>
        <button @click="fetchUserProfile" class="btn btn-success">获取用户资料</button>
        <pre><code>{{ userCode }}</code></pre>
      </div>
    </div>

    <!-- 错误处理示例 -->
    <div class="example-section">
      <h3>4. 错误处理示例</h3>
      <div class="example-code">
        <h4>统一错误处理：</h4>
        <pre><code>{{ errorHandlingCode }}</code></pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { authAPI, articleAPI, userAPI } from '@/api'

// 响应式数据
const articles = ref([])

// 代码示例
const loginCode = `
// 用户登录
import { authAPI } from '@/api'

const handleLogin = async () => {
  try {
    const result = await authAPI.login({
      username: 'your-username',
      password: 'your-password'
    })
    console.log('登录成功：', result)
    // 处理登录成功逻辑
  } catch (error) {
    console.error('登录失败：', error.message)
    // 处理登录失败逻辑
  }
}
`

const registerCode = `
// 用户注册
import { authAPI } from '@/api'

const handleRegister = async () => {
  try {
    const result = await authAPI.register({
      username: 'new-username',
      email: 'user@example.com',
      password: 'password123',
      confirmPassword: 'password123'
    })
    console.log('注册成功：', result)
  } catch (error) {
    console.error('注册失败：', error.message)
  }
}
`

const articleCode = `
// 获取文章列表
import { articleAPI } from '@/api'

const fetchArticles = async () => {
  try {
    const result = await articleAPI.getHomeArticles(1, 10)
    articles.value = result.records || result
    console.log('文章列表：', result)
  } catch (error) {
    console.error('获取文章失败：', error.message)
  }
}
`

const likeCode = `
// 文章点赞/取消点赞
import { articleAPI } from '@/api'

const toggleLike = async (articleId) => {
  try {
    const result = await articleAPI.toggleLike(articleId)
    console.log('点赞操作结果：', result)
    // 更新UI状态
  } catch (error) {
    if (error.status === 401) {
      console.log('请先登录')
      // 重定向到登录页
    } else {
      console.error('点赞操作失败：', error.message)
    }
  }
}
`

const userCode = `
// 获取用户资料
import { userAPI } from '@/api'

const fetchUserProfile = async () => {
  try {
    const result = await userAPI.getProfile()
    console.log('用户资料：', result)
  } catch (error) {
    console.error('获取用户资料失败：', error.message)
  }
}
`

const errorHandlingCode = `
// 统一错误处理工具函数
export const handleApiError = (error) => {
  switch (error.status) {
    case 400:
      return '请求参数错误'
    case 401:
      // 清除本地token
      localStorage.removeItem('token')
      // 重定向到登录页
      router.push('/login')
      return '请重新登录'
    case 403:
      return '权限不足'
    case 404:
      return '请求的资源不存在'
    case 500:
      return '服务器错误，请稍后重试'
    default:
      return error.message || '未知错误'
  }
}

// 在组件中使用
try {
  const result = await someAPI()
} catch (error) {
  const errorMessage = handleApiError(error)
  // 显示错误消息给用户
  alert(errorMessage)
}
`

// 示例方法实现
const handleLogin = async () => {
  try {
    console.log('这是登录示例，实际使用时请提供真实的用户名和密码')
    // 实际实现中需要获取表单数据
    // const result = await authAPI.login({ username: '...', password: '...' })
  } catch (error) {
    console.error('登录示例错误：', error)
  }
}

const handleRegister = async () => {
  try {
    console.log('这是注册示例，实际使用时请提供真实的注册数据')
    // 实际实现中需要获取表单数据
    // const result = await authAPI.register({ ... })
  } catch (error) {
    console.error('注册示例错误：', error)
  }
}

const fetchArticles = async () => {
  try {
    const result = await articleAPI.getHomeArticles(1, 5)
    articles.value = result.records || result || []
    console.log('获取到的文章：', articles.value)
  } catch (error) {
    console.error('获取文章失败：', error)
  }
}

const toggleLike = async () => {
  try {
    console.log('这是点赞示例，实际使用时需要提供文章ID')
    // const result = await articleAPI.toggleLike(articleId)
  } catch (error) {
    console.error('点赞示例错误：', error)
  }
}

const fetchUserProfile = async () => {
  try {
    const result = await userAPI.getProfile()
    console.log('用户资料：', result)
  } catch (error) {
    console.error('获取用户资料失败：', error)
  }
}
</script>

<style scoped>
.api-usage-example {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.example-section {
  margin-bottom: 40px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
}

.example-code {
  margin-bottom: 20px;
}

.example-code h4 {
  color: #333;
  margin-bottom: 10px;
}

.example-code pre {
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 15px;
  overflow-x: auto;
  font-size: 14px;
  line-height: 1.4;
}

.example-code code {
  font-family: 'Monaco', 'Consolas', 'Courier New', monospace;
}

.result {
  margin-top: 15px;
  padding: 10px;
  background-color: #e8f5e8;
  border-radius: 4px;
}

.result h5 {
  margin: 0 0 10px 0;
  color: #28a745;
}

.result ul {
  margin: 0;
  padding-left: 20px;
}

button {
  margin-bottom: 10px;
}
</style>
