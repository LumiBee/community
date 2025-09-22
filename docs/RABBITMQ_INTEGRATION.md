# RabbitMQ 消息队列集成文档

## 概述

本文档详细介绍了 Hive 项目中 RabbitMQ 消息队列的集成实现，包括架构设计、配置说明、使用方法和监控运维等内容。

## 架构设计

### 核心目标

1. **异步处理**：将文章发布后的非核心操作（搜索同步、缓存更新、标签处理）改为异步处理
2. **服务解耦**：通过消息队列解耦各个服务模块，提高系统可维护性
3. **可靠性保证**：确保消息不丢失、不重复、能重试
4. **性能提升**：大幅提升文章发布接口的响应速度

### 消息流程

```
文章发布 → 数据库写入 → 发送消息 → 立即返回响应
                ↓
        消息队列异步处理
                ↓
    ┌─────────┬─────────┬─────────┐
    │ 搜索同步 │ 缓存更新 │ 标签处理 │
    └─────────┴─────────┴─────────┘
```

## 技术实现

### 1. 消息模型

#### 基础消息类 (BaseMessage)
- `messageId`: 消息唯一ID
- `businessId`: 业务唯一ID（用于幂等性检查）
- `messageType`: 消息类型
- `createTime`: 消息创建时间
- `retryCount`: 重试次数
- `maxRetryCount`: 最大重试次数

#### 具体消息类型
- `ArticlePublishMessage`: 文章发布消息
- `ArticleSearchSyncMessage`: 搜索同步消息
- `ArticleCacheUpdateMessage`: 缓存更新消息
- `ArticleTagSyncMessage`: 标签同步消息

### 2. 队列配置

#### 交换机配置
- `hive.article.exchange`: 文章相关消息交换机（Direct类型）

#### 队列配置
- `hive.article.queue`: 文章发布主队列
- `hive.article.search.queue`: 搜索同步队列
- `hive.article.cache.queue`: 缓存更新队列
- `hive.article.tag.queue`: 标签同步队列
- `hive.dlx.queue`: 死信队列

#### 死信队列配置
- 所有队列都配置了死信交换机 `hive.dlx.exchange`
- 重试次数超限的消息会自动路由到死信队列

### 3. 可靠性机制

#### 消息不丢失
1. **Publisher Confirms**: 生产者发送消息后等待Broker确认
2. **消息持久化**: 交换机、队列、消息都设置为持久化
3. **手动确认**: 消费者处理完成后手动发送ACK

#### 消息不重复
1. **幂等性检查**: 基于业务ID的Redis幂等性检查
2. **唯一业务ID**: 每个消息都有唯一的业务标识

#### 消息重试
1. **有限重试**: 最多重试3次
2. **指数退避**: 重试间隔递增
3. **死信队列**: 重试失败后进入死信队列

## 配置说明

### 1. 依赖配置

在 `pom.xml` 中添加：

```xml
<!-- RabbitMQ 支持 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>

<!-- RabbitMQ 测试支持 -->
<dependency>
    <groupId>org.springframework.amqp</groupId>
    <artifactId>spring-rabbit-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 2. 应用配置

在 `application.yml` 中配置：

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    connection-timeout: 30000
    publisher-confirm-type: correlated
    publisher-returns: true
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 1
        concurrency: 1
        max-concurrency: 5

# 自定义 RabbitMQ 配置
rabbitmq:
  host: localhost
  port: 5672
  username: guest
  password: guest
  virtual-host: /
  connection-timeout: 30000
  requested-heartbeat: 60
  publisher-confirm-timeout: 5000
  publisher-returns-timeout: 5000
  ack-mode: manual
  prefetch-count: 1
  max-retry-attempts: 3
  retry-interval: 1000
  dead-letter:
    exchange: hive.dlx.exchange
    queue: hive.dlx.queue
    routing-key: hive.dlx
  article:
    exchange: hive.article.exchange
    queue: hive.article.queue
    routing-key: hive.article.publish
    search-queue: hive.article.search.queue
    search-routing-key: hive.article.search
    cache-queue: hive.article.cache.queue
    cache-routing-key: hive.article.cache
    tag-queue: hive.article.tag.queue
    tag-routing-key: hive.article.tag
```

## 使用方法

### 1. 发送消息

