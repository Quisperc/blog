#!/bin/bash

# 前端环境配置脚本

echo "前端环境配置助手"
echo "=================="
echo

# 检查是否存在.env.example文件
if [ ! -f ".env.example" ]; then
    echo "错误: 找不到.env.example文件"
    exit 1
fi

# 显示可用环境选项
echo "请选择要配置的环境:"
echo "1) 开发环境 (development)"
echo "2) 生产环境 (production)"
echo "3) 测试环境 (test)"
echo

read -p "请输入选项 (1-3): " choice

case $choice in
    1)
        echo "配置开发环境..."
        if [ ! -f ".env.development" ]; then
            cp .env.example .env.development
        fi
        echo "已创建/更新 .env.development 文件"
        echo "请编辑 .env.development 文件配置开发环境参数"
        ;;
    2)
        echo "配置生产环境..."
        if [ ! -f ".env.production.template" ]; then
            echo "错误: 找不到 .env.production.template 文件"
            exit 1
        fi
        if [ ! -f ".env.production" ]; then
            cp .env.production.template .env.production
        fi
        echo "已创建/更新 .env.production 文件"
        echo "请编辑 .env.production 文件配置生产环境参数"
        ;;
    3)
        echo "配置测试环境..."
        if [ ! -f ".env.test" ]; then
            cp .env.example .env.test
        fi
        echo "已创建/更新 .env.test 文件"
        echo "请编辑 .env.test 文件配置测试环境参数"
        ;;
    *)
        echo "无效选项"
        exit 1
        ;;
esac

echo
echo "配置完成后，可以使用以下命令启动应用:"
echo "- 开发环境: npm start"
echo "- 生产构建: npm run build:prod"