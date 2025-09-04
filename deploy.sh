#!/bin/bash

echo "🚀 开始部署Hive应用..."

# 1. 拉取最新代码
echo "📥 拉取最新代码..."
git pull origin main

# 2. 构建项目
echo "🔨 构建项目..."
./mvnw clean package -DskipTests

# 检查Java版本
echo "☕ 检查Java版本..."
java -version

# 3. 停止现有服务
echo "🛑 停止现有服务..."
docker-compose down

# 4. 清理旧镜像
echo "🧹 清理旧镜像..."
docker system prune -f

# 5. 重新构建并启动
echo "🚀 重新构建并启动服务..."
docker-compose up -d --build

# 6. 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 7. 检查服务状态
echo "🔍 检查服务状态..."
docker-compose ps

# 8. 检查健康状态
echo "🏥 检查应用健康状态..."
for i in {1..10}; do
    if curl -f http://localhost:8090/actuator/health > /dev/null 2>&1; then
        echo "✅ 应用启动成功！"
        break
    else
        echo "⏳ 等待应用启动... ($i/10)"
        sleep 10
    fi
done

echo "🎉 部署完成！"
echo "📊 服务访问地址："
echo "   - 应用: http://localhost:8090"
echo "   - 健康检查: http://localhost:8090/actuator/health"
echo "   - MySQL: localhost:3306"
echo "   - Redis: localhost:6379"
echo "   - Elasticsearch: localhost:9200"
