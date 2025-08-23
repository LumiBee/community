# API使用指南

本指南详细说明如何在Vue前端应用中与Java后端进行通信。

## 项目结构

```
src/
├── api/
│   ├── config.js      # axios配置和拦截器
│   ├── auth.js        # 认证相关API
│   ├── article.js     # 文章相关API
│   ├── user.js        # 用户相关API
│   ├── portfolio.js   # 作品集相关API
│   ├── tag.js         # 标签相关API
│   ├── favorite.js    # 收藏相关API
│   └── index.js       # 统一导出
└── examples/
    └── ApiUsageExample.vue  # 使用示例
```

## 基础配置

### 1. axios配置 (src/api/config.js)

已配置的功能：
- 请求/响应拦截器
- 自动添加认证token
- CSRF保护支持
- 统一错误处理
- 请求日志记录

### 2. Vite代理配置 (vite.config.js)

已配置的代理端点：
- `/api/*` - API接口
- `/login`, `/signup`, `/logout` - 认证端点
- `/oauth2/*` - OAuth2认证
- `/uploads/*` - 文件上传
- `/article/*`, `/search` - 内容页面

### 3. 环境变量配置

创建以下文件来配置不同环境：

#### .env.development (开发环境)
```bash
VITE_API_URL=http://localhost:3000
VITE_BACKEND_URL=http://localhost:8090
VITE_APP_TITLE=Hive - 开发环境
VITE_DEBUG=true
```

#### .env.production (生产环境)
```bash
VITE_API_URL=/api
VITE_BACKEND_URL=https://your-backend-domain.com
VITE_APP_TITLE=Hive Blog Platform
VITE_DEBUG=false
```

## API使用方式

### 方式1：按需导入
```javascript
import { authAPI, articleAPI, userAPI } from '@/api'

// 使用
const result = await authAPI.login(loginData)
const articles = await articleAPI.getHomeArticles()
```

### 方式2：统一导入
```javascript
import api from '@/api'

// 使用
const result = await api.auth.login(loginData)
const articles = await api.article.getHomeArticles()
```

### 方式3：直接使用request实例
```javascript
import { request } from '@/api'

// 自定义请求
const result = await request({
  url: '/api/custom-endpoint',
  method: 'get'
})
```

## 主要API接口

### 认证API (authAPI)

| 方法 | 描述 | 参数 |
|------|------|------|
| `login(loginData)` | 用户登录 | `{username, password}` |
| `register(signupData)` | 用户注册 | `{username, email, password, confirmPassword}` |
| `logout()` | 用户登出 | 无 |
| `getCurrentUser()` | 获取当前用户信息 | 无 |
| `checkAuth()` | 检查认证状态 | 无 |

### 文章API (articleAPI)

| 方法 | 描述 | 参数 |
|------|------|------|
| `getHomeArticles(page, size)` | 获取首页文章列表 | 页码, 页大小 |
| `getArticleBySlug(slug)` | 根据slug获取文章 | 文章slug |
| `searchArticles(query)` | 搜索文章 | 搜索关键词 |
| `toggleLike(articleId)` | 切换点赞状态 | 文章ID |
| `publishArticle(articleData)` | 发布文章 | 文章数据 |
| `saveDraft(draftData)` | 保存草稿 | 草稿数据 |

### 用户API (userAPI)

| 方法 | 描述 | 参数 |
|------|------|------|
| `getProfile(page, size)` | 获取用户资料 | 页码, 页大小 |
| `updateProfile(userData)` | 更新用户资料 | 用户数据 |
| `uploadAvatar(formData)` | 上传头像 | FormData |
| `toggleFollow(userId)` | 关注/取消关注 | 用户ID |
| `changePassword(passwordData)` | 修改密码 | 密码数据 |

## 错误处理

### 统一错误处理
所有API请求都会通过响应拦截器进行统一错误处理：

```javascript
// 在axios响应拦截器中处理
switch (status) {
  case 400:
    console.error('请求参数错误')
    break
  case 401:
    // 自动清除token并重定向到登录页
    localStorage.removeItem('token')
    break
  case 403:
    console.error('权限不足')
    break
  case 500:
    console.error('服务器错误')
    break
}
```

