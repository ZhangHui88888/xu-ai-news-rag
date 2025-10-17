#!/bin/bash

###############################################################################
# XU-News-AI-RAG Docker环境测试脚本
# 
# 此脚本在Docker环境下运行所有测试
#
# @author XU
# @since 2025-10-17
###############################################################################

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo ""
    echo "=========================================="
    echo -e "${CYAN}$1${NC}"
    echo "=========================================="
    echo ""
}

# 检查Docker是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    print_success "Docker环境检查通过"
}

# 显示使用帮助
show_help() {
    echo "使用方法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  all       运行所有测试（默认）"
    echo "  backend   仅运行后端测试"
    echo "  frontend  仅运行前端测试"
    echo "  clean     清理测试容器和缓存"
    echo "  help      显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0              # 运行所有测试"
    echo "  $0 backend      # 仅运行后端测试"
    echo "  $0 clean        # 清理测试环境"
}

# 运行后端测试
run_backend_test() {
    print_header "运行后端测试（Docker）"
    
    docker-compose -f docker-compose.test.yml run --rm backend-test
    
    if [ $? -eq 0 ]; then
        print_success "后端测试通过 ✓"
        print_info "覆盖率报告: backend/target/site/jacoco/index.html"
    else
        print_error "后端测试失败 ✗"
        exit 1
    fi
}

# 运行前端测试
run_frontend_test() {
    print_header "运行前端测试（Docker）"
    
    docker-compose -f docker-compose.test.yml run --rm frontend-test
    
    if [ $? -eq 0 ]; then
        print_success "前端测试通过 ✓"
        print_info "覆盖率报告: frontend/coverage/index.html"
    else
        print_warning "前端测试失败或未完全配置"
    fi
}

# 运行所有测试
run_all_tests() {
    print_header "运行所有测试（Docker）"
    
    # 使用docker-compose同时运行所有测试
    docker-compose -f docker-compose.test.yml up --abort-on-container-exit --exit-code-from backend-test
    
    if [ $? -eq 0 ]; then
        print_success "所有测试通过 ✓"
    else
        print_error "测试失败，请检查上面的错误信息"
        exit 1
    fi
}

# 清理测试环境
clean_test_env() {
    print_header "清理测试环境"
    
    print_info "停止并删除测试容器..."
    docker-compose -f docker-compose.test.yml down -v
    
    print_info "删除测试镜像..."
    docker images | grep xu-news | grep test | awk '{print $3}' | xargs -r docker rmi -f
    
    print_info "清理Maven缓存..."
    rm -rf backend/target
    
    print_info "清理前端缓存..."
    rm -rf frontend/node_modules
    rm -rf frontend/coverage
    
    print_success "测试环境清理完成"
}

# 显示测试结果摘要
show_summary() {
    print_header "测试执行摘要"
    
    echo ""
    echo "📊 测试报告位置："
    echo "  • 后端覆盖率报告: backend/target/site/jacoco/index.html"
    echo "  • 前端覆盖率报告: frontend/coverage/index.html"
    echo ""
    echo "📚 相关文档："
    echo "  • Docker测试指南: DOCKER_TEST_GUIDE.md"
    echo "  • 完整测试指南: TESTING_COMPLETE_GUIDE.md"
    echo "  • 后端测试指南: backend/TEST_GUIDE.md"
    echo "  • 前端测试指南: frontend/tests/README.md"
    echo ""
    echo "💡 提示："
    echo "  • 在宿主机运行测试更快: ./run-all-tests.sh"
    echo "  • 清理测试环境: $0 clean"
    echo "  • 查看帮助: $0 help"
    echo ""
}

# 主函数
main() {
    # 检查Docker环境
    check_docker
    
    # 解析参数
    case "${1:-all}" in
        backend)
            run_backend_test
            ;;
        frontend)
            run_frontend_test
            ;;
        all)
            run_all_tests
            ;;
        clean)
            clean_test_env
            exit 0
            ;;
        help)
            show_help
            exit 0
            ;;
        *)
            print_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
    
    # 显示摘要
    show_summary
    
    print_success "测试执行完成！🎉"
}

# 执行主函数
main "$@"

