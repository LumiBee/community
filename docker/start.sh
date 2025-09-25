#!/bin/bash

# Docker启动脚本 - 用Docker取代Flyway

echo "🚀 启动Hive应用栈..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行，请先启动Docker"
    exit 1
fi

# 停止并删除现有容器
echo "🔄 清理现有容器..."
docker-compose down -v

# 启动服务
echo "🚀 启动服务..."
docker-compose up -d

# 等待MySQL启动
echo "⏳ 等待MySQL启动..."
sleep 30

# 检查MySQL连接
echo "🔍 检查MySQL连接..."
until docker exec mysql-db mysqladmin ping -h localhost -u root -p${DB_PASSWORD:-cgqlbh114514} --silent; do
    echo "⏳ 等待MySQL就绪..."
    sleep 5
done

echo "✅ MySQL已就绪"

# 检查数据库表是否创建成功
echo "🔍 检查数据库表..."
docker exec mysql-db mysql -u root -p${DB_PASSWORD:-cgqlbh114514} -e "USE community; SHOW TABLES;" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ 数据库初始化成功"
else
    echo "❌ 数据库初始化失败"
    exit 1
fi

# 检查Flyway迁移表
echo "🔍 检查Flyway迁移状态..."
docker exec mysql-db mysql -u root -p${DB_PASSWORD:-cgqlbh114514} -e "USE community; SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;" 2>/dev/null

# 启动应用
echo "🚀 启动Spring Boot应用..."
docker-compose up -d hive-app

echo "🎉 所有服务启动完成！"
echo "📊 应用地址: http://localhost:8090"
echo "🗄️  MySQL地址: localhost:3306"
echo "🔴 Redis地址: localhost:6379"
echo "🔍 Elasticsearch地址: http://localhost:9200"

# 显示服务状态
echo "📋 服务状态:"
docker-compose ps
