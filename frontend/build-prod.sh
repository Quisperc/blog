#!/bin/bash

# 前端生产环境构建脚本

echo "开始构建生产环境前端应用..."

# 检查.env.production文件是否存在
if [ ! -f ".env.production" ]; then
    echo "错误: 找不到.env.production文件"
    echo "请先复制.env.example为.env.production并配置生产环境变量"
    exit 1
fi

# 创建临时环境变量文件，确保使用生产环境配置
echo "GENERATE_SOURCEMAP=false" > .env.local

# 构建生产环境
npm run build

# 检查构建是否成功
if [ $? -eq 0 ]; then
    echo "生产环境构建成功！"
    echo "构建文件位于 build/ 目录"
    
    # 显示构建文件大小
    du -sh build/
    
    # 提示部署
    echo ""
    echo "可以将 build/ 目录的内容部署到Web服务器"
else
    echo "构建失败，请检查错误信息"
    exit 1
fi

# 清理临时文件
rm -f .env.local