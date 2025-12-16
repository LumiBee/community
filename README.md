# LumiBee Hive - 高性能智能后端服务

LumiBee Hive 是一个基于 Spring Boot 3 和 Java 17 构建的现代化、功能丰富的后端应用程序。它集成了一系列尖端技术，旨在提供一个高性能、可扩展且智能的基础平台，适用于社区、知识库或任何需要复杂后端支持的应用程序。

## ✨ 主要特性

- **安全认证**: 基于 Spring Security 和 JWT 的强大用户认证与授权系统。
- **高效数据持久化**: 使用 MySQL 和 MyBatis-Plus，结合 Druid 连接池，实现高效的数据管理。
- **全文搜索**: 集成 Elasticsearch，提供强大的实时全文搜索能力。
- **AI 能力集成**: 内置 Spring AI 并对接阿里巴巴通义千问（Dashscope），轻松实现智能问答、内容生成等功能。
- **自动化API文档**: 通过 OpenAPI 3 (Swagger) 自动生成交互式 API 文档，方便前端对接和测试。
- **多级缓存**: 融合 Caffeine 和 Redis，实现本地加分布式多级缓存，显著提升应用性能。
- **分布式锁**: 采用 Redisson 实现可靠的分布式锁，确保分布式环境下的数据一致性。
- **云存储支持**: 支持文件上传至本地或阿里云 OSS，灵活应对不同存储需求。
- **全方位监控**: 通过 Spring Boot Actuator 暴露监控端点，并可与 Prometheus 集成，实现应用性能的实时监控。
- **平滑数据库迁移**: 使用 Flyway 管理数据库版本，让数据库结构变更和数据迁移自动化、版本化。

## 🛠️ 技术栈

| 类别 | 技术 |
| --- | --- |
| **核心框架** | Spring Boot 3, Spring Framework 6 |
| **编程语言** | Java 17 |
| **数据访问** | MyBatis-Plus, Druid, Flyway |
| **数据库** | MySQL 8, Redis |
| **搜索** | Elasticsearch |
| **缓存** | Spring Cache, Caffeine, Redisson |
| **安全** | Spring Security, JWT, OAuth2 |
| **AI** | Spring AI, Alibaba Dashscope |
| **API** | RESTful API, OpenAPI 3 (Swagger UI) |
| **DevOps** | Maven, Docker, Actuator, Prometheus |
| **云服务** | 阿里云 OSS |

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Elasticsearch 8.0+

### 启动步骤

1.  **克隆项目**
    ```bash
    git clone https://github.com/your-username/hive.git
    cd hive
    ```

2.  **数据库设置**
    - 在 MySQL 中创建一个新的数据库，例如 `community`。
    - Flyway 将在应用首次启动时自动处理表的创建和初始化。

3.  **配置应用程序**
    - 复制或重命名 `src/main/resources/application.yml`。
    - 修改以下关键配置以匹配您的本地环境：
      - `spring.datasource`: 配置您的 MySQL URL、用户名和密码。
      - `spring.redis`: 配置您的 Redis 地址和密码。
      - `spring.ai.dashscope.api-key`: 填入您的阿里巴巴 Dashscope API Key。
      - `jwt.secret`: **强烈建议**修改为一个长且随机的字符串，用于生产环境。
      - 其他第三方API Key（如 `qweather`, `search-api`）根据需要配置。

4.  **运行项目**
    ```bash
    mvn spring-boot:run
    ```

5.  **访问应用**
    - 应用启动后，API 服务将在 `http://localhost:8090/api` 上可用。

## 📄 API 文档

应用启动后，您可以访问交互式的 API 文档（由 Swagger UI 提供）：

[http://localhost:8090/swagger-ui.html](http://localhost:8090/swagger-ui.html)

## 🩺 系统监控

项目集成了 Spring Boot Actuator 来提供丰富的监控端点。

- **监控端点基础路径**: `/actuator`
- **健康检查**: `http://localhost:8090/actuator/health`
- **Prometheus 指标**: `http://localhost:8090/actuator/prometheus`

## ⚙️ 配置

应用程序的主要配置文件是 `src/main/resources/application.yml`。项目默认使用 `dev` 环境配置。

为了安全起见，在生产环境中，强烈建议通过**环境变量**或外部配置文件来覆盖敏感信息（如数据库密码、API密钥、JWT密钥）。

**示例 (通过环境变量覆盖数据库密码):**
```bash
export SPRING_DATASOURCE_PASSWORD="your_production_password"
mvn spring-boot:run
```

## 🤝 贡献

欢迎任何形式的贡献！如果您有任何想法或问题，请随时提交 Pull Request 或创建 Issue。
