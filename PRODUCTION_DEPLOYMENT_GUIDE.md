# 生产环境部署指南 - Flyway数据库迁移

## 概述

本指南说明如何在已有生产数据的情况下，安全地部署包含Flyway数据库迁移的新版本。

## 部署前准备

### 1. 备份生产数据库
```bash
# 在服务器上执行数据库备份
mysqldump -u root -p community > backup_$(date +%Y%m%d_%H%M%S).sql

# 或者使用Docker备份
docker exec mysql-db mysqldump -u root -p community > backup_$(date +%Y%m%d_%H%M%S).sql
```

### 2. 检查当前数据库结构
```sql
-- 连接到生产数据库
mysql -u root -p community

-- 检查现有表结构
SHOW TABLES;
DESCRIBE articles;

-- 检查是否已有allow_comments字段
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'community' 
AND TABLE_NAME = 'articles' 
AND COLUMN_NAME = 'allow_comments';
```

## 部署步骤

### 1. 停止现有应用
```bash
# 停止Docker容器
docker-compose down

# 或者停止其他方式运行的应用
```

### 2. 部署新代码
```bash
# 拉取最新代码
git pull origin main

# 构建新版本
mvn clean package -DskipTests

# 或者使用Docker构建
docker build -t hive-app:latest .
```

### 3. 配置Flyway Baseline
生产环境配置已经设置好：
- `baseline-version: 1` - 将现有数据库标记为V1版本
- `baseline-description: "Existing production database"` - 基线描述
- `baseline-on-migrate: true` - 自动创建基线

### 4. 启动应用
```bash
# 使用Docker启动
docker-compose up -d

# 或者直接启动应用
java -jar target/hive-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 5. 验证迁移结果
```sql
-- 检查Flyway迁移历史
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;

-- 检查新字段是否添加成功
DESCRIBE articles;

-- 检查新索引是否创建成功
SHOW INDEX FROM articles;
```

## 预期结果

### 迁移历史记录
```
installed_rank | version | description                    | success
1              | 1       | Production baseline           | 1
2              | 2       | Add allow comments field      | 1  
3              | 3       | Add production indexes        | 1
```

### 数据库结构变化
- ✅ 添加 `allow_comments` 字段到 `articles` 表
- ✅ 添加 `idx_status_deleted_time` 复合索引
- ✅ 添加 `idx_allow_comments` 索引
- ✅ 现有数据完全保留

## 回滚计划

如果出现问题，可以按以下步骤回滚：

### 1. 停止应用
```bash
docker-compose down
```

### 2. 恢复数据库
```bash
# 从备份恢复数据库
mysql -u root -p community < backup_20241224_103000.sql
```

### 3. 回滚代码
```bash
# 回滚到上一个版本
git checkout <previous-commit-hash>
```

## 监控和验证

### 1. 应用健康检查
```bash
curl http://your-server:8090/api/actuator/health
```

### 2. 数据库连接检查
```bash
curl http://your-server:8090/api/actuator/health/db
```

### 3. 功能验证
- 测试文章创建功能
- 测试评论功能
- 验证新字段是否正常工作

## 注意事项

1. **数据安全**：部署前务必备份数据库
2. **测试环境**：先在测试环境验证迁移脚本
3. **监控日志**：部署后密切关注应用日志
4. **性能影响**：添加索引可能需要一些时间，建议在低峰期部署
5. **字段默认值**：`allow_comments` 字段默认值为1，现有文章将自动允许评论

## 故障排除

### 问题1：迁移失败
```sql
-- 检查迁移历史
SELECT * FROM flyway_schema_history WHERE success = 0;

-- 手动删除失败的迁移记录
DELETE FROM flyway_schema_history WHERE version = 'X' AND success = 0;
```

### 问题2：字段已存在
```sql
-- 检查字段是否存在
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'community' 
AND TABLE_NAME = 'articles' 
AND COLUMN_NAME = 'allow_comments';
```

### 问题3：索引创建失败
```sql
-- 检查现有索引
SHOW INDEX FROM articles;

-- 手动删除重复索引
DROP INDEX idx_name ON articles;
```

## 联系信息

如有问题，请联系开发团队或查看项目文档。
