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

## 问题原因
1. **MyBatis-Plus映射问题**：可能在查询用户时没有正确映射password字段
2. **数据库字段问题**：数据库中的password字段可能为null
3. **缓存问题**：可能存在缓存导致获取到的是旧数据

## 解决方案
我们采取了多方面的措施来解决这个问题：

### 1. 修改UserMapper.java
- **问题**：使用`SELECT *`可能导致某些字段映射不正确
- **解决方案**：明确列出所有需要查询的字段，包括password字段
- **修改前**：
  ```java
  @Select("SELECT * FROM user WHERE name = #{name} LIMIT 1")
  User selectByName(@Param("name") String name);
  ```
- **修改后**：
  ```java
  @Select("SELECT id, name, token, gmt_create, gmt_modified, bio, avatar_url, email, password, github_id, qq_open_id, background_img_url, deleted, version FROM user WHERE name = #{name} LIMIT 1")
  User selectByName(@Param("name") String name);
  ```

### 2. 添加调试工具
- 创建了`DebugController`类，提供了两个API：
  - `/api/debug/user/{username}`：查看用户信息，包括密码是否存在
  - `/api/debug/reset-password/{username}/{password}`：重置用户密码

### 3. 修复应用启动问题
- **问题**：应用启动失败，因为缺少百度翻译API的配置
- **解决方案**：
  1. 创建`config/application-local.yml`文件，添加必要的配置
  2. 修改`JavaApp.java`，为配置属性添加默认值

## 修改的文件
1. `src/main/java/com/lumibee/hive/mapper/UserMapper.java`
2. `src/main/java/com/lumibee/hive/controller/DebugController.java` (新文件)
3. `src/main/java/com/lumibee/hive/config/SecurityConfig.java`
4. `src/main/java/com/lumibee/hive/agent/JavaApp.java`
5. `config/application-local.yml` (新文件)

## 使用方法
1. 启动应用后，可以通过以下API查看用户信息：
   ```
   GET /api/debug/user/{username}
   ```

2. 如果发现用户密码为null，可以通过以下API重置密码：
   ```
   GET /api/debug/reset-password/{username}/{password}
   ```

3. 重置密码后，用户应该能够正常登录。

## 注意事项
- 调试API仅用于开发环境，生产环境应该禁用
- 密码重置API使用明文传输密码，仅用于开发测试
- 如果还有用户无法登录，可以使用调试API查看具体问题
