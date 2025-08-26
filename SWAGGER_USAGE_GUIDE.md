# Swagger/OpenAPI 使用指南

## 概述

本项目已集成 Swagger/OpenAPI 3，用于自动生成 API 文档。开发人员可以通过这些文档了解 API 的使用方法，前端开发人员也可以直接在 Swagger UI 中测试 API。

## 访问方式

### 1. Swagger UI 界面
启动应用后，访问：`http://localhost:8090/swagger-ui.html`

### 2. OpenAPI JSON 文档
访问：`http://localhost:8090/api-docs`

### 3. 快速测试
运行项目根目录下的测试脚本：
```bash
./test_swagger.sh
```

这个脚本会测试所有 Swagger 相关路径的访问状态。

## 主要功能

### 1. API 文档浏览
- 按标签分组查看 API 接口
- 查看每个接口的详细说明
- 查看请求参数和响应格式

### 2. 在线测试
- 直接在浏览器中测试 API 接口
- 填写请求参数并发送请求
- 查看响应结果
- 使用 Authorize 按钮进行认证

### 3. 代码生成
- 可以导出 OpenAPI 规范文件
- 支持多种编程语言的客户端代码生成

## 已配置的 API 分组

### 文章管理 (`@Tag(name = "文章管理")`)
- `GET /api/articles/popular` - 获取热门文章
- `GET /api/articles/featured` - 获取精选文章
- `GET /api/article/{slug}` - 根据 slug 获取文章详情
- `POST /api/article/{articleId}/like` - 切换文章点赞状态
- `GET /api/article/{articleId}/related` - 获取相关文章

### 文章发布 (`@Tag(name = "文章发布")`)
- `POST /api/article/publish` - 发布新文章 **[需要认证]**
- `PUT /api/article/{articleId}/edit` - 编辑文章 **[需要认证]**
- `DELETE /api/article/delete/{articleId}` - 删除文章 **[需要认证]**

> **注意**：所有文章发布相关的 API 都需要用户认证。在 Swagger UI 中测试这些接口时，需要先登录系统获取认证信息。

### AI 服务 (`@Tag(name = "AI 服务")`)
- `POST /api/ai/generate-summary-deepseek` - 生成文章摘要

## 常用注解说明

### 类级别注解
```java
@Tag(name = "分组名称", description = "分组描述")
```

### 方法级别注解
```java
@Operation(summary = "接口摘要", description = "详细描述")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "成功"),
    @ApiResponse(responseCode = "400", description = "参数错误"),
    @ApiResponse(responseCode = "401", description = "未认证"),
    @ApiResponse(responseCode = "500", description = "服务器错误")
})
```

### 参数注解
```java
@Parameter(description = "参数描述")
@RequestBody(description = "请求体描述")
@PathVariable(description = "路径参数描述")
```

### 响应注解
```java
@Content(schema = @Schema(implementation = ResponseClass.class))
```

## 最佳实践

### 1. 为所有公共 API 添加注解
```java
@RestController
@Tag(name = "用户管理", description = "用户相关的 API 接口")
public class UserController {
    
    @GetMapping("/api/users/{id}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public ResponseEntity<User> getUser(
        @Parameter(description = "用户ID") @PathVariable Long id) {
        // 实现代码
    }
}
```

### 2. 使用清晰的描述
- `summary`: 简洁明了，一句话概括接口功能
- `description`: 详细说明，包括使用场景、注意事项等

### 3. 完整的响应码说明
- 列出所有可能的响应状态码
- 为每个状态码提供清晰的描述

### 4. 参数验证说明
- 使用 `@Parameter` 注解描述参数
- 说明参数的格式、范围、是否必填等

## 认证与安全配置

### 1. 使用认证功能
Swagger UI 提供了认证功能，可以在测试需要认证的 API 时使用：

1. 点击 Swagger UI 界面右上角的 "Authorize" 按钮
2. 在弹出的对话框中，输入您的 JWT token（格式：Bearer xxx...）
3. 点击 "Authorize" 按钮确认
4. 现在您可以测试需要认证的 API 了

获取 JWT token 的方法：
- 通过正常登录系统获取
- 使用浏览器开发者工具查看请求头中的 Authorization 字段
- 或者使用登录 API 获取

### 2. 生产环境配置
在生产环境中，建议：
- 禁用 Swagger UI（设置 `springdoc.swagger-ui.enabled=false`）
- 限制 API 文档访问权限
- 使用 HTTPS 访问

### 3. 开发环境配置
在开发环境中：
- 启用 Swagger UI
- 允许所有开发人员访问
- 配置适当的 CORS 设置

## 故障排除

### 1. 无法访问 Swagger UI
- 检查应用是否正常启动
- 确认端口 8090 是否被占用
- 检查防火墙设置
- **如果跳转到登录页面**：检查 Spring Security 配置，确保 Swagger 路径已添加到白名单

### 2. 测试需要认证的接口时出现 401 或 NullPointerException
- 这是因为 Swagger UI 默认不会发送认证信息
- 解决方法：
  1. 先通过正常的前端界面登录系统
  2. 然后再使用 Swagger UI 测试接口
  3. 或者在 Swagger UI 中使用 Authorize 按钮提供认证信息

### 3. 被 Spring Security 拦截
如果访问 Swagger UI 时跳转到登录页面，需要在 `SecurityConfig.java` 中添加以下路径到白名单：
```java
.permitAll() // 允许所有用户访问
    .requestMatchers(
        // ... 其他路径
        "/swagger-ui/**", // Swagger UI 界面
        "/swagger-ui.html", // Swagger UI 主页
        "/api-docs/**", // OpenAPI 文档
        "/v3/api-docs/**" // OpenAPI 3 文档
    ).permitAll()
```

同时也要在 CSRF 忽略列表中添加这些路径：
```java
.ignoringRequestMatchers(
    // ... 其他路径
    "/swagger-ui/**",
    "/swagger-ui.html", 
    "/api-docs/**",
    "/v3/api-docs/**"
)
```

### 3. API 文档不完整
- 确认控制器类已添加 `@Tag` 注解
- 确认方法已添加 `@Operation` 注解
- 检查包扫描配置是否正确

### 4. 注解不生效
- 确认已添加 springdoc-openapi 依赖
- 重启应用以使配置生效
- 检查注解导入是否正确

## 扩展功能

### 1. 自定义响应示例
```java
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "成功",
        content = @Content(
            schema = @Schema(implementation = User.class),
            examples = @ExampleObject(
                name = "成功示例",
                value = "{\"id\": 1, \"name\": \"张三\", \"email\": \"zhangsan@example.com\"}"
            )
        )
    )
})
```

### 2. 添加认证信息
```java
@SecurityRequirement(name = "bearerAuth")
@Operation(security = @SecurityRequirement(name = "bearerAuth"))
```

### 3. 文件上传说明
```java
@Operation(summary = "上传文件", description = "支持多种文件格式")
@Parameter(name = "file", description = "要上传的文件", required = true)
public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
    // 实现代码
}
```

## 总结

Swagger/OpenAPI 是一个强大的工具，可以帮助：
- 前后端开发人员更好地协作
- 减少 API 文档维护成本
- 提供在线测试能力
- 支持客户端代码自动生成

通过合理使用这些注解，可以生成清晰、完整的 API 文档，提高开发效率和代码质量。
