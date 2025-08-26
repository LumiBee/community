#!/bin/bash

echo "测试 Swagger 接口访问..."

echo "1. 测试 Swagger UI 主页..."
curl -s -o /dev/null -w "Swagger UI 主页: %{http_code}\n" http://localhost:8090/swagger-ui.html

echo "2. 测试 OpenAPI 文档..."
curl -s -o /dev/null -w "OpenAPI 文档: %{http_code}\n" http://localhost:8090/api-docs

echo "3. 测试 Swagger UI 资源..."
curl -s -o /dev/null -w "Swagger UI 资源: %{http_code}\n" http://localhost:8090/swagger-ui/index.html

echo "4. 测试 API 端点..."
curl -s -o /dev/null -w "首页 API: %{http_code}\n" http://localhost:8090/api/home

echo "测试完成！"
echo ""
echo "如果状态码是 200，说明可以正常访问"
echo "如果状态码是 401 或 403，说明仍然被拦截"
echo "如果状态码是 404，说明路径不存在"
