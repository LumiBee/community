#!/bin/bash

echo "===== JWT 认证调试工具 ====="

# 1. 检查后端服务状态
echo "1. 检查后端服务状态..."
BACKEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8090/api/home)
if [ "$BACKEND_STATUS" -eq 200 ]; then
  echo "✅ 后端服务正常运行"
else
  echo "❌ 后端服务异常，状态码: $BACKEND_STATUS"
  echo "请确保后端服务已启动，然后重试"
  exit 1
fi

# 2. 尝试登录获取 JWT 令牌
echo -e "\n2. 尝试登录获取 JWT 令牌..."
LOGIN_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d '{"account":"lumibee","password":"114514","remember-me":"on"}' http://localhost:8090/api/login)
echo "登录响应: $LOGIN_RESPONSE"

# 从登录响应中提取 JWT 令牌
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | sed 's/"token":"\(.*\)"/\1/')
echo "提取的令牌: $TOKEN"

if [ -z "$TOKEN" ]; then
  echo "❌ 未能提取到令牌，请检查登录响应"
  exit 1
else
  echo "✅ 成功提取到 JWT 令牌"
fi

# 3. 使用 JWT 令牌访问受保护的 API
echo -e "\n3. 使用 JWT 令牌访问发布文章 API..."
PUBLISH_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"调试测试文章","content":"这是一篇调试测试文章","excerpt":"调试测试摘要"}' \
  http://localhost:8090/api/article/publish)

echo "发布文章响应: $PUBLISH_RESPONSE"

# 检查发布结果
if [[ $PUBLISH_RESPONSE == *"articleId"* ]]; then
  echo "✅ 文章发布成功"
else
  echo "❌ 文章发布失败"
fi

# 4. 检查请求头传递情况
echo -e "\n4. 检查请求头传递情况..."
HEADERS_RESPONSE=$(curl -s -X GET \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Debug-Headers: true" \
  http://localhost:8090/api/debug/headers)

echo "请求头响应: $HEADERS_RESPONSE"

# 5. 创建前端调试文件
echo -e "\n5. 创建前端调试文件..."
cat > frontend/public/debug-auth.html << EOF
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>JWT 认证调试工具</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
        }
        pre {
            background-color: #f5f5f5;
            padding: 10px;
            border-radius: 5px;
            overflow-x: auto;
        }
        .card {
            border: 1px solid #ddd;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 20px;
        }
        button {
            background-color: #4CAF50;
            color: white;
            padding: 10px 15px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            margin-right: 10px;
            margin-bottom: 10px;
        }
        button:hover {
            background-color: #45a049;
        }
        .success {
            color: green;
        }
        .error {
            color: red;
        }
        .warning {
            color: orange;
        }
    </style>
