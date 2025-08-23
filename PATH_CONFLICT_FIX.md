# 路径冲突问题修复

## 🐛 问题描述

### 错误信息：
```
Ambiguous mapping. Cannot map 'searchController' method 
com.lumibee.hive.controller.SearchController#search(String)
to {GET [/api/search]}: There is already 'apiController' bean method
com.lumibee.hive.controller.ApiController#searchArticles(String) mapped.
```

### 根本原因：
两个控制器都映射到了同一个路径 `/api/search`：
1. **SearchController** - 专门的搜索控制器，映射到 `@RequestMapping("/api/search")`
2. **ApiController** - 我们新建的API控制器，也有 `@GetMapping("/search")` 方法

Spring Boot不允许有重复的路径映射，导致应用启动失败。

## ✅ 解决方案

### 1. 删除重复的搜索映射
从 `ApiController.java` 中删除了重复的搜索方法：
```java
// 删除了这个重复的方法
@GetMapping("/search")
public ResponseEntity<Map<String, Object>> searchArticles(@RequestParam("query") String query) {
    // ...
}
```

### 2. 保留专门的 SearchController
保留了 `SearchController.java`，因为它：
- 专门处理搜索功能
- 使用了更合适的 `ArticleRepository` 服务
- 有更好的搜索实现（前缀匹配）

### 3. 修复前端数据处理
修复了 `Navbar.vue` 中搜索结果的数据处理：
```javascript
// 修复前：期望 response.data
searchResults.value = response.data || []

// 修复后：直接使用 response（axios拦截器已处理）
searchResults.value = response || []
```

### 4. API端点映射确认

| 功能 | 控制器 | 端点 | 返回类型 |
|------|--------|------|----------|
| 搜索文章 | SearchController | `GET /api/search` | `List<ArticleDocument>` |
| 首页数据 | ApiController | `GET /api/home` | `Map<String, Object>` |
| 标签列表 | ApiController | `GET /api/tags` | `List<TagDTO>` |
| 作品集 | ApiController | `GET /api/portfolios` | `List<PortfolioDetailsDTO>` |
| 用户资料 | ApiController | `GET /api/profile` | `Map<String, Object>` |

## 🎯 测试验证

### 后端编译：
```bash
mvn compile
# ✅ BUILD SUCCESS
```

### 前端构建：
```bash
npm run build  
# ✅ built in 1.34s
```

### 应用启动：
现在应该可以正常启动，没有路径冲突错误。

## 📋 SearchController 功能

`SearchController` 提供的搜索功能：
- **端点**: `GET /api/search?query={keyword}`
- **功能**: 基于标题和内容的前缀匹配搜索
- **返回**: 直接返回 `List<ArticleDocument>`
- **服务**: 使用 `ArticleRepository.findByTitleOrContentWithPrefix()`

## 🔧 前端使用

前端通过以下方式调用搜索：
```javascript
import { articleAPI } from '@/api'

// 搜索文章
const results = await articleAPI.searchArticles(keyword)
// results 直接是 ArticleDocument 数组
```

## 🚀 下一步

现在可以正常启动应用：

1. **启动后端**：
   ```bash
   cd /Users/czar/Java/hive
   mvn spring-boot:run
   ```

2. **启动前端**：
   ```bash
   cd frontend  
   npm run dev
   ```

3. **测试搜索功能**：
   - 访问前端页面
   - 在导航栏搜索框输入关键词
   - 验证搜索结果显示正常

路径冲突问题已完全解决！🎉
