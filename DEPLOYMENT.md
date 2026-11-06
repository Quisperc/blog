# 生产环境部署指南

本文档提供了将博客系统部署到生产环境的具体步骤和注意事项。

## 打包应用

1. 在开发环境中，确保项目代码已提交并推送到版本控制系统

2. 使用Maven打包项目：

```bash
mvn clean package -DskipTests
```

3. 打包完成后，会在`target`目录下生成`blog-0.0.1-SNAPSHOT.jar`文件

## 服务器准备

### 系统要求

- Linux服务器（推荐Ubuntu 20.04+）
- Java 17+ 运行环境
- MySQL 8.0+ 数据库
- Redis 6.0+ 缓存服务
- 至少2GB内存，20GB存储空间

### 安装Java 17

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk java-17-openjdk-devel

# 验证安装
java -version
```

## 部署步骤

### 1. 上传文件

将以下文件上传到服务器（例如`/opt/blog`目录）：

- `blog-0.0.1-SNAPSHOT.jar`（应用JAR文件）
- `start.sh`（启动脚本）
- `.env.example`（环境变量示例）

### 2. 配置环境变量

1. 复制环境变量示例文件：

```bash
cp .env.example .env
```

2. 编辑`.env`文件，配置生产环境参数：

```bash
nano .env
```

重要配置项：

```bash
# 服务器端口
SERVER_PORT=8080

# 启用生产环境配置
SPRING_PROFILES_ACTIVE=prod

# 数据库连接（使用实际的数据库地址和凭据）
DATABASE_URL=jdbc:mysql://db-host:3306/blog?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8
DATABASE_USERNAME=blog_user
DATABASE_PASSWORD=secure_password

# Redis连接
REDIS_HOST=redis-host
REDIS_PORT=6379
REDIS_PASSWORD=redis_password

# JWT密钥（请使用强密钥）
JWT_SECRET=your-strong-jwt-secret-key-for-production

# 华为云OBS配置（如使用）
OBS_ENDPOINT=https://obs.cn-east-3.myhuaweicloud.com
OBS_ACCESS_KEY=your-obs-access-key
OBS_SECRET_ACCESS_KEY=your-obs-secret-key
OBS_BUCKET_NAME=your-production-bucket
OBS_ACCESS_URL=https://your-bucket-name.obs.cn-east-3.myhuaweicloud.com
```

### 3. 设置执行权限

```bash
chmod +x start.sh
```

### 4. 测试启动

```bash
./start.sh
```

检查应用是否正常启动，可以通过以下方式验证：

```bash
# 检查进程
ps aux | grep blog

# 检查端口监听
netstat -tlnp | grep 8080

# 查看日志（如果有重定向日志）
tail -f nohup.out
```

### 5. 设置为系统服务（可选）

创建systemd服务文件：

```bash
sudo nano /etc/systemd/system/blog.service
```

内容如下：

```ini
[Unit]
Description=Blog Application
After=network.target

[Service]
Type=simple
User=blog
WorkingDirectory=/opt/blog
ExecStart=/usr/bin/java -jar /opt/blog/blog-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=blog

[Install]
WantedBy=multi-user.target
```

启用并启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable blog
sudo systemctl start blog
```

查看服务状态：

```bash
sudo systemctl status blog
```

## 使用Nginx反向代理（推荐）

安装Nginx：

```bash
sudo apt install nginx
```

创建Nginx配置：

```bash
sudo nano /etc/nginx/sites-available/blog
```

配置内容：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

启用配置：

```bash
sudo ln -s /etc/nginx/sites-available/blog /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

## 安全建议

1. **防火墙配置**：
   ```bash
   sudo ufw allow 22/tcp
   sudo ufw allow 80/tcp
   sudo ufw allow 443/tcp
   sudo ufw enable
   ```

2. **使用HTTPS**：
   配置SSL证书（可使用Let's Encrypt免费证书）

3. **定期备份**：
   - 数据库备份
   - 文件存储备份
   - 配置文件备份

4. **监控日志**：
   设置日志轮转和监控

## 故障排除

### 常见问题

1. **应用无法启动**：
   - 检查Java版本是否正确
   - 检查环境变量配置是否正确
   - 查看错误日志

2. **数据库连接失败**：
   - 确认数据库服务运行状态
   - 检查连接参数和网络连接
   - 验证数据库用户权限

3. **端口占用**：
   ```bash
   # 查找占用端口的进程
   sudo lsof -i :8080
   # 修改.env文件中的SERVER_PORT
   ```

## 更新部署

当需要更新应用时：

1. 备份当前版本和配置

2. 停止应用：
   ```bash
   sudo systemctl stop blog
   ```

3. 替换JAR文件

4. 重启应用：
   ```bash
   sudo systemctl start blog
   ```

5. 验证更新是否成功