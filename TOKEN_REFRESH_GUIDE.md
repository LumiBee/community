# Token自动刷新功能说明

## 概述

本系统实现了JWT token的自动刷新机制，确保用户在长时间使用过程中不会因为token过期而需要重新登录。

## 功能特性

### 1. 自动刷新机制
- **刷新阈值**: 当token剩余时间少于2小时时自动刷新
- **刷新频率**: 每30分钟检查一次token状态
- **无缝体验**: 用户无需感知token刷新过程

### 2. 手动刷新
- 提供手动刷新token的API接口
- 支持在token即将过期时主动刷新

### 3. 错误处理
- 自动处理401认证失败
- 在API调用失败时自动尝试刷新token
- 刷新失败时优雅降级

## 技术实现

### 后端实现

#### 1. Token刷新端点
```java
@PostMapping("/api/auth/refresh")
public ResponseEntity<Map<String, Object>> refreshToken(@RequestHeader("Authorization") String authHeader)
```

#### 2. 刷新逻辑
- 验证当前token的有效性
- 检查token是否需要刷新（剩余时间 < 2小时）
- 生成新的token并返回

#### 3. 配置参数
```java
// JWT过期时间 - 24小时
private static final long JWT_EXPIRATION = 86400000;
// JWT刷新阈值 - 当token剩余时间少于2小时时自动刷新
private static final long JWT_REFRESH_THRESHOLD = 7200000; // 2小时
```

### 前端实现

#### 1. 自动刷新定时器
```javascript
// 每30分钟检查一次token是否需要刷新
tokenRefreshTimer = setInterval(async () => {
  if (user.value?.token) {
    try {
      await refreshToken()
    } catch (error) {
      console.error('自动刷新token失败:', error)
    }
  }
}, 30 * 60 * 1000) // 30分钟
```

#### 2. 响应拦截器
```javascript
// 在API调用返回401时自动尝试刷新token
if (error.status === 401 && !error.config.url.includes('/auth/refresh')) {
  const refreshSuccess = await authStore.refreshToken()
  if (refreshSuccess) {
    // 重试原请求
    return axios(originalRequest)
  }
}
```

#### 3. Auth Store集成
- 登录成功后自动启动token刷新定时器
- 登出时自动停止定时器
- 提供手动刷新token的方法

## 使用方法

### 1. 自动刷新
用户登录后，系统会自动启动token刷新机制，无需任何额外操作。

### 2. 手动刷新
```javascript
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
const success = await authStore.refreshToken()
```

### 3. 测试功能
访问 `/token-test` 页面可以测试token刷新功能：
- 查看当前token状态
- 手动刷新token
- 测试API调用
- 查看刷新日志

## 安全考虑

### 1. Token验证
- 每次刷新前都会验证当前token的有效性
- 只有有效的token才能进行刷新

### 2. 刷新阈值
- 设置合理的刷新阈值（2小时），避免频繁刷新
- 只有在token即将过期时才进行刷新

### 3. 错误处理
- 刷新失败时不会影响用户体验
- 提供降级机制，确保系统稳定性

## 配置说明

### 后端配置
可以在 `LoginController.java` 中修改以下参数：
```java
// 修改JWT过期时间（毫秒）
private static final long JWT_EXPIRATION = 86400000; // 24小时

// 修改刷新阈值（毫秒）
private static final long JWT_REFRESH_THRESHOLD = 7200000; // 2小时
```

### 前端配置
可以在 `auth.js` store中修改刷新频率：
```javascript
// 修改检查频率（毫秒）
tokenRefreshTimer = setInterval(async () => {
  // 刷新逻辑
}, 30 * 60 * 1000) // 30分钟
```

## 故障排除

### 1. Token刷新失败
- 检查后端服务是否正常运行
- 查看浏览器控制台的错误日志
- 确认token格式是否正确

### 2. 自动刷新不工作
- 检查用户是否已登录
- 查看定时器是否正确启动
- 确认localStorage中的用户信息是否完整

### 3. API调用仍然返回401
- 检查响应拦截器是否正确配置
- 确认token刷新逻辑是否执行
- 查看网络请求的Authorization头是否正确

## 最佳实践

1. **合理设置过期时间**: 根据应用安全需求设置合适的token过期时间
2. **监控刷新频率**: 定期检查token刷新日志，确保机制正常工作
3. **用户通知**: 在token刷新失败时，可以通知用户重新登录
4. **安全审计**: 定期审查token刷新日志，发现异常行为

## 更新日志

- **v1.0.0**: 初始实现token自动刷新功能
- 支持自动和手动刷新
- 集成到现有认证系统
- 提供测试页面和文档