```java
@Autowired
private MessageProducerService messageProducerService;

// 发送文章发布消息
ArticlePublishMessage message = new ArticlePublishMessage();
message.setArticleId(articleId);
message.setUserId(userId);
message.setTitle(title);
message.setContent(content);
messageProducerService.sendArticlePublishMessage(message);
```

### 2. 消费消息

消息消费通过 `@RabbitListener` 注解自动处理，无需手动编写消费代码。

### 3. 监控指标

通过 Spring Boot Actuator 可以查看以下指标：

- `rabbitmq.messages.sent.total`: 发送消息总数
- `rabbitmq.messages.consumed.total`: 消费消息总数
- `rabbitmq.messages.retried.total`: 重试消息总数
- `rabbitmq.messages.dead_lettered.total`: 死信消息总数
- `rabbitmq.queue.length`: 队列长度

## 监控运维

### 1. 健康检查

访问 `/actuator/health` 可以查看 RabbitMQ 连接状态。

### 2. 指标监控

访问 `/actuator/metrics` 可以查看详细的监控指标。

### 3. 日志监控

关键日志包括：
- 消息发送成功/失败
- 消息消费成功/失败
- 幂等性检查结果
- 重试和死信队列处理

### 4. 告警配置

建议配置以下告警：
- 消息发送失败率 > 5%
- 消息消费失败率 > 5%
- 死信队列消息数量 > 10
- 队列长度 > 1000

## 部署说明

### 1. RabbitMQ 安装

#### Docker 部署
```bash
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=admin123 \
  rabbitmq:3-management
```

#### 本地安装
```bash
# Ubuntu/Debian
sudo apt-get install rabbitmq-server

# CentOS/RHEL
sudo yum install rabbitmq-server

# 启动服务
sudo systemctl start rabbitmq-server
sudo systemctl enable rabbitmq-server
```

### 2. 环境配置

#### 开发环境
```yaml
spring:
  profiles:
    active: dev
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

#### 生产环境
```yaml
spring:
  profiles:
    active: prod
  rabbitmq:
    host: ${RABBITMQ_HOST:rabbitmq-server}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:admin}
    password: ${RABBITMQ_PASSWORD:admin123}
    virtual-host: ${RABBITMQ_VHOST:/}
```

### 3. 高可用配置

#### 集群部署
```yaml
spring:
  rabbitmq:
    addresses: rabbitmq-node1:5672,rabbitmq-node2:5672,rabbitmq-node3:5672
    username: admin
    password: admin123
    virtual-host: /
```

#### 镜像队列
```bash
# 设置镜像队列策略
rabbitmqctl set_policy ha-all "^hive\." '{"ha-mode":"all"}'
```

## 故障排查

### 1. 常见问题

#### 连接失败
- 检查 RabbitMQ 服务是否启动
- 检查网络连接和防火墙设置
- 验证用户名密码是否正确

#### 消息堆积
- 检查消费者是否正常运行
- 检查消息处理逻辑是否有异常
- 考虑增加消费者数量

#### 消息丢失
- 检查消息持久化配置
- 检查 Publisher Confirms 配置
- 检查消费者 ACK 机制

### 2. 调试工具

#### RabbitMQ 管理界面
访问 `http://localhost:15672` 可以查看：
- 队列状态
- 消息统计
- 连接信息
- 交换机配置

#### 日志分析
```bash
# 查看应用日志
tail -f logs/hive.log | grep -i rabbit

# 查看 RabbitMQ 日志
tail -f /var/log/rabbitmq/rabbit@hostname.log
```

## 性能优化

### 1. 队列优化
- 合理设置预取数量
- 调整消费者并发数
- 使用批量处理

### 2. 消息优化
- 压缩消息内容
- 使用二进制序列化
- 减少消息大小

### 3. 网络优化
- 启用消息压缩
- 调整心跳间隔
- 优化连接池配置

## 总结

通过集成 RabbitMQ 消息队列，Hive 项目实现了：

1. **性能提升**：文章发布接口响应时间从几秒缩短到几百毫秒
2. **系统解耦**：各服务模块独立部署和扩展
3. **可靠性保证**：消息不丢失、不重复、可重试
4. **监控完善**：全面的指标监控和告警机制

这套消息队列架构为 Hive 项目提供了强大的异步处理能力，为后续的功能扩展和性能优化奠定了坚实的基础。
