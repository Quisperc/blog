# 个人博客前端

这是一个基于React的个人博客前端系统，用于展示博客文章，支持文章浏览、分类展示、标签筛选等功能。

## 功能特点

- 文章列表展示
- 文章详情查看
- 分类浏览和筛选
- 标签浏览和筛选
- 响应式设计，支持移动端
- 文章点赞功能
- 文章浏览量统计

## 技术栈

- React 18
- React Router
- Axios
- CSS

## 项目结构

```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── components/       # 公共组件
│   │   ├── Footer.js
│   │   ├── Header.js
│   │   ├── Layout.js
│   │   ├── PostCard.js
│   │   ├── Error.js
│   │   └── Loading.js
│   ├── pages/           # 页面组件
│   │   ├── Home.js
│   │   ├── PostDetail.js
│   │   ├── Categories.js
│   │   ├── CategoryDetail.js
│   │   ├── Labels.js
│   │   └── LabelDetail.js
│   ├── services/        # API服务
│   │   └── api.js
│   ├── App.js
│   ├── App.css
│   ├── index.css
│   ├── index.js
│   └── setupTests.js
├── package.json
└── README.md
```

## 开发指南

### 环境要求

- Node.js 14+
- npm 或 yarn

### 安装依赖

```bash
cd frontend
npm install
```

### 启动开发服务器

```bash
npm start
```

应用将在 `http://localhost:3000` 上运行。

### 构建生产版本

```bash
npm run build
```

构建的文件将输出到 `build` 目录中。

### 配置后端API地址

在启动应用前，可以通过环境变量配置后端API地址：

```bash
REACT_APP_API_BASE_URL=http://your-backend-url npm start
```

如果不配置，默认使用 `http://localhost:8099`。

## 路由配置

- `/` - 首页，展示文章列表
- `/post/:postId` - 文章详情页
- `/categories` - 分类列表页
- `/category/:categoryId` - 分类详情页
- `/labels` - 标签列表页
- `/label/:labelId` - 标签详情页

## API集成

项目通过 `src/services/api.js` 文件集成后端API，支持以下功能：

- 文章相关：获取文章列表、获取文章详情、按分类/标签筛选文章、文章点赞等
- 分类相关：获取分类列表、获取分类详情等
- 标签相关：获取标签列表、获取标签详情等
- 用户相关：用户注册、登录、登出等
- 文件相关：文件上传、下载等

## 扩展开发

### 添加新页面

1. 在 `src/pages/` 目录下创建新的页面组件
2. 在 `src/App.js` 中添加路由配置

### 添加新API

1. 在 `src/services/api.js` 中添加API方法
2. 在页面组件中调用API方法

### 自定义样式

可以在 `src/index.css` 中添加全局样式，或在组件中添加组件级样式。