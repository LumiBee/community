# Flyway 数据库迁移指南

## 概述

本项目使用 Flyway 进行数据库版本管理和迁移。Flyway 确保数据库结构在不同环境（开发、测试、生产）之间保持一致。

## 目录结构

```
src/main/resources/db/migration/
├── V1__Initial_schema.sql          # 基线版本 - 初始表结构
├── V2__Add_user_preferences_table.sql  # 示例：添加新表
└── V3__Add_article_reading_time.sql    # 示例：修改现有表
```

## 迁移文件命名规范

### 版本化迁移（推荐）
- 格式：`V{版本号}__{描述}.sql`
- 示例：`V1__Initial_schema.sql`、`V2__Add_user_table.sql`
- 特点：按版本号顺序执行，不可修改

### 可重复迁移
- 格式：`R__{描述}.sql`
- 示例：`R__Update_data.sql`
- 特点：每次启动都会检查并执行

### 撤销迁移
- 格式：`U{版本号}__{描述}.sql`
- 示例：`U2__Remove_user_table.sql`
- 特点：用于回滚特定版本

## 使用方法

### 1. 创建新的迁移文件

```bash
# 在 src/main/resources/db/migration/ 目录下创建新文件
# 文件名格式：V{下一个版本号}__{描述}.sql

# 例如：V4__Add_notification_table.sql
```

### 2. 编写迁移SQL

```sql
-- V4__Add_notification_table.sql
CREATE TABLE IF NOT EXISTS `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` text,
  `is_read` boolean DEFAULT false,
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 3. 执行迁移

#### 开发环境
```bash
# 启动应用，Flyway会自动执行未执行的迁移
mvn spring-boot:run

# 或者使用Docker
docker-compose up -d
```

#### 生产环境
```bash
# 生产环境会自动执行迁移，但不会清理数据库
# 确保在部署前测试迁移脚本
```

## 最佳实践

### 1. 迁移文件编写规范

- ✅ **使用 IF NOT EXISTS**：避免重复创建表
- ✅ **添加注释**：说明迁移的目的和影响
- ✅ **测试迁移**：在开发环境充分测试
- ✅ **备份数据**：生产环境迁移前备份重要数据

```sql
-- ✅ 好的示例
CREATE TABLE IF NOT EXISTS `new_table` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '表名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新表说明';

-- ❌ 避免的写法
CREATE TABLE `new_table` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
);
```

### 2. 数据迁移

```sql
-- 添加新字段并设置默认值
ALTER TABLE `articles` 
ADD COLUMN `new_field` varchar(255) DEFAULT 'default_value' COMMENT '新字段';

-- 迁移现有数据
UPDATE `articles` 
SET `new_field` = 'calculated_value' 
WHERE `some_condition` = true;
```

### 3. 索引和约束

```sql
-- 添加索引
CREATE INDEX `idx_articles_new_field` ON `articles` (`new_field`);

-- 添加外键约束
ALTER TABLE `new_table` 
ADD CONSTRAINT `fk_new_table_user` 
FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
```

## 常见操作

### 查看迁移状态

```sql
-- 查看迁移历史
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;

-- 查看当前版本
SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank DESC LIMIT 1;
```

### 处理迁移失败

1. **检查错误日志**：查看应用启动日志中的Flyway错误信息
2. **修复SQL**：修正迁移文件中的SQL错误
3. **重新执行**：重启应用，Flyway会重新尝试执行失败的迁移

### 回滚迁移

```sql
-- 手动删除迁移记录（谨慎使用）
DELETE FROM flyway_schema_history WHERE version = 'V4';

-- 手动回滚数据库结构
DROP TABLE IF EXISTS `notifications`;
```

## 环境配置

### 开发环境 (application-dev.yml)
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    clean-disabled: false  # 允许清理数据库
```

### 生产环境 (application-prod.yml)
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    clean-disabled: true   # 禁用清理操作
    out-of-order: false    # 严格按顺序执行
```

## 故障排除

### 1. 迁移文件冲突
- 确保版本号唯一且递增
- 不要修改已执行的迁移文件

### 2. 外键约束问题
- 按依赖关系顺序创建表
- 使用 `IF NOT EXISTS` 避免重复创建

### 3. 字符集问题
- 统一使用 `utf8mb4` 字符集
- 确保排序规则为 `utf8mb4_unicode_ci`

## 监控和维护

### 1. 定期检查
- 监控 `flyway_schema_history` 表
- 检查迁移执行状态

### 2. 备份策略
- 生产环境迁移前备份数据库
- 保留迁移文件的版本控制

### 3. 团队协作
- 使用Git管理迁移文件
- 代码审查迁移脚本
- 统一迁移文件命名规范

## 示例场景

### 场景1：添加新功能表
```sql
-- V5__Add_chat_system.sql
CREATE TABLE IF NOT EXISTS `chat_rooms` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `creator_id` bigint NOT NULL,
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_chat_rooms_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 场景2：修改现有表结构
```sql
-- V6__Add_article_categories.sql
ALTER TABLE `articles` 
ADD COLUMN `category_id` int DEFAULT NULL COMMENT '文章分类ID' AFTER `portfolio_id`;

CREATE INDEX `idx_articles_category` ON `articles` (`category_id`);
```

### 场景3：数据迁移
```sql
-- V7__Migrate_legacy_data.sql
-- 为现有文章设置默认分类
UPDATE `articles` 
SET `category_id` = 1 
WHERE `category_id` IS NULL AND `status` = 'published';
```

---

**注意**：在生产环境执行迁移前，请务必在测试环境验证迁移脚本的正确性。
