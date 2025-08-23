# 🚀 快速启动指南

## 前置条件

确保您的系统已安装：
- Node.js 16+ 
- npm 或 yarn
- Java 后端服务运行在 localhost:8090

## 第一次运行

### 1. 安装依赖
```bash
cd frontend
npm install
```

### 2. 启动开发服务器
```bash
npm run dev
```

### 3. 访问应用
打开浏览器访问: http://localhost:3000

## 🔧 开发命令

```bash
# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview

# 代码检查
npm run lint
```

## 📁 重要文件

- `src/main.js` - 应用入口
- `src/App.vue` - 根组件
- `src/router/index.js` - 路由配置
- `src/store/auth.js` - 认证状态管理
- `src/api/index.js` - API服务

## 🛠️ 开发流程

1. **添加新页面**:
   - 在 `src/views/` 创建组件
   - 在 `src/router/index.js` 添加路由

2. **添加新API**:
   - 在 `src/api/index.js` 添加API方法

3. **添加新组件**:
   - 在 `src/components/` 创建组件

## 📱 移动端测试

```bash
# 获取本机IP
npm run dev -- --host

# 然后使用手机访问 http://your-ip:3000
```

## 🔍 调试技巧

1. 安装 Vue DevTools 浏览器扩展
2. 打开浏览器开发者工具查看网络请求
3. 检查控制台输出

## ⚠️ 常见问题

**Q: API请求失败怎么办？**
A: 确保后端服务运行在 localhost:8090，检查CORS配置

**Q: 页面空白怎么办？**
A: 检查控制台错误信息，通常是组件导入问题

**Q: 样式不生效怎么办？**
A: 确保Bootstrap和自定义样式正确导入

## 📞 获取帮助

- 查看 README.md 了解详细信息
- 查看 MIGRATION_SUMMARY.md 了解项目架构
- 检查代码注释获取技术细节