</head>
<body>
    <h1>JWT 认证调试工具</h1>
    
    <div class="card">
        <h2>1. 检查 localStorage</h2>
        <div id="localStorage-info"></div>
        <pre id="localStorage-content"></pre>
        <button onclick="checkLocalStorage()">检查 localStorage</button>
        <button onclick="clearLocalStorage()">清除 localStorage</button>
    </div>
    
    <div class="card">
        <h2>2. 登录获取 JWT 令牌</h2>
        <div>
            <label for="account">账号:</label>
            <input type="text" id="account" value="lumibee">
        </div>
        <div style="margin-top: 10px;">
            <label for="password">密码:</label>
            <input type="password" id="password" value="114514">
        </div>
        <div style="margin-top: 10px;">
            <label>
                <input type="checkbox" id="remember-me" checked>
                记住我
            </label>
        </div>
        <div style="margin-top: 10px;">
            <button onclick="login()">登录</button>
        </div>
        <div id="login-result"></div>
        <pre id="login-response"></pre>
    </div>
    
    <div class="card">
        <h2>3. 测试受保护的 API</h2>
        <div>
            <button onclick="testApi('/api/portfolios')">GET /api/portfolios</button>
            <button onclick="testApi('/api/user/current')">GET /api/user/current</button>
            <button onclick="publishArticle()">POST /api/article/publish</button>
        </div>
        <div id="api-result"></div>
        <pre id="api-response"></pre>
    </div>
    
    <div class="card">
        <h2>4. 修复工具</h2>
        <div>
            <button onclick="fixTokenIssue()">一键修复 JWT 认证问题</button>
            <button onclick="manuallySetToken()">手动设置 JWT 令牌</button>
        </div>
        <div id="fix-result"></div>
    </div>

    <script>
        // 检查 localStorage
        function checkLocalStorage() {
            const localStorageInfo = document.getElementById('localStorage-info');
            const localStorageContent = document.getElementById('localStorage-content');
            
            const authUserKey = 'hive_auth_user';
            const authUser = localStorage.getItem(authUserKey);
            
            if (authUser) {
                try {
                    const userData = JSON.parse(authUser);
                    localStorageInfo.innerHTML = '<p class="success">✅ 在 localStorage 中找到用户数据</p>';
                    
                    if (userData.token) {
                        localStorageInfo.innerHTML += '<p class="success">✅ 在用户数据中找到 token</p>';
                        
                        // 解析 JWT token
                        try {
                            const tokenParts = userData.token.split('.');
                            if (tokenParts.length === 3) {
                                const header = JSON.parse(atob(tokenParts[0]));
                                const payload = JSON.parse(atob(tokenParts[1]));
                                
                                localStorageInfo.innerHTML += '<p>Token 算法: ' + header.alg + '</p>';
                                localStorageInfo.innerHTML += '<p>用户 ID: ' + payload.sub + '</p>';
                                localStorageInfo.innerHTML += '<p>用户名: ' + payload.name + '</p>';
                                
                                const issuedAt = new Date(payload.iat * 1000);
                                const expiresAt = new Date(payload.exp * 1000);
                                const now = new Date();
                                
                                localStorageInfo.innerHTML += '<p>签发时间: ' + issuedAt.toLocaleString() + '</p>';
                                localStorageInfo.innerHTML += '<p>过期时间: ' + expiresAt.toLocaleString() + '</p>';
                                
                                if (now > expiresAt) {
                                    localStorageInfo.innerHTML += '<p class="error">❌ Token 已过期</p>';
                                } else {
                                    const remainingTime = Math.floor((expiresAt - now) / 1000 / 60);
                                    localStorageInfo.innerHTML += '<p class="success">✅ Token 有效 (剩余 ' + remainingTime + ' 分钟)</p>';
                                }
                            }
                        } catch (e) {
                            localStorageInfo.innerHTML += '<p class="warning">⚠️ 无法解析 JWT token: ' + e.message + '</p>';
                        }
                    } else {
                        localStorageInfo.innerHTML += '<p class="error">❌ 用户数据中没有 token</p>';
                    }
                    
                    localStorageContent.textContent = JSON.stringify(userData, null, 2);
                } catch (e) {
                    localStorageInfo.innerHTML = '<p class="warning">⚠️ 在 localStorage 中找到数据，但无法解析为 JSON: ' + e.message + '</p>';
                    localStorageContent.textContent = authUser;
                }
            } else {
                localStorageInfo.innerHTML = '<p class="error">❌ 在 localStorage 中没有找到用户数据</p>';
                localStorageContent.textContent = '';
            }
        }
        
        // 清除 localStorage
        function clearLocalStorage() {
            localStorage.removeItem('hive_auth_user');
            document.getElementById('localStorage-info').innerHTML = '<p class="success">✅ localStorage 已清除</p>';
            document.getElementById('localStorage-content').textContent = '';
        }
        
        // 登录
        async function login() {
            const loginResult = document.getElementById('login-result');
            const loginResponse = document.getElementById('login-response');
            
            const account = document.getElementById('account').value;
            const password = document.getElementById('password').value;
            const rememberMe = document.getElementById('remember-me').checked;
            
            loginResult.innerHTML = '<p>正在登录...</p>';
            
            try {
                const response = await fetch('/api/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        account: account,
                        password: password,
                        'remember-me': rememberMe ? 'on' : ''
                    }),
                    credentials: 'include'
                });
                
                const data = await response.json();
                
                if (response.ok && data.success) {
                    loginResult.innerHTML = '<p class="success">✅ 登录成功</p>';
                    
                    // 保存用户数据到 localStorage
                    if (data.user) {
                        localStorage.setItem('hive_auth_user', JSON.stringify(data.user));
                        loginResult.innerHTML += '<p class="success">✅ 用户数据已保存到 localStorage</p>';
                        
                        if (data.user.token) {
                            loginResult.innerHTML += '<p class="success">✅ 在用户数据中找到 token</p>';
                        } else {
                            loginResult.innerHTML += '<p class="error">❌ 用户数据中没有 token</p>';
                        }
                    } else {
                        loginResult.innerHTML += '<p class="error">❌ 响应中没有用户数据</p>';
                    }
                    
                    // 设置 temp_session
                    if (!rememberMe) {
                        sessionStorage.setItem('temp_session', 'true');
                        loginResult.innerHTML += '<p>已在 sessionStorage 中设置 temp_session</p>';
                    } else {
                        sessionStorage.removeItem('temp_session');
                        loginResult.innerHTML += '<p>已从 sessionStorage 中移除 temp_session</p>';
                    }
                } else {
                    loginResult.innerHTML = '<p class="error">❌ 登录失败: ' + (data.message || '未知错误') + '</p>';
                }
                
                loginResponse.textContent = JSON.stringify(data, null, 2);
            } catch (error) {
                loginResult.innerHTML = '<p class="error">❌ 错误: ' + error.message + '</p>';
                loginResponse.textContent = error.toString();
            }
        }
        
        // 测试 API
        async function testApi(url) {
            const apiResult = document.getElementById('api-result');
            const apiResponse = document.getElementById('api-response');
            
            apiResult.innerHTML = '<p>正在测试 API: ' + url + '...</p>';
            
            try {
                // 从 localStorage 获取 token
                const authUser = localStorage.getItem('hive_auth_user');
                let token = null;
                
                if (authUser) {
                    try {
                        const userData = JSON.parse(authUser);
                        if (userData.token) {
                            token = userData.token;
                        }
                    } catch (e) {
                        console.error('解析用户数据出错:', e);
                    }
                }
                
                // 使用 token 发送请求
                const headers = {
                    'Content-Type': 'application/json'
                };
                
                if (token) {
                    headers['Authorization'] = 'Bearer ' + token;
                    apiResult.innerHTML += '<p>包含 Authorization 头: Bearer ' + token.substring(0, 20) + '...</p>';
                } else {
                    apiResult.innerHTML += '<p class="warning">⚠️ 没有找到 token，请求中不包含 Authorization 头</p>';
                }
                
                const response = await fetch(url, {
                    method: 'GET',
                    headers: headers,
                    credentials: 'include'
                });
                
                const contentType = response.headers.get('content-type');
                let data;
                
                if (contentType && contentType.includes('application/json')) {
                    data = await response.json();
                } else {
                    data = await response.text();
                }
                
                if (response.ok) {
                    apiResult.innerHTML += '<p class="success">✅ 请求成功 (' + response.status + ')</p>';
                } else {
                    apiResult.innerHTML += '<p class="error">❌ 请求失败 (' + response.status + '): ' + response.statusText + '</p>';
                }
                
                if (typeof data === 'object') {
                    apiResponse.textContent = JSON.stringify(data, null, 2);
                } else {
                    apiResponse.textContent = data;
                }
            } catch (error) {
                apiResult.innerHTML += '<p class="error">❌ 错误: ' + error.message + '</p>';
                apiResponse.textContent = error.toString();
            }
        }
        
        // 发布文章
        async function publishArticle() {
            const apiResult = document.getElementById('api-result');
            const apiResponse = document.getElementById('api-response');
            
            apiResult.innerHTML = '<p>正在发布文章...</p>';
            
            try {
                // 从 localStorage 获取 token
                const authUser = localStorage.getItem('hive_auth_user');
                let token = null;
                
                if (authUser) {
                    try {
                        const userData = JSON.parse(authUser);
                        if (userData.token) {
                            token = userData.token;
                        }
                    } catch (e) {
                        console.error('解析用户数据出错:', e);
                    }
                }
                
                // 使用 token 发送请求
                const headers = {
                    'Content-Type': 'application/json'
                };
                
                if (token) {
                    headers['Authorization'] = 'Bearer ' + token;
                    apiResult.innerHTML += '<p>包含 Authorization 头: Bearer ' + token.substring(0, 20) + '...</p>';
                } else {
                    apiResult.innerHTML += '<p class="warning">⚠️ 没有找到 token，请求中不包含 Authorization 头</p>';
                }
                
                const articleData = {
                    title: '调试测试文章',
                    content: '这是一篇调试测试文章',
                    excerpt: '调试测试摘要'
                };
                
                const response = await fetch('/api/article/publish', {
                    method: 'POST',
                    headers: headers,
                    body: JSON.stringify(articleData),
                    credentials: 'include'
                });
                
                const contentType = response.headers.get('content-type');
                let data;
                
                if (contentType && contentType.includes('application/json')) {
                    data = await response.json();
                } else {
                    data = await response.text();
                }
                
                if (response.ok) {
                    apiResult.innerHTML += '<p class="success">✅ 文章发布成功 (' + response.status + ')</p>';
                } else {
                    apiResult.innerHTML += '<p class="error">❌ 文章发布失败 (' + response.status + '): ' + response.statusText + '</p>';
                }
                
                if (typeof data === 'object') {
                    apiResponse.textContent = JSON.stringify(data, null, 2);
                } else {
                    apiResponse.textContent = data;
                }
            } catch (error) {
                apiResult.innerHTML += '<p class="error">❌ 错误: ' + error.message + '</p>';
                apiResponse.textContent = error.toString();
            }
        }
        
        // 一键修复 JWT 认证问题
        async function fixTokenIssue() {
            const fixResult = document.getElementById('fix-result');
            
            fixResult.innerHTML = '<p>正在修复 JWT 认证问题...</p>';
            
            try {
                // 1. 登录获取新的 JWT 令牌
                fixResult.innerHTML += '<p>1. 正在登录获取新的 JWT 令牌...</p>';
                
                const loginResponse = await fetch('/api/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        account: 'lumibee',
                        password: '114514',
                        'remember-me': 'on'
                    }),
                    credentials: 'include'
                });
                
                const loginData = await loginResponse.json();
                
                if (loginResponse.ok && loginData.success && loginData.user && loginData.user.token) {
                    fixResult.innerHTML += '<p class="success">✅ 登录成功，获取到 JWT 令牌</p>';
                    
                    // 2. 保存用户数据到 localStorage
                    localStorage.setItem('hive_auth_user', JSON.stringify(loginData.user));
                    fixResult.innerHTML += '<p class="success">✅ 用户数据已保存到 localStorage</p>';
                    
                    // 3. 测试发布文章
                    fixResult.innerHTML += '<p>3. 正在测试发布文章...</p>';
                    
                    const articleData = {
                        title: '修复测试文章',
                        content: '这是一篇修复测试文章',
                        excerpt: '修复测试摘要'
                    };
                    
                    const publishResponse = await fetch('/api/article/publish', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': 'Bearer ' + loginData.user.token
                        },
                        body: JSON.stringify(articleData),
                        credentials: 'include'
                    });
                    
                    const publishData = await publishResponse.json();
                    
                    if (publishResponse.ok) {
                        fixResult.innerHTML += '<p class="success">✅ 文章发布成功，JWT 认证问题已修复</p>';
                    } else {
                        fixResult.innerHTML += '<p class="error">❌ 文章发布失败，JWT 认证问题未修复</p>';
                    }
                } else {
                    fixResult.innerHTML += '<p class="error">❌ 登录失败，无法获取 JWT 令牌</p>';
                }
            } catch (error) {
                fixResult.innerHTML += '<p class="error">❌ 修复过程中出错: ' + error.message + '</p>';
            }
        }
        
        // 手动设置 JWT 令牌
        function manuallySetToken() {
            const token = prompt('请输入 JWT 令牌:');
            
            if (token) {
                const fixResult = document.getElementById('fix-result');
                
                try {
                    // 从 localStorage 获取用户数据
                    const authUser = localStorage.getItem('hive_auth_user');
                    let userData = {};
                    
                    if (authUser) {
                        try {
                            userData = JSON.parse(authUser);
                        } catch (e) {
                            console.error('解析用户数据出错:', e);
                        }
                    }
                    
                    // 设置 token
                    userData.token = token;
                    
                    // 保存回 localStorage
                    localStorage.setItem('hive_auth_user', JSON.stringify(userData));
                    
                    fixResult.innerHTML = '<p class="success">✅ JWT 令牌已手动设置</p>';
                    
                    // 解析 JWT token
                    try {
                        const tokenParts = token.split('.');
                        if (tokenParts.length === 3) {
                            const header = JSON.parse(atob(tokenParts[0]));
                            const payload = JSON.parse(atob(tokenParts[1]));
                            
                            fixResult.innerHTML += '<p>Token 算法: ' + header.alg + '</p>';
                            fixResult.innerHTML += '<p>用户 ID: ' + payload.sub + '</p>';
                            fixResult.innerHTML += '<p>用户名: ' + payload.name + '</p>';
                            
                            const issuedAt = new Date(payload.iat * 1000);
                            const expiresAt = new Date(payload.exp * 1000);
                            
                            fixResult.innerHTML += '<p>签发时间: ' + issuedAt.toLocaleString() + '</p>';
                            fixResult.innerHTML += '<p>过期时间: ' + expiresAt.toLocaleString() + '</p>';
                        }
                    } catch (e) {
                        fixResult.innerHTML += '<p class="warning">⚠️ 无法解析 JWT token: ' + e.message + '</p>';
                    }
                } catch (e) {
                    fixResult.innerHTML = '<p class="error">❌ 设置 JWT 令牌失败: ' + e.message + '</p>';
                }
            }
        }
        
        // 页面加载时检查 localStorage
        checkLocalStorage();
    </script>
</body>
</html>
EOF

echo "✅ 前端调试文件已创建: frontend/public/debug-auth.html"
echo "请在浏览器中访问: http://localhost:8090/debug-auth.html"

echo -e "\n===== 调试完成 ====="
