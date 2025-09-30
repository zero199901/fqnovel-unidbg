#!/bin/bash

echo "=== Docker部署测试脚本 ==="
echo "端口已从8750更改为8757"
echo ""

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行，请先启动Docker"
    exit 1
fi

echo "✅ Docker正在运行"

# 检查docker-compose.yml语法
echo "🔍 检查docker-compose.yml语法..."
if docker-compose config > /dev/null 2>&1; then
    echo "✅ docker-compose.yml语法正确"
else
    echo "❌ docker-compose.yml语法错误"
    docker-compose config
    exit 1
fi

# 检查端口是否被占用
echo "🔍 检查端口8757是否被占用..."
if lsof -i :8757 > /dev/null 2>&1; then
    echo "⚠️  端口8757已被占用，请先释放端口"
    lsof -i :8757
else
    echo "✅ 端口8757可用"
fi

echo ""
echo "=== 部署命令 ==="
echo "构建并启动所有服务："
echo "  docker-compose up --build -d"
echo ""
echo "查看服务状态："
echo "  docker-compose ps"
echo ""
echo "查看日志："
echo "  docker-compose logs -f"
echo ""
echo "停止服务："
echo "  docker-compose down"
echo ""
echo "访问应用："
echo "  http://localhost:8757"
echo ""
echo "健康检查："
echo "  curl http://localhost:8757/actuator/health"
