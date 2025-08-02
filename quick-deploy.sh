#!/bin/bash

echo "🚀 开始快速部署..."

# 拉取最新代码
echo "📥 拉取代码..."
git pull origin main

# 检查代码是否有变化
if git diff --quiet HEAD~1 HEAD -- src/ pom.xml; then
    echo "📝 代码无变化，仅重启服务..."
    docker-compose restart hive-app
else
    echo "🔨 代码有变化，重新构建..."
    
    # 只停止应用服务，保持数据库等服务运行
    docker-compose stop hive-app
    
    # 构建时使用缓存（移除--no-cache）
    docker-compose build hive-app
    
    # 启动应用服务
    docker-compose up -d hive-app
fi

echo "✅ 部署完成！"

# 显示服务状态
echo "📊 服务状态："
docker-compose ps hive-app