### 在组件中处理错误
```javascript
try {
  const result = await authAPI.login(loginData)
  // 处理成功逻辑
} catch (error) {
  // error对象包含 status, message, data 三个属性
  console.error('错误:', error.message)
  
  // 根据错误状态码进行不同处理
  if (error.status === 401) {
    // 跳转到登录页
    router.push('/login')
  } else if (error.status === 400) {
    // 显示表单验证错误
    showFormErrors(error.data)
  }
}
```

## 认证机制

### 基于Session的认证（当前方式）
- 使用Spring Security的默认session机制
- 通过`withCredentials: true`携带session cookie
- 登录后session自动维护

### JWT Token认证（可选）
如果后端切换到JWT认证：
1. 登录成功后保存token到localStorage
2. 请求拦截器自动添加Authorization header
3. token过期后自动清除并重定向

## 文件上传

### 头像上传示例
```javascript
const uploadAvatar = async (file) => {
  const formData = new FormData()
  formData.append('avatar', file)
  
  try {
    const result = await userAPI.uploadAvatar(formData)
    console.log('上传成功:', result)
  } catch (error) {
    console.error('上传失败:', error.message)
  }
}
```

### 文章图片上传
```javascript
const uploadArticleImage = async (file) => {
  const formData = new FormData()
  formData.append('image', file)
  
  try {
    const result = await request({
      url: '/api/upload/image',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    return result.url
  } catch (error) {
    console.error('图片上传失败:', error.message)
  }
}
```

## 分页处理

### 标准分页请求
```javascript
const fetchArticles = async (page = 1, size = 10) => {
  try {
    const result = await articleAPI.getHomeArticles(page, size)
    
    // 后端返回的分页数据结构
    const {
      records,    // 当前页数据
      current,    // 当前页码
      size,       // 页大小
      total,      // 总记录数
      pages       // 总页数
    } = result
    
    return result
  } catch (error) {
    console.error('获取文章失败:', error.message)
  }
}
```

## 调试技巧

### 1. 开发环境调试
- 启用代理服务: `npm run dev`
- 查看Network面板监控请求
- 检查Console面板的代理日志

### 2. 请求日志
请求拦截器会自动记录：
- 发送的请求: `Sending Request to the Target: METHOD URL`
- 接收的响应: `Received Response from the Target: STATUS URL`

### 3. 错误排查
1. 检查后端是否启动 (http://localhost:8090)
2. 检查CORS配置是否正确
3. 验证API端点是否存在
4. 确认请求参数格式是否正确

## 性能优化

### 1. 请求缓存
对于不常变化的数据（如标签列表），可以实现简单缓存：

```javascript
let tagsCache = null
let tagsCacheTime = 0

export const getCachedTags = async () => {
  const now = Date.now()
  if (tagsCache && (now - tagsCacheTime < 5 * 60 * 1000)) {
    return tagsCache
  }
  
  tagsCache = await tagAPI.getAllTags()
  tagsCacheTime = now
  return tagsCache
}
```

### 2. 请求去重
防止重复请求：

```javascript
const pendingRequests = new Map()

const requestWithDedup = async (config) => {
  const key = `${config.method}:${config.url}`
  
  if (pendingRequests.has(key)) {
    return pendingRequests.get(key)
  }
  
  const promise = request(config)
  pendingRequests.set(key, promise)
  
  try {
    const result = await promise
    return result
  } finally {
    pendingRequests.delete(key)
  }
}
```

## 部署注意事项

### 开发环境
- 前端: http://localhost:3000
- 后端: http://localhost:8090
- 使用Vite代理处理跨域

### 生产环境
- 确保nginx配置正确代理API请求
- 更新环境变量为生产环境地址
- 启用HTTPS
- 配置正确的CORS策略

### nginx配置示例
```nginx
server {
    listen 80;
    server_name yourdomain.com;
    
    # 静态文件
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    # API代理
    location /api/ {
        proxy_pass http://localhost:8090/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

## 示例组件

参考 `src/examples/ApiUsageExample.vue` 文件，其中包含了各种API的完整使用示例。

## 常见问题

### Q: 为什么请求返回401错误？
A: 通常是因为用户未登录或session过期，请检查登录状态。

### Q: 如何处理文件上传进度？
A: 可以在axios配置中添加onUploadProgress回调。

### Q: 如何取消正在进行的请求？
A: 使用AbortController或axios的CancelToken。

### Q: 如何处理并发请求？
A: 使用Promise.all()或手动管理请求队列。

更多详细示例请查看 `src/examples/ApiUsageExample.vue` 文件。
