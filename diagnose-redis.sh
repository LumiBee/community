#!/bin/bash

echo "=== Redis Connection Diagnosis ==="

echo "1. Checking container status..."
docker-compose ps

echo ""
echo "2. Testing Redis from redis-cache container..."
docker-compose exec redis-cache redis-cli ping

echo ""
echo "3. Checking Redis configuration..."
docker-compose exec redis-cache redis-cli CONFIG GET bind
docker-compose exec redis-cache redis-cli CONFIG GET protected-mode

echo ""
echo "4. Testing Redis connection from hive-app..."
docker-compose exec hive-app sh -c "which nc >/dev/null 2>&1 && echo 'PING' | nc redis-cache 6379 || echo 'nc command not available'"

echo ""
echo "5. Checking network connectivity with nslookup..."
docker-compose exec hive-app sh -c "nslookup redis-cache || echo 'nslookup not available'"

echo ""
echo "6. Recent Redis logs:"
docker-compose logs --tail=20 redis-cache

echo ""
echo "7. Recent application logs:"
docker-compose logs --tail=20 hive-app

echo ""
echo "Diagnosis completed."