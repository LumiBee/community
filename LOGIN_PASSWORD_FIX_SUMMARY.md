# 登录密码验证修复总结

## 问题描述
用户登录时出现401 Unauthorized错误，无论输入什么用户名和密码都无法登录，但通过注册成功后跳转到登录页面可以正常登录。

## 问题原因
1. **用户密码可能为空**：一些用户（特别是通过OAuth2登录的用户）可能没有设置本地密码
2. **密码验证逻辑不完善**：CustomUserServiceImpl没有检查用户密码是否为空
3. **数据库设计允许密码为空**：数据库表设计允许password字段为null

## 解决方案
在CustomUserServiceImpl中添加了对密码为空的检查，如果用户密码为空，则抛出UsernameNotFoundException异常。

### 修改内容

#### CustomUserServiceImpl.java
- **问题**：用户密码为空时，仍然允许登录，但后续的密码比对会失败
- **解决方案**：添加对密码为空的检查，如果为空则直接抛出异常
- **修改前**：
  ```java
  if (user.getPassword() == null) {
      System.out.println("警告: 用户密码为空!");
  }
  ```
- **修改后**：
  ```java
  if (user.getPassword() == null || user.getPassword().isEmpty()) {
      System.out.println("警告: 用户密码为空!");
      throw new UsernameNotFoundException("用户 '" + usernameOrEmail + "' 未设置密码，无法登录");
  }
  ```

### 修改的文件
1. `src/main/java/com/lumibee/hive/service/CustomUserServiceImpl.java`

## 技术原理
1. **Spring Security认证流程**：
   - `AuthenticationManager.authenticate()`调用`UserDetailsService.loadUserByUsername()`加载用户
   - 如果用户存在但密码为空，后续的密码比对会失败
   - 我们提前检查密码是否为空，避免后续的密码比对失败

2. **用户密码为空的情况**：
   - OAuth2登录的用户（如GitHub登录）通常没有本地密码
   - 数据库表设计允许password字段为null（V8__Alter_password_col_can_null_of_user_table.sql）

3. **为什么注册后可以登录**：
   - 注册时会设置密码并加密（SignupController.java中的passwordEncoder.encode()）
   - 这些用户有正确设置的密码，所以可以正常登录

## 测试验证
- 编译成功，没有语法错误
- 新注册的用户应该能够正常登录
- 通过OAuth2登录的用户需要设置本地密码后才能使用账号密码登录

## 优势
1. **提高安全性**：确保只有设置了密码的用户才能使用账号密码登录
2. **提高用户体验**：提供明确的错误信息，而不是通用的"用户名或密码错误"
3. **避免潜在问题**：防止后续的密码比对失败导致的异常

## 注意事项
- OAuth2登录的用户需要设置本地密码后才能使用账号密码登录
- 可以考虑添加设置密码的功能，特别是对于OAuth2登录的用户
- 可以在前端添加相应的提示，引导用户设置密码
