#!/bin/bash

echo "🧪 测试CORS配置..."
echo "=================="

# 测试OPTIONS预检请求
echo "1. 测试OPTIONS预检请求..."
curl -X OPTIONS "http://localhost:8090/api/profile/LumiBee" \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v

echo -e "\n\n2. 测试GET请求..."
curl -X GET "http://localhost:8090/api/profile/LumiBee" \
  -H "Origin: http://localhost:3000" \
  -v

echo -e "\n\n✅ CORS测试完成！"
echo "如果看到正确的CORS头部和200状态码，说明配置成功。"
