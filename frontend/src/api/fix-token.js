/**
 * 这个脚本用于修复前端 JWT 认证问题
 * 可以在浏览器控制台中执行
 */

// 1. 手动获取 JWT 令牌
async function getJwtToken() {
  try {
    console.log('正在获取 JWT 令牌...');
    
    const response = await fetch('/api/login', {
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
    
    const data = await response.json();
    
    if (response.ok && data.success) {
      console.log('登录成功，获取到用户信息:', data.user);
      
      // 保存用户信息到 localStorage
      if (data.user && data.user.token) {
        localStorage.setItem('hive_auth_user', JSON.stringify(data.user));
        console.log('用户信息已保存到 localStorage');
        return data.user.token;
      } else {
        console.error('用户信息中没有 token');
        return null;
      }
    } else {
      console.error('登录失败:', data.message || '未知错误');
      return null;
    }
  } catch (error) {
    console.error('获取 JWT 令牌失败:', error);
    return null;
  }
}

// 2. 使用 JWT 令牌发布文章
async function publishArticleWithToken(token) {
  try {
    console.log('正在使用 JWT 令牌发布文章...');
    
    const articleData = {
      title: '测试文章 - JWT 认证',
      content: '这是一篇使用 JWT 认证发布的测试文章',
      excerpt: '测试摘要'
    };
    
    const response = await fetch('/api/article/publish', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(articleData),
      credentials: 'include'
    });
    
    const data = await response.json();
    
    if (response.ok) {
      console.log('文章发布成功:', data);
      return true;
    } else {
      console.error('文章发布失败:', data);
      return false;
    }
  } catch (error) {
    console.error('发布文章失败:', error);
    return false;
  }
}

// 3. 检查 localStorage 中的用户信息
function checkLocalStorage() {
  try {
    console.log('正在检查 localStorage 中的用户信息...');
    
    const authUser = localStorage.getItem('hive_auth_user');
    
    if (authUser) {
      const userData = JSON.parse(authUser);
      console.log('找到用户信息:', userData);
      
      if (userData.token) {
        console.log('找到 JWT 令牌:', userData.token);
        return userData.token;
      } else {
        console.error('用户信息中没有 token');
        return null;
      }
    } else {
      console.error('localStorage 中没有用户信息');
      return null;
    }
  } catch (error) {
    console.error('检查 localStorage 失败:', error);
    return null;
  }
}

// 4. 主函数
async function fixTokenIssue() {
  console.log('开始修复 JWT 认证问题...');
  
  // 先检查 localStorage
  let token = checkLocalStorage();
  
  // 如果没有找到 token，尝试登录获取
  if (!token) {
    token = await getJwtToken();
  }
  
  // 如果有 token，尝试发布文章
  if (token) {
    const result = await publishArticleWithToken(token);
    
    if (result) {
      console.log('问题已修复，文章发布成功！');
    } else {
      console.error('问题未修复，文章发布失败');
    }
  } else {
    console.error('无法获取 JWT 令牌，无法修复问题');
  }
}

// 执行修复
fixTokenIssue();
