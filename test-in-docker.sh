#!/bin/bash

# XU-News-AI-RAG Docker环境测试脚本 (Bash)
# 用途: 在VMware Docker环境中运行后端测试

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 参数
TEST_CLASS=""
COVERAGE=false
COPY_REPORTS=true

# 解析参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--test)
            TEST_CLASS="$2"
            shift 2
            ;;
        -c|--coverage)
            COVERAGE=true
            shift
            ;;
        --no-copy)
            COPY_REPORTS=false
            shift
            ;;
        *)
            echo "未知参数: $1"
            exit 1
            ;;
    esac
done

echo -e "${CYAN}==========================================${NC}"
echo -e "${CYAN}XU-News-AI-RAG Docker 测试套件${NC}"
echo -e "${CYAN}==========================================${NC}"
echo ""

# 检查Docker
echo -e "${YELLOW}检查Docker环境...${NC}"
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker未安装！${NC}"
    exit 1
fi

if ! docker ps &> /dev/null; then
    echo -e "${RED}❌ Docker未运行！${NC}"
    exit 1
fi

# 检查容器
if ! docker ps -a --filter "name=xu-news-backend" --format "{{.Names}}" | grep -q "xu-news-backend"; then
    echo -e "${RED}❌ 后端容器 'xu-news-backend' 不存在！${NC}"
    echo -e "${YELLOW}请先启动项目: docker-compose up -d${NC}"
    exit 1
fi

# 启动容器（如果未运行）
if ! docker ps --filter "name=xu-news-backend" --format "{{.Names}}" | grep -q "xu-news-backend"; then
    echo -e "${YELLOW}⚠️  后端容器未运行，正在启动...${NC}"
    docker start xu-news-backend
    sleep 5
fi

echo -e "${GREEN}✅ Docker环境检查通过${NC}"
echo ""

# 构建测试命令
TEST_CMD="cd /app && mvn clean test"

if [ -n "$TEST_CLASS" ]; then
    echo -e "${CYAN}运行指定测试: $TEST_CLASS${NC}"
    TEST_CMD="$TEST_CMD -Dtest=$TEST_CLASS"
fi

if [ "$COVERAGE" = true ]; then
    echo -e "${CYAN}生成代码覆盖率报告...${NC}"
    TEST_CMD="$TEST_CMD jacoco:report"
fi

echo ""
echo -e "${CYAN}==========================================${NC}"
echo -e "${CYAN}在Docker容器中运行测试...${NC}"
echo -e "${CYAN}==========================================${NC}"
echo ""

# 运行测试
if docker exec -it xu-news-backend sh -c "$TEST_CMD"; then
    echo ""
    echo -e "${GREEN}==========================================${NC}"
    echo -e "${GREEN}✅ 测试通过！${NC}"
    echo -e "${GREEN}==========================================${NC}"
    echo ""
    
    if [ "$COPY_REPORTS" = true ]; then
        echo -e "${YELLOW}正在复制测试报告到本地...${NC}"
        
        # 创建报告目录
        REPORT_DIR="./test-reports"
        rm -rf "$REPORT_DIR"
        mkdir -p "$REPORT_DIR"
        
        # 复制Surefire报告
        echo "  - 复制Surefire报告..."
        docker cp xu-news-backend:/app/target/surefire-reports "$REPORT_DIR/surefire" 2>/dev/null || true
        
        if [ "$COVERAGE" = true ]; then
            # 复制JaCoCo报告
            echo "  - 复制JaCoCo覆盖率报告..."
            docker cp xu-news-backend:/app/target/site/jacoco "$REPORT_DIR/jacoco" 2>/dev/null || true
            
            if [ -f "$REPORT_DIR/jacoco/index.html" ]; then
                echo ""
                echo -e "${CYAN}📊 代码覆盖率报告:${NC}"
                echo "   $(pwd)/test-reports/jacoco/index.html"
            fi
        fi
        
        echo ""
        echo -e "${GREEN}✅ 报告已复制到: ./test-reports/${NC}"
    fi
    
    echo ""
    echo -e "${CYAN}测试报告位置（容器内）:${NC}"
    echo "  - Surefire: /app/target/surefire-reports/"
    if [ "$COVERAGE" = true ]; then
        echo "  - JaCoCo: /app/target/site/jacoco/index.html"
    fi
else
    echo ""
    echo -e "${RED}==========================================${NC}"
    echo -e "${RED}❌ 测试失败！${NC}"
    echo -e "${RED}==========================================${NC}"
    echo ""
    echo -e "${YELLOW}查看详细错误信息:${NC}"
    echo "  docker exec -it xu-news-backend cat /app/target/surefire-reports/*.txt"
    echo ""
    
    if [ "$COPY_REPORTS" = true ]; then
        docker cp xu-news-backend:/app/target/surefire-reports ./test-reports/surefire 2>/dev/null || true
        echo -e "${YELLOW}失败报告已复制到: ./test-reports/surefire/${NC}"
    fi
    
    exit 1
fi

echo ""
echo -e "${CYAN}==========================================${NC}"
echo -e "${CYAN}测试完成！${NC}"
echo -e "${CYAN}==========================================${NC}"

