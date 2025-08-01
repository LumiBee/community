#!/bin/bash

echo "Testing Redis connection in Docker environment..."

# 检查Docker容器状态
echo "=== Docker Container Status ==="
docker-compose ps

echo ""
echo "=== Testing Redis Connection ==="

# 测试Redis连接
docker-compose exec redis-cache redis-cli ping

echo ""
echo "=== Redis Configuration Test ==="

# 验证Redis配置
docker-compose exec redis-cache redis-cli CONFIG GET bind
docker-compose exec redis-cache redis-cli CONFIG GET port
docker-compose exec redis-cache redis-cli CONFIG GET protected-mode

echo ""
echo "=== Network Connectivity Test ==="

# 从应用容器测试Redis连接
echo "Testing ping from hive-app to redis-cache..."
docker-compose exec hive-app ping -c 3 redis-cache

echo ""
echo "Testing telnet connection..."
docker-compose exec hive-app sh -c "echo 'PING' | nc redis-cache 6379"

echo ""
echo "Testing DNS resolution..."
docker-compose exec hive-app nslookup redis-cache

echo ""
echo "=== Redis Logs Check ==="
echo "Recent Redis container logs:"
docker-compose logs --tail=10 redis-cache

echo ""
echo "Test completed."