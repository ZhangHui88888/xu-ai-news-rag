#!/bin/bash

# XU-News-AI-RAG 一键构建部署脚本（适用于 VMware 环境）
# 用途：在虚拟机中构建 Maven 项目，然后启动 Docker 服务

set -e

echo "=========================================="
echo "   XU-News-AI-RAG 一键构建部署脚本"
echo "=========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查依赖
echo "✓ 1. 检查依赖..."

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}✗ 错误: 未找到 Maven${NC}"
    echo "  请安装 Maven: sudo apt install maven"
    exit 1
fi
echo -e "${GREEN}  ✓ Maven 已安装: $(mvn -version | head -n 1)${NC}"

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}✗ 错误: 未找到 Docker${NC}"
    echo "  请安装 Docker"
    exit 1
fi
echo -e "${GREEN}  ✓ Docker 已安装: $(docker --version)${NC}"

# 检查 Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}✗ 错误: 未找到 docker-compose${NC}"
    echo "  请安装 docker-compose"
    exit 1
fi
echo -e "${GREEN}  ✓ docker-compose 已安装: $(docker-compose --version)${NC}"

echo ""
echo "✓ 2. 构建后端项目..."
cd backend

# 清理旧构建
echo "  清理旧的构建产物..."
mvn clean > /dev/null 2>&1

# 构建项目
echo "  编译项目（跳过测试）..."
mvn package -DskipTests -B

# 检查构建结果
if [ ! -f target/*.jar ]; then
    echo -e "${RED}✗ 构建失败: jar 文件未生成${NC}"
    exit 1
fi

JAR_FILE=$(ls target/*.jar)
JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
echo -e "${GREEN}  ✓ 构建成功: $JAR_FILE ($JAR_SIZE)${NC}"

cd ..

echo ""
echo "✓ 3. 停止旧的服务..."
docker-compose down > /dev/null 2>&1 || true

echo ""
echo "✓ 4. 构建 Docker 镜像..."
docker-compose build backend

echo ""
echo "✓ 5. 启动所有服务..."
docker-compose up -d

echo ""
echo "✓ 6. 等待服务启动..."
sleep 5

echo ""
echo "✓ 7. 检查服务状态..."
docker-compose ps

echo ""
echo "=========================================="
echo -e "${GREEN}✓ 部署完成！${NC}"
echo "=========================================="
echo ""
echo "服务地址："
echo "  - 后端 API:  http://localhost:8080"
echo "  - 前端界面:  http://localhost:5173"
echo "  - MySQL:     localhost:3306"
echo "  - n8n:       http://localhost:5678"
echo ""
echo "常用命令："
echo "  查看日志:   docker-compose logs -f backend"
echo "  停止服务:   docker-compose down"
echo "  重启服务:   docker-compose restart backend"
echo ""

