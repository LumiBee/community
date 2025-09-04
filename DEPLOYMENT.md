# Hive 前后端分离部署指南

## 架构说明

- **前端**: `www.hivelumi.com` (Vercel部署)
- **后端**: `www.hivelumi.com/api` (阿里云服务器)

## 后端配置更新

### 1. 更新CORS配置
已更新 `CorsConfig.java` 允许前端域名访问：
- `https://www.hivelumi.com`
- `https://hivelumi.com`

### 2. 更新文件URL配置
已更新 `application-prod.yml` 中的文件访问URL：
```yaml
file:
  base:
    url: https://www.hivelumi.com/uploads/
```

### 3. 更新OAuth2配置
已更新GitHub OAuth重定向URL：
```yaml
redirect-uri: https://www.hivelumi.com/login/oauth2/code/github
```

## 阿里云服务器配置

### 1. 域名解析
确保DNS解析正确：
```
www.hivelumi.com -> 您的服务器IP
hivelumi.com -> 您的服务器IP (可选，用于重定向)
```

### 2. Nginx配置 (如果使用)
在您的阿里云服务器上配置Nginx：

```nginx
server {
    listen 80;
    listen 443 ssl;
    server_name www.hivelumi.com hivelumi.com;
    
    # SSL证书配置
    ssl_certificate /path/to/your/cert.pem;
    ssl_certificate_key /path/to/your/key.pem;
    
    # API代理
    location /api/ {
        proxy_pass http://localhost:8090/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # CORS头
        add_header Access-Control-Allow-Origin "https://www.hivelumi.com" always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization,X-XSRF-TOKEN" always;
        add_header Access-Control-Allow-Credentials "true" always;
        
        # 处理预检请求
        if ($request_method = 'OPTIONS') {
            add_header Access-Control-Allow-Origin "https://www.hivelumi.com";
            add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
            add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization,X-XSRF-TOKEN";
            add_header Access-Control-Allow-Credentials "true";
            add_header Access-Control-Max-Age 1728000;
            add_header Content-Type 'text/plain; charset=utf-8';
            add_header Content-Length 0;
            return 204;
        }
    }
    
    # 文件访问
    location /uploads/ {
        alias /path/to/your/uploads/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
    
    # 健康检查
    location /health {
        proxy_pass http://localhost:8090/actuator/health;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 3. 重启服务
```bash
# 重启Spring Boot应用
sudo systemctl restart your-hive-service

# 重启Nginx (如果使用)
sudo systemctl restart nginx
```

## 前端配置

### 1. Vercel环境变量
在Vercel项目设置中配置：
```
VITE_API_URL = https://www.hivelumi.com/api
```

### 2. 测试连接
访问以下URL测试：
- 前端: `https://www.hivelumi.com`
- 后端API: `https://www.hivelumi.com/api/health`
- 文件访问: `https://www.hivelumi.com/uploads/`

## 故障排除

### 1. CORS错误
检查后端CORS配置是否正确允许前端域名

### 2. 文件访问404
检查文件上传目录路径和Nginx配置

### 3. OAuth登录失败
检查GitHub OAuth应用的重定向URL配置

## 监控

- 健康检查: `https://www.hivelumi.com/health`
- 应用监控: `https://www.hivelumi.com/actuator/health`
