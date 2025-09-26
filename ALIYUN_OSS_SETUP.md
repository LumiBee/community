# 阿里云OSS集成配置指南

## 概述

本项目已集成阿里云OSS对象存储服务，用于生产环境的文件存储。开发环境仍使用本地文件存储。

## 配置步骤

### 1. 创建OSS Bucket

1. 登录阿里云控制台
2. 进入对象存储OSS服务
3. 创建新的Bucket，建议配置：
   - 存储类型：标准存储
   - 读写权限：公共读
   - 地域：选择离您服务器最近的地域

### 2. 获取访问密钥

1. 在阿里云控制台右上角点击头像
2. 选择"AccessKey管理"
3. 创建AccessKey，保存AccessKey ID和AccessKey Secret

### 3. 配置应用

编辑 `src/main/resources/application-prod.yml` 文件，更新以下配置：

```yaml
aliyun:
  oss:
    endpoint: https://oss-cn-hangzhou.aliyuncs.com  # 替换为您的OSS endpoint
    access-key-id: YOUR_ACCESS_KEY_ID  # 替换为您的AccessKey ID
    access-key-secret: YOUR_ACCESS_KEY_SECRET  # 替换为您的AccessKey Secret
    bucket-name: your-bucket-name  # 替换为您的Bucket名称
    domain: https://your-bucket-name.oss-cn-hangzhou.aliyuncs.com  # 替换为您的OSS域名
```

### 4. 获取正确的配置值

#### Endpoint
- 在OSS控制台Bucket概览页面可以找到Endpoint
- 格式：`https://oss-cn-{region}.aliyuncs.com`

#### Domain
- 格式：`https://{bucket-name}.oss-cn-{region}.aliyuncs.com`
- 或者使用自定义域名（如果已配置）

## 功能特性

### 自动环境切换
- **开发环境**：使用本地文件存储（`./uploads`目录）
- **生产环境**：自动切换到阿里云OSS存储

### 支持的功能
- 头像上传
- 背景图片上传
- 文件删除
- 自动URL生成

### 文件组织
- 头像文件存储在 `avatars/` 目录
- 背景图片存储在 `backgrounds/` 目录
- 文件名使用UUID确保唯一性

## 安全建议

1. **访问权限**：建议使用RAM子账号，只授予OSS相关权限
2. **网络安全**：可以配置OSS的IP白名单
3. **HTTPS**：确保使用HTTPS域名访问
4. **定期轮换**：定期更换AccessKey

## 监控和日志

- 文件上传/删除操作会记录在应用日志中
- 可以在阿里云OSS控制台查看存储使用情况
- 建议配置OSS的访问日志

## 故障排除

### 常见问题

1. **403 Forbidden**
   - 检查AccessKey权限
   - 确认Bucket读写权限设置

2. **404 Not Found**
   - 检查Bucket名称是否正确
   - 确认Endpoint配置

3. **连接超时**
   - 检查网络连接
   - 确认Endpoint地址正确

### 调试模式

在开发环境可以临时启用OSS测试：

```yaml
spring:
  profiles:
    active: prod  # 临时切换到生产环境配置
```

## 成本优化

1. **存储类型**：根据访问频率选择合适的存储类型
2. **生命周期**：配置自动删除过期文件
3. **CDN加速**：可以配置阿里云CDN加速文件访问

## 更新历史

- 2025-09-26: 初始集成OSS支持
- 支持环境自动切换
- 集成头像和背景图片上传功能
