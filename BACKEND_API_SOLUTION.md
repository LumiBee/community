# 后端API问题解决方案

## 🐛 问题诊断

### 错误现象：
1. **Thymeleaf模板错误**：
   ```
   Error resolving template [tags], template might not exist
   Error resolving template [index], template might not exist
   ```

2. **内容类型转换错误**：
   ```
   No converter for [class java.util.LinkedHashMap] with preset Content-Type 'text/html;charset=UTF-8'
   ```

### 根本原因：
- `IndexController` 使用 `@Controller` 注解，返回Thymeleaf模板名称
- Spring Boot试图渲染不存在的模板文件（因为我们已改用Vue前端）
- 控制器期望返回HTML页面，但应该返回JSON数据

## ✅ 解决方案

### 1. 创建新的REST API控制器

创建了 `ApiController.java`：
- 使用 `@RestController` 注解
- 映射到 `/api` 路径
- 返回JSON数据而不是模板名称
- 配置CORS支持前端跨域访问

### 2. API端点映射

| 原始端点 | 新API端点 | 功能 |
|---------|-----------|------|
| `GET /` | `GET /api/home` | 获取首页数据 |
| `GET /tags` | `GET /api/tags` | 获取所有标签 |
| `GET /portfolio` | `GET /api/portfolios` | 获取作品集 |
| `GET /profile` | `GET /api/profile` | 获取用户资料 |
| `GET /search` | `GET /api/search` | 搜索文章 |
| `GET /favorites` | `GET /api/favorites` | 获取收藏 |
| `GET /messages` | `GET /api/messages` | 获取消息 |

### 3. 更新前端API配置

更新了前端API文件，确保请求正确的端点：
- `articleAPI.getHomeArticles()` → `/api/home`
- `tagAPI.getAllTags()` → `/api/tags`
- `portfolioAPI.getAllPortfolios()` → `/api/portfolios`
- `userAPI.getProfile()` → `/api/profile`
- `articleAPI.searchArticles()` → `/api/search`
- `favoriteAPI.getFavorites()` → `/api/favorites`

## 🚀 测试验证

### 后端编译测试：
```bash
cd /Users/czar/Java/hive
mvn compile
# ✅ BUILD SUCCESS
```

### 前端构建测试：
```bash
cd frontend
npm run build
# ✅ built in 1.32s
```

## 📋 启动说明

### 1. 启动后端：
```bash
cd /Users/czar/Java/hive
mvn spring-boot:run
# 服务运行在 http://localhost:8090
```

### 2. 启动前端：
```bash
cd frontend
npm run dev
# 服务运行在 http://localhost:3000 或 3001
```

### 3. 测试API：
访问以下端点验证API工作正常：
- http://localhost:8090/api/home
- http://localhost:8090/api/tags
- http://localhost:8090/api/portfolios

## 🔧 CORS配置

已在 `CorsConfig.java` 中配置：
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}
```

额外在 `ApiController` 中添加了注解：
```java
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
```

## 📝 注意事项

1. **保留原控制器**：原 `IndexController` 仍然保留，如果需要可以删除
2. **认证状态**：API支持获取当前登录用户信息
3. **错误处理**：新API包含适当的错误响应（401, 404等）
4. **数据结构**：返回的JSON数据结构与前端期望一致

## 🎯 下一步

现在可以正常启动前后端进行开发：
1. 后端API正常返回JSON数据
2. 前端可以正常请求API
3. 不再有Thymeleaf模板错误
4. CORS配置允许跨域访问

前后端通信现在应该完全正常工作！
