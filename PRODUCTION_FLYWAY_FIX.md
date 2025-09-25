# 生产环境Flyway校验和修复指南

## 问题描述
生产环境部署时出现Flyway校验和不匹配错误，这是因为本地修改了迁移文件，但生产数据库中的校验和记录还是旧版本的。

## 快速解决方案

### 方案1：使用修复脚本（推荐）
```bash
# 在服务器上执行
cd /opt/community  # 或您的项目目录
chmod +x scripts/fix-production-flyway.sh
./scripts/fix-production-flyway.sh
```

### 方案2：手动执行Maven命令
```bash
# 在服务器上执行
cd /opt/community
mvn flyway:repair \
  -Dflyway.url="jdbc:mysql://mysql-db:3306/community?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true" \
  -Dflyway.user=root \
  -Dflyway.password=cgqlbh114514
```

### 方案3：使用Docker执行
```bash
# 如果使用Docker
docker exec -it <container-name> mvn flyway:repair \
  -Dflyway.url="jdbc:mysql://mysql-db:3306/community?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true" \
  -Dflyway.user=root \
  -Dflyway.password=cgqlbh114514
```

### 方案4：手动更新数据库（紧急情况）
```sql
-- 连接到生产数据库
mysql -h mysql-db -u root -p community

-- 更新校验和
UPDATE flyway_schema_history SET checksum = 1496469583 WHERE version = '1';
UPDATE flyway_schema_history SET checksum = 1703340595 WHERE version = '2';
UPDATE flyway_schema_history SET checksum = -1923566472 WHERE version = '3';
UPDATE flyway_schema_history SET checksum = 948720861 WHERE version = '4';

-- 验证更新结果
SELECT version, description, checksum, success 
FROM flyway_schema_history 
ORDER BY installed_rank DESC;
```

## 修复后重启应用

```bash
# 重启Docker容器
docker-compose restart

# 或重启应用
systemctl restart your-app-service
```

## 验证部署结果

```bash
# 检查应用健康状态
curl http://your-server:8090/api/actuator/health

# 检查数据库连接
curl http://your-server:8090/api/actuator/health/db
```

## 预防措施

1. **不要修改已执行的迁移文件**：除非绝对必要
2. **使用条件检查**：在迁移文件中添加 `IF NOT EXISTS` 检查
3. **测试环境验证**：先在测试环境验证所有迁移
4. **版本控制**：确保迁移文件在Git中正确管理

## 回滚计划

如果修复失败，可以回滚到上一个版本：

```bash
# 1. 停止应用
docker-compose down

# 2. 恢复数据库（如果有备份）
mysql -h mysql-db -u root -p community < backup_file.sql

# 3. 回滚代码
git checkout <previous-commit>

# 4. 重新部署
docker-compose up -d
```
