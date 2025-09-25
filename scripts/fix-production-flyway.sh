#!/bin/bash

# 生产环境Flyway修复脚本
# 解决校验和不匹配问题

set -e

echo "🔧 开始修复生产环境Flyway校验和问题..."

# 配置变量
DB_HOST=${DB_HOST:-"mysql-db"}
DB_PORT=${DB_PORT:-"3306"}
DB_NAME=${DB_NAME:-"community"}
DB_USER=${DB_USER:-"root"}
DB_PASSWORD=${DB_PASSWORD:-"cgqlbh114514"}

echo "📊 当前迁移状态："
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASSWORD $DB_NAME -e "
SELECT version, description, checksum, success 
FROM flyway_schema_history 
ORDER BY installed_rank DESC;
"

echo "🔧 执行Flyway repair..."
mvn flyway:repair \
  -Dflyway.url="jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true" \
  -Dflyway.user=$DB_USER \
  -Dflyway.password=$DB_PASSWORD

echo "✅ 修复完成！"
echo "📊 修复后的迁移状态："
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASSWORD $DB_NAME -e "
SELECT version, description, checksum, success 
FROM flyway_schema_history 
ORDER BY installed_rank DESC;
"

echo "🚀 现在可以重新启动应用了"
