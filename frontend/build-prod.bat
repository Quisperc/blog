@echo off

REM 前端生产环境构建脚本 (Windows)

echo 开始构建生产环境前端应用...

REM 检查.env.production文件是否存在
if not exist ".env.production" (
    echo 错误: 找不到.env.production文件
    echo 请先复制.env.example为.env.production并配置生产环境变量
    pause
    exit /b 1
)

REM 创建临时环境变量文件，确保使用生产环境配置
echo GENERATE_SOURCEMAP=false > .env.local

REM 构建生产环境
npm run build

REM 检查构建是否成功
if %ERRORLEVEL% EQU 0 (
    echo 生产环境构建成功！
    echo 构建文件位于 build/ 目录
    
    REM 提示部署
    echo.
    echo 可以将 build/ 目录的内容部署到Web服务器
) else (
    echo 构建失败，请检查错误信息
    exit /b 1
)

REM 清理临时文件
del .env.local

pause