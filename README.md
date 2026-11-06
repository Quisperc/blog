# Blog项目

这是一个基于Spring Boot和React的博客系统项目。

## 环境要求

- Java 17+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+

## 快速开始

### 后端启动

#### 开发环境启动

1. 在项目根目录下，复制 `.env.example` 文件为 `.env`，并根据实际情况修改配置：

```bash
cp .env.example .env
```

2. 配置数据库连接和其他必要的环境变量：

```bash
# 数据库配置
DATABASE_URL=jdbc:mysql://localhost:3306/blog?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8
DATABASE_USERNAME=root
DATABASE_PASSWORD=your_password

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT配置
JWT_SECRET=your-jwt-secret-key

# 华为云OBS配置 (可选)
OBS_ENDPOINT=https://obs.cn-east-3.myhuaweicloud.com
OBS_ACCESS_KEY=your-access-key
OBS_SECRET_ACCESS_KEY=your-secret-access-key
OBS_BUCKET_NAME=your-bucket-name
OBS_ACCESS_URL=https://your-bucket-name.obs.cn-east-3.myhuaweicloud.com
```

3. 使用Maven编译并运行后端：

```bash
mvn clean package
java -jar target/blog-0.0.1-SNAPSHOT.jar
```

#### 生产环境部署（使用JAR包）

1. 使用Maven打包项目：

```bash
mvn clean package -DskipTests
```

2. 将打包好的JAR文件（`target/blog-0.0.1-SNAPSHOT.jar`）复制到目标服务器

3. 在JAR文件同一目录下，创建`.env`文件并配置生产环境变量：

```bash
# 生产环境配置示例
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# 数据库配置
DATABASE_URL=jdbc:mysql://your-production-db-host:3306/blog?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8
DATABASE_USERNAME=prod_user
DATABASE_PASSWORD=strong_password

# Redis配置
REDIS_HOST=your-redis-host
REDIS_PORT=6379
REDIS_PASSWORD=redis_password

# JWT配置
JWT_SECRET=strong-jwt-secret-key

# 华为云OBS配置
OBS_ENDPOINT=https://obs.cn-east-3.myhuaweicloud.com
OBS_ACCESS_KEY=your-production-access-key
OBS_SECRET_ACCESS_KEY=your-production-secret-key
OBS_BUCKET_NAME=your-production-bucket
OBS_ACCESS_URL=https://your-bucket-name.obs.cn-east-3.myhuaweicloud.com
```

4. 运行JAR文件：

```bash
java -jar blog-0.0.1-SNAPSHOT.jar
```

**注意**：
- 确保`.env`文件与JAR文件在同一目录下
- JAR启动时会自动加载同目录下的`.env`文件
- 如果没有`.env`文件，系统将使用默认配置或通过命令行参数指定的配置

#### 替代方案：通过命令行参数

如果不使用`.env`文件，也可以通过命令行参数传递配置：

```bash
java -jar blog-0.0.1-SNAPSHOT.jar \
  --SERVER_PORT=8080 \
  --SPRING_PROFILES_ACTIVE=prod \
  --DATABASE_URL=jdbc:mysql://your-production-db-host:3306/blog \
  --DATABASE_USERNAME=prod_user \
  --DATABASE_PASSWORD=strong_password
```

### 前端启动

1. 进入前端目录：

```bash
cd frontend
```

2. 复制 `.env.example` 文件为 `.env.development`（开发环境）或 `.env.production`（生产环境）：

```bash
cp .env.example .env.development
```

3. 根据实际情况修改API基础URL：

```bash
# 开发环境
REACT_APP_API_BASE_URL=http://localhost:8099

# 生产环境
REACT_APP_API_BASE_URL=https://your-production-api-url.com
```

4. 安装依赖并运行前端：

```bash
npm install
npm start
```

## 项目结构

```
blog/
├── src/                    # 后端源代码
│   ├── main/
│   │   ├── java/
│   │   └── resources/      # 配置文件
│   └── test/
├── frontend/               # 前端源代码
│   ├── public/
│   └── src/
├── .env.example           # 后端环境变量示例
└── pom.xml                # Maven配置文件
```

## 功能特性

- 用户认证与授权
- 文章管理（CRUD操作）
- 分类和标签管理
- 文件上传与管理
- 图片库展示
- 评论系统
- 搜索功能

## API文档

开发环境下，可以通过以下URL访问API文档：
- Swagger UI: http://localhost:8099/swagger-ui.html
- OpenAPI JSON: http://localhost:8099/v3/api-docs

## 注意事项

1. 确保数据库和Redis服务已启动
2. 首次运行前需要创建数据库并导入必要的表结构
3. 生产环境中请使用HTTPS协议
4. 定期备份数据库和文件存储

## 许可证

MIT License