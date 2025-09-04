# Docker 部署完整流程

## 🚀 阿里云服务器部署步骤

### 1. 登录服务器并拉取代码
```bash
# 登录到您的阿里云服务器
ssh root@your-server-ip

# 进入项目目录
cd /path/to/your/hive-project

# 拉取最新代码
git pull origin main
```

### 2. 构建项目
```bash
# 使用Maven构建项目
./mvnw clean package -DskipTests

# 或者如果服务器上没有Maven wrapper
mvn clean package -DskipTests
```

### 3. 停止现有服务
```bash
# 停止所有Docker容器
docker-compose down

# 清理旧镜像（可选）
docker system prune -f
```

### 4. 重新构建并启动服务
```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps
```

### 5. 查看日志
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f hive-app
docker-compose logs -f mysql-db
docker-compose logs -f redis-cache
docker-compose logs -f elasticsearch-search
```

## 📋 完整的一键部署脚本

创建一个部署脚本 `deploy.sh`：

```bash
#!/bin/bash

echo "🚀 开始部署Hive应用..."

# 1. 拉取最新代码
echo "📥 拉取最新代码..."
git pull origin main

# 2. 构建项目
echo "🔨 构建项目..."
./mvnw clean package -DskipTests

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
```

## 🔧 常用Docker命令

### 服务管理
```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart hive-app

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f [service-name]
```

### 数据库管理
```bash
# 进入MySQL容器
docker exec -it mysql-db mysql -u root -p

# 进入Redis容器
docker exec -it redis-cache redis-cli

# 备份MySQL数据
docker exec mysql-db mysqldump -u root -p community > backup.sql

# 恢复MySQL数据
docker exec -i mysql-db mysql -u root -p community < backup.sql
```

### 应用管理
```bash
# 进入应用容器
docker exec -it hive-app bash

# 查看应用日志
docker logs -f hive-app

# 重启应用
docker restart hive-app
```

## 🐛 故障排除

### 1. 端口冲突
```bash
# 检查端口占用
netstat -tulpn | grep :8090
netstat -tulpn | grep :3306
netstat -tulpn | grep :6379
netstat -tulpn | grep :9200

# 杀死占用端口的进程
sudo kill -9 <PID>
```

### 2. 容器启动失败
```bash
# 查看容器日志
docker logs <container-name>

# 查看容器状态
docker ps -a

# 重新创建容器
docker-compose up -d --force-recreate
```

### 3. 数据库连接问题
```bash
# 检查数据库容器状态
docker-compose logs mysql-db

# 测试数据库连接
docker exec -it mysql-db mysql -u root -p -e "SHOW DATABASES;"
```

### 4. 内存不足
```bash
# 检查系统资源
free -h
df -h

# 清理Docker资源
docker system prune -a
```

## 📊 监控和维护

### 1. 健康检查
```bash
# 应用健康检查
curl http://localhost:8090/actuator/health

# 数据库健康检查
docker exec mysql-db mysqladmin ping -h localhost -u root -p

# Redis健康检查
docker exec redis-cache redis-cli ping
```

### 2. 性能监控
```bash
# 查看容器资源使用
docker stats

# 查看系统资源
htop
```

### 3. 日志管理
```bash
# 查看应用日志
docker-compose logs --tail=100 hive-app

# 清理旧日志
docker system prune -f
```

## 🔄 自动化部署

### 使用GitHub Actions（可选）
创建 `.github/workflows/deploy.yml`：

```yaml
name: Deploy to Server

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
    - name: Deploy to server
      uses: appleboy/ssh-action@v0.1.5
      with:
        host: ${{ secrets.HOST }}
        username: ${{ secrets.USERNAME }}
        key: ${{ secrets.SSH_KEY }}
        script: |
          cd /path/to/your/hive-project
          git pull origin main
          ./mvnw clean package -DskipTests
          docker-compose down
          docker-compose up -d --build
```

这样您就可以通过 `git pull` 后运行 `./deploy.sh` 来完成整个部署流程了！
