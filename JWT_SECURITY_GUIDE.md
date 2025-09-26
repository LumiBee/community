# JWT安全配置指南

## 概述

JWT密钥已从代码中移除，现在通过配置文件进行管理，提高了安全性。

## 当前配置

### 开发环境 (application-dev.yml)
```yaml
jwt:
  secret: lumiHiveSecretKeyForJwtAuthenticationToken1234567890123456789012345678901234567890
  expiration: 86400000  # 24小时
```

### 生产环境 (application-prod.yml)
```yaml
jwt:
  secret: lumiHiveSecretKeyForJwtAuthenticationToken1234567890123456789012345678901234567890
  expiration: 86400000  # 24小时
```

## 安全建议

### 1. 生产环境密钥管理

**强烈建议**在生产环境中使用环境变量或密钥管理服务：

#### 方法1：环境变量
```bash
# 设置环境变量
export JWT_SECRET="your-super-secure-64-character-secret-key-here"
export JWT_EXPIRATION="86400000"
```

然后在 `application-prod.yml` 中：
```yaml
jwt:
  secret: ${JWT_SECRET:lumiHiveSecretKeyForJwtAuthenticationToken1234567890123456789012345678901234567890}
  expiration: ${JWT_EXPIRATION:86400000}
```

#### 方法2：Docker环境变量
```yaml
# docker-compose.yml
environment:
  - JWT_SECRET=your-super-secure-64-character-secret-key-here
  - JWT_EXPIRATION=86400000
```

### 2. 密钥生成

生成安全的64字符密钥：
```bash
# 使用openssl生成随机密钥
openssl rand -base64 48 | tr -d "=+/" | cut -c1-64

# 或者使用Python
python3 -c "import secrets; print(secrets.token_urlsafe(48)[:64])"
```

### 3. 密钥轮换

定期更换JWT密钥：
1. 生成新密钥
2. 更新配置文件或环境变量
3. 重启应用
4. 所有现有Token将失效，用户需要重新登录

### 4. 密钥要求

- **长度**：至少64字符（512位）
- **复杂度**：包含大小写字母、数字、特殊字符
- **唯一性**：每个环境使用不同的密钥
- **保密性**：不要提交到版本控制系统

## 配置验证

### 检查密钥长度
```java
// 在JwtUtil中添加验证方法
@PostConstruct
public void validateSecret() {
    if (jwtSecret.length() < 64) {
        throw new IllegalStateException("JWT secret must be at least 64 characters long");
    }
}
```

### 测试Token生成
```bash
# 测试登录接口
curl -X POST https://api.hivelumi.com/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
```

## 监控和日志

### 1. 密钥使用监控
- 监控JWT Token生成频率
- 记录异常Token验证失败
- 设置密钥轮换提醒

### 2. 安全日志
```yaml
logging:
  level:
    com.lumibee.hive.utils.JwtUtil: DEBUG
```

## 故障排除

### 常见问题

1. **密钥长度不足**
   - 错误：`WeakKeyException: The signing key's size is 376 bits`
   - 解决：确保密钥至少64字符

2. **配置未生效**
   - 检查配置文件格式
   - 确认环境变量设置
   - 重启应用

3. **Token验证失败**
   - 检查密钥一致性
   - 确认Token格式正确
   - 检查过期时间

## 更新历史

- 2025-09-26: 将JWT密钥从代码移至配置文件
- 添加环境变量支持
- 创建安全配置指南
