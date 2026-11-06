# 前端部署指南

本文档介绍如何构建和部署前端应用到生产环境。

## 环境变量配置

React项目使用.env文件来管理环境变量，根据不同环境会自动加载对应的配置文件：

- `.env.development` - 开发环境 (npm start时加载)
- `.env.production` - 生产环境 (npm run build时加载)
- `.env.local` - 本地覆盖配置 (会覆盖上述文件中的同名变量)
- `.env` - 通用配置 (所有环境都会加载)

### 配置环境变量

1. 复制模板文件创建生产环境配置：

```bash
# Linux/Mac
cp .env.production.template .env.production

# Windows
copy .env.production.template .env.production
```

2. 编辑 `.env.production` 文件，配置生产环境参数：

```bash
# API服务器地址 (必需)
REACT_APP_API_BASE_URL=https://your-production-api-url.com

# 其他配置...
REACT_APP_TITLE=博客系统
REACT_APP_DEBUG=false
GENERATE_SOURCEMAP=false
```

## 构建生产版本

### 方法1：使用脚本 (推荐)

提供了两个脚本文件，根据系统选择：

- Linux/Mac: `build-prod.sh`
- Windows: `build-prod.bat`

执行脚本：

```bash
# Linux/Mac
./build-prod.sh

# Windows
build-prod.bat

# 或者使用npm脚本
npm run build:prod  # Linux/Mac
npm run build:prod:windows  # Windows
```

### 方法2：直接使用npm命令

```bash
npm run build
```

### 构建结果

构建完成后，会在项目根目录下生成 `build/` 文件夹，包含优化后的生产环境代码。

## 部署到Web服务器

### 1. 传统Web服务器部署

将 `build/` 目录中的所有文件复制到Web服务器的根目录：

```bash
# 示例：部署到Nginx
sudo cp -r build/* /var/www/html/

# 示例：部署到Apache
sudo cp -r build/* /var/www/html/
```

### 2. 使用Nginx配置

创建Nginx配置文件：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /var/www/html;
    index index.html;

    # 处理React Router
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 静态资源缓存
    location /static/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # API代理 (可选)
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 3. 使用Docker部署

创建 `Dockerfile`:

```dockerfile
# 构建阶段
FROM node:16 as builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

# 生产阶段
FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

构建和运行Docker容器：

```bash
docker build -t blog-frontend .
docker run -p 80:80 blog-frontend
```

### 4. 部署到CDN或云存储

可以将构建后的文件上传到云存储服务：

- AWS S3 + CloudFront
- 阿里云OSS + CDN
- 腾讯云COS + CDN

## 配置HTTPS

### 使用Let's Encrypt免费证书

```bash
# 安装certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo crontab -e
# 添加以下行
0 12 * * * /usr/bin/certbot renew --quiet
```

## 性能优化

### 1. 启用Gzip压缩

在Nginx配置中添加：

```nginx
gzip on;
gzip_vary on;
gzip_min_length 1024;
gzip_proxied any;
gzip_comp_level 6;
gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
```

### 2. 设置浏览器缓存

```nginx
# 静态资源缓存
location ~* \.(?:css|js)$ {
    expires 1y;
    add_header Cache-Control "public";
}

# 图片缓存
location ~* \.(?:jpg|jpeg|gif|png|ico|cur|gz|svg|svgz|mp4|ogg|ogv|webm|htc)$ {
    expires 1M;
    add_header Cache-Control "public";
}
```

### 3. 启用HTTP/2

在Nginx配置中：

```nginx
listen 443 ssl http2;
```

## 监控和日志

### 1. 设置访问日志

在Nginx配置中：

```nginx
access_log /var/log/nginx/blog.access.log;
error_log /var/log/nginx/blog.error.log;
```

### 2. 使用Google Analytics或其他分析工具

在 `public/index.html` 中添加分析代码：

```html
<!-- Google Analytics -->
<script async src="https://www.googletagmanager.com/gtag/js?id=GA_MEASUREMENT_ID"></script>
<script>
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());
  gtag('config', 'GA_MEASUREMENT_ID');
</script>
```

## 故障排除

### 常见问题

1. **页面刷新后404错误**：
   - 确保Web服务器配置了回退到index.html
   - 对于Nginx：`try_files $uri $uri/ /index.html;`

2. **API请求失败**：
   - 检查`.env.production`中的API地址是否正确
   - 确保CORS配置正确

3. **资源加载失败**：
   - 检查文件路径是否正确
   - 确保服务器配置了正确的MIME类型

4. **部署后仍显示开发环境内容**：
   - 确认使用了`npm run build`而不是`npm start`
   - 检查是否有缓存问题，尝试清除浏览器缓存

## 更新部署流程

当需要更新前端应用时：

1. 更新代码并提交到版本控制

2. 拉取最新代码：

```bash
git pull origin main
```

3. 更新依赖（如有变更）：

```bash
npm install
```

4. 构建生产版本：

```bash
npm run build:prod
```

5. 部署新版本：

```bash
# 备份当前版本
sudo mv /var/www/html /var/www/html.backup

# 部署新版本
sudo cp -r build/* /var/www/html/

# 重启Web服务器
sudo systemctl reload nginx
```

6. 验证部署是否成功

7. 如有问题，可以快速回滚：

```bash
# 回滚到备份版本
sudo rm -rf /var/www/html
sudo mv /var/www/html.backup /var/www/html
sudo systemctl reload nginx
```