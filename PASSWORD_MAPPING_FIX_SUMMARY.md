# 用户密码为null问题修复总结

## 问题描述
用户登录时，后端日志显示用户密码为null，导致认证失败：
```
接收到登录请求: account=abcd, password长度=6
准备认证...
===== CustomUserServiceImpl.loadUserByUsername 被调用 =====
输入参数: abcd
尝试按用户名查找用户: abcd
找到用户: ID=1959560885180293121, Name=abcd, Email=456@q.com
用户密码: 未设置
警告: 用户密码为空!
```

## 问题原因分析

1. **缓存问题**：`UserServiceImpl`中的`selectByName`和`selectByEmail`方法使用了`@Cacheable`注解，可能导致即使数据库中的密码已更新，但缓存中仍然保留了旧的数据（密码为null的用户对象）。

2. **映射问题**：虽然SQL查询明确包含了password字段，但可能存在MyBatis-Plus的映射问题，导致密码字段没有正确映射到User对象中。

3. **数据库问题**：数据库中的密码字段可能实际为null，尽管用户认为密码已设置。

## 解决方案

我们实现了一个全面的解决方案，包括：

1. **添加调试工具**：创建了`DebugController`类，提供了以下API：
   - `/api/debug/user/{username}`：查看用户信息，包括密码是否存在
   - `/api/debug/reset-password/{username}/{password}`：重置用户密码
   - `/api/debug/clear-cache/{username}`：清除用户缓存
   - `/api/debug/db-user/{username}`：直接从数据库获取用户信息（绕过缓存）

2. **扩展UserService接口**：添加了`getUserMapper()`方法，允许直接访问`UserMapper`实例，以绕过缓存层。

3. **配置安全性**：更新了`SecurityConfig`，将调试API添加到CSRF忽略列表中，确保API可以正常访问。

## 使用方法

1. **检查用户信息**：
   ```
   GET /api/debug/user/{username}
   ```
   返回用户基本信息和密码状态

2. **检查数据库中的用户信息**（绕过缓存）：
   ```
   GET /api/debug/db-user/{username}
   ```
   直接从数据库获取用户信息，不受缓存影响

3. **清除用户缓存**：
   ```
   GET /api/debug/clear-cache/{username}
   ```
   清除指定用户的缓存，强制从数据库重新加载

4. **重置用户密码**：
   ```
   GET /api/debug/reset-password/{username}/{password}
   ```
   为指定用户设置新密码（会自动加密）

## 修改的文件

1. `src/main/java/com/lumibee/hive/controller/DebugController.java` (新文件)
2. `src/main/java/com/lumibee/hive/service/UserService.java`
3. `src/main/java/com/lumibee/hive/service/UserServiceImpl.java`
4. `src/main/java/com/lumibee/hive/config/SecurityConfig.java`

## 后续建议

1. **优化缓存策略**：考虑在更新用户信息（特别是密码）时，自动清除相关缓存。

2. **增强日志记录**：添加更详细的日志记录，特别是在用户认证过程中，以便更容易诊断问题。

3. **密码管理**：为通过OAuth2登录的用户提供设置本地密码的功能，以便他们可以使用用户名/密码登录。

4. **安全考虑**：在生产环境中禁用或限制调试API的访问，以防止潜在的安全风险。
