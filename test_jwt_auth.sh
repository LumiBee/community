#!/bin/bash

echo "===== JWT 认证测试 ====="

# 1. 登录并获取 JWT 令牌
echo "1. 登录并获取 JWT 令牌..."
LOGIN_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d '{"account":"lumibee","password":"114514","remember-me":"on"}' http://localhost:8090/api/login)
echo "登录响应: $LOGIN_RESPONSE"

# 从登录响应中提取 JWT 令牌
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | sed 's/"token":"\(.*\)"/\1/')
echo "提取的令牌: $TOKEN"

if [ -z "$TOKEN" ]; then
  echo "未能提取到令牌，请检查登录响应"
  exit 1
fi

# 2. 使用 JWT 令牌访问受保护的 API
echo "2. 使用 JWT 令牌访问发布文章 API..."
PUBLISH_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"测试文章","content":"这是一篇测试文章","excerpt":"测试摘要"}' \
  http://localhost:8090/api/article/publish)

echo "发布文章响应: $PUBLISH_RESPONSE"

# 3. 不使用令牌尝试访问受保护的 API
echo "3. 不使用令牌尝试访问发布文章 API..."
NO_AUTH_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"title":"未认证测试","content":"这是一篇未认证测试文章","excerpt":"测试摘要"}' \
  http://localhost:8090/api/article/publish)

echo "未认证访问响应: $NO_AUTH_RESPONSE"

echo "===== 测试完成 ====="
