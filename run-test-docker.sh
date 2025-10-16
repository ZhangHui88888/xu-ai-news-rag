#!/bin/bash

# XU-News-AI-RAG 测试脚本 - Linux版本
# 用途：在Docker环境中运行测试（针对没有Maven的生产容器）

set -e

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}==========================================${NC}"
echo -e "${CYAN}XU-News-AI-RAG 测试套件 (Docker)${NC}"
echo -e "${CYAN}==========================================${NC}"
echo ""

# 解析参数
TEST_CLASS=""
COVERAGE=false

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
        -h|--help)
            echo "用法: $0 [选项]"
            echo ""
            echo "选项:"
            echo "  -t, --test <类名>    运行指定的测试类"
            echo "  -c, --coverage       生成代码覆盖率报告"
            echo "  -h, --help           显示此帮助信息"
            echo ""
            echo "示例:"
            echo "  $0                          # 运行所有测试"
            echo "  $0 -c                       # 运行测试并生成覆盖率"
            echo "  $0 -t JwtUtilTest           # 运行指定测试"
            echo "  $0 -t JwtUtilTest -c        # 运行指定测试并生成覆盖率"
            exit 0
            ;;
        *)
            echo -e "${RED}未知参数: $1${NC}"
            echo "使用 $0 --help 查看帮助"
            exit 1
            ;;
    esac
done

# 检查Docker
echo -e "${YELLOW}检查Docker环境...${NC}"
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker未安装！${NC}"
    exit 1
fi

if ! docker ps &> /dev/null; then
    echo -e "${RED}❌ Docker未运行或权限不足！${NC}"
    echo -e "${YELLOW}提示: 确保你的用户在docker组中，或使用sudo${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Docker环境正常${NC}"
echo ""

# 构建测试命令
TEST_CMD="mvn clean test"

if [ -n "$TEST_CLASS" ]; then
    echo -e "${CYAN}运行指定测试: $TEST_CLASS${NC}"
    TEST_CMD="$TEST_CMD -Dtest=$TEST_CLASS"
fi

if [ "$COVERAGE" = true ]; then
    echo -e "${CYAN}将生成代码覆盖率报告${NC}"
    TEST_CMD="$TEST_CMD jacoco:report"
fi

echo ""
echo -e "${CYAN}==========================================${NC}"
echo -e "${CYAN}使用Maven容器运行测试...${NC}"
echo -e "${CYAN}==========================================${NC}"
echo ""

# 创建Maven仓库卷（如果不存在）
if ! docker volume ls | grep -q maven-repo-test; then
    echo -e "${YELLOW}创建Maven仓库卷...${NC}"
    docker volume create maven-repo-test
fi

# 运行测试容器
echo -e "${YELLOW}启动测试容器...${NC}"
if docker run --rm \
    --name xu-news-test-runner \
    -v "$(pwd)/backend:/app" \
    -v maven-repo-test:/root/.m2 \
    -w /app \
    -e SPRING_PROFILES_ACTIVE=test \
    maven:3.9-eclipse-temurin-17 \
    sh -c "$TEST_CMD"; then
    
    echo ""
    echo -e "${GREEN}==========================================${NC}"
    echo -e "${GREEN}✅ 测试通过！${NC}"
    echo -e "${GREEN}==========================================${NC}"
    echo ""
    
    # 复制报告
    echo -e "${YELLOW}整理测试报告...${NC}"
    REPORT_DIR="./test-reports"
    mkdir -p "$REPORT_DIR"
    
    if [ -d "./backend/target/surefire-reports" ]; then
        cp -r ./backend/target/surefire-reports "$REPORT_DIR/"
        echo -e "${GREEN}✅ Surefire报告已复制${NC}"
    fi
    
    if [ "$COVERAGE" = true ] && [ -d "./backend/target/site/jacoco" ]; then
        cp -r ./backend/target/site/jacoco "$REPORT_DIR/"
        echo -e "${GREEN}✅ JaCoCo覆盖率报告已复制${NC}"
        echo ""
        echo -e "${CYAN}📊 覆盖率报告位置:${NC}"
        echo "   file://$(pwd)/test-reports/jacoco/index.html"
    fi
    
    echo ""
    echo -e "${CYAN}测试报告目录: ./test-reports/${NC}"
    
else
    echo ""
    echo -e "${RED}==========================================${NC}"
    echo -e "${RED}❌ 测试失败！${NC}"
    echo -e "${RED}==========================================${NC}"
    echo ""
    
    # 复制失败报告
    if [ -d "./backend/target/surefire-reports" ]; then
        mkdir -p ./test-reports
        cp -r ./backend/target/surefire-reports ./test-reports/
        echo -e "${YELLOW}失败报告已复制到: ./test-reports/surefire-reports/${NC}"
    fi
    
    exit 1
fi

echo ""
echo -e "${CYAN}==========================================${NC}"
echo -e "${CYAN}测试完成！${NC}"
echo -e "${CYAN}==========================================${NC}"

