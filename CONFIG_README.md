# 敏感信息配置说明

## 当前配置方案

项目使用 **application-secret.yml** 文件管理敏感信息，这是最简单直接的方案。

## 配置文件结构

1. **application.yml** - 主配置文件，包含非敏感配置和性能优化配置
2. **application-secret.yml** - 敏感信息配置文件（包含密码、API密钥等）

## 具体配置内容

### 主配置文件 (application.yml)
- 数据库连接池优化配置
- 监控和健康检查配置
- 应用基础配置
- 敏感信息通过注释说明在 secret 文件中

### 敏感配置文件 (application-secret.yml)
- 数据库连接信息（URL、用户名、密码）
- Elasticsearch 连接信息
- GitHub OAuth 客户端信息
- DeepSeek API 密钥

## 工作原理

Spring Boot 通过 `spring.config.import: "optional:application-secret.yml"` 自动加载敏感配置，并覆盖主配置文件中的对应配置项。

## 安全建议

1. **版本控制**：确保 `application-secret.yml` 在 `.gitignore` 中
2. **权限控制**：限制配置文件的访问权限 (chmod 600)
3. **生产环境**：考虑使用环境变量或外部配置中心
4. **密钥轮换**：定期更换API密钥和密码

## 其他可选方案

如果你想使用环境变量，可以修改配置为：
```yaml
spring:
  datasource:
    url: ${DB_URL:默认值}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD}
```

但目前项目使用的是 application-secret.yml 方案，更适合开发环境。