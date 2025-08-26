#!/bin/bash

echo "测试 JWT 认证流程..."

# 登录并获取 JWT 令牌
echo "1. 登录并获取 JWT 令牌..."
LOGIN_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d '{"account":"admin","password":"admin123","remember-me":"on"}' http://localhost:8090/api/login)
echo "登录响应: $LOGIN_RESPONSE"

# 从登录响应中提取 JWT 令牌
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | sed 's/"token":"\(.*\)"/\1/')
echo "提取的令牌: $TOKEN"

if [ -z "$TOKEN" ]; then
  echo "未能提取到令牌，请检查登录响应"
  exit 1
fi

# 使用 JWT 令牌访问受保护的 API
echo "2. 使用 JWT 令牌访问发布文章 API..."
curl -s -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d '{"title":"测试文章","content":"这是一篇测试文章"}' http://localhost:8090/api/article/publish

echo "测试完成！"
