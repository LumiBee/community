# Hive Frontend

这是 LumiHive 博客平台的 Vue.js 前端项目，使用 Vue 3 + Vite + Pinia + Axios 构建。

## 🚀 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **UI框架**: Bootstrap 5
- **图标**: Font Awesome 6
- **样式预处理**: Sass
- **Markdown渲染**: Marked + DOMPurify
- **动画**: AOS (Animate On Scroll)

## 📁 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API 接口
│   ├── assets/            # 资源文件
│   │   ├── images/        # 图片
│   │   └── styles/        # 样式文件
│   ├── components/        # 组件
│   │   └── layout/        # 布局组件
│   ├── router/            # 路由配置
│   ├── store/             # 状态管理
│   ├── utils/             # 工具函数
│   └── views/             # 页面组件
│       ├── auth/          # 认证相关页面
│       └── error/         # 错误页面
├── package.json
├── vite.config.js
└── README.md
```

## 🛠️ 开发指南

### 安装依赖

```bash
cd frontend
npm install
```

### 启动开发服务器

```bash
npm run dev
```

项目将在 `http://localhost:3000` 启动。

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
```

## 📄 页面列表

- **首页** (`/`) - 文章列表、轮播图、热门文章、标签云
- **登录** (`/login`) - 用户登录，支持OAuth2
- **注册** (`/signup`) - 用户注册
- **文章详情** (`/article/:slug`) - 文章内容展示
- **发布文章** (`/publish`) - 文章发布和编辑
- **个人中心** (`/profile`) - 用户个人资料
- **作品集** (`/portfolio`) - 作品集展示
- **收藏夹** (`/favorites`) - 用户收藏
- **标签** (`/tags`) - 标签云
- **搜索** (`/search`) - 搜索结果
- **设置** (`/settings`) - 用户设置
- **草稿箱** (`/drafts`) - 草稿管理
- **私信** (`/messages`) - 消息中心

## 🔧 配置说明

### API代理配置

项目已配置 Vite 代理，将以下路径代理到后端服务器：

- `/api/*` → `http://localhost:8090`
- `/oauth2/*` → `http://localhost:8090`
- `/login-process` → `http://localhost:8090`
- `/logout` → `http://localhost:8090`
- `/uploads/*` → `http://localhost:8090`

### 环境变量

创建 `.env` 文件配置环境变量：

```env
VITE_API_BASE_URL=http://localhost:8090
```

## 🎨 主要功能

### 认证系统
- 用户登录/注册
- OAuth2 登录 (GitHub)
- JWT token 管理
- 路由守卫

### 文章系统
- 文章列表展示
- Markdown 渲染
- 文章点赞/收藏
- 实时搜索
- 相关文章推荐

### 用户交互
- 关注系统
- 评论系统（待实现）
- 个人资料管理
- 收藏夹管理

### 响应式设计
- 移动端适配
- Bootstrap 响应式网格
- 触摸友好的交互

## 🚨 注意事项

1. **后端依赖**: 前端需要后端 API 服务运行在 `http://localhost:8090`
2. **图片资源**: 需要将原项目的图片资源复制到 `public/img/` 目录
3. **CORS配置**: 确保后端正确配置 CORS 以允许前端访问
4. **认证**: 登录状态通过 Cookie 维护，需要后端支持

## 📝 待完善功能

- [ ] 富文本编辑器 (发布文章页面)
- [ ] 评论系统
- [ ] 文件上传功能
- [ ] PWA 支持
- [ ] 国际化 (i18n)
- [ ] 单元测试
- [ ] E2E 测试

## 🤝 开发规范

- 使用 ESLint + Prettier 进行代码格式化
- 组件命名使用 PascalCase
- 文件命名使用 kebab-case
- 提交信息遵循 Conventional Commits

## 🔍 调试技巧

1. 使用 Vue DevTools 浏览器扩展
2. 查看网络请求在开发者工具中的状态
3. 使用 `console.log` 调试 API 调用
4. 检查 Pinia store 状态变化

## 📚 学习资源

- [Vue 3 官方文档](https://vuejs.org/)
- [Vite 文档](https://vitejs.dev/)
- [Pinia 文档](https://pinia.vuejs.org/)
- [Vue Router 文档](https://router.vuejs.org/)
- [Bootstrap 5 文档](https://getbootstrap.com/)

## 📞 联系方式

如有问题，请联系开发团队或提交 Issue。
