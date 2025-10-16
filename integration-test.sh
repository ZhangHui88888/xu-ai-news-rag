#!/bin/bash

# XU-News-AI-RAG 集成测试脚本
# 测试真实API端点

set -e

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

BASE_URL="http://localhost:8080/api"
PASS_COUNT=0
FAIL_COUNT=0

# curl命令别名，跳过代理访问localhost
alias curl_local='curl --noproxy localhost,127.0.0.1'

echo -e "${CYAN}=========================================${NC}"
echo -e "${CYAN}XU-News-AI-RAG 集成测试${NC}"
echo -e "${CYAN}=========================================${NC}"
echo ""

# 测试函数
test_api() {
    local test_name=$1
    local method=$2
    local endpoint=$3
    local data=$4
    local expected_code=${5:-200}
    
    echo -e "${YELLOW}测试: $test_name${NC}"
    
    if [ "$method" = "POST" ]; then
        response=$(curl --noproxy localhost,127.0.0.1 -s -w "\n%{http_code}" -X POST "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data")
    elif [ "$method" = "GET" ]; then
        response=$(curl --noproxy localhost,127.0.0.1 -s -w "\n%{http_code}" "$BASE_URL$endpoint")
    fi
    
    http_code=$(echo "$response" | tail -1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "$expected_code" ]; then
        echo -e "${GREEN}  ✅ 通过 (HTTP $http_code)${NC}"
        PASS_COUNT=$((PASS_COUNT + 1))
        echo "  响应: $(echo $body | jq -c . 2>/dev/null || echo $body | head -c 100)"
    else
        echo -e "${RED}  ❌ 失败 (期望$expected_code, 实际$http_code)${NC}"
        FAIL_COUNT=$((FAIL_COUNT + 1))
        echo "  响应: $body"
    fi
    echo ""
}

# 开始测试
echo -e "${CYAN}=== 基础功能测试 ===${NC}"
echo ""

# 1. 健康检查
test_api "健康检查" "GET" "/auth/health"

# 2. 用户注册
TIMESTAMP=$(date +%s)
test_api "用户注册" "POST" "/auth/register" \
  "{\"username\":\"test$TIMESTAMP\",\"password\":\"Test123456\",\"confirmPassword\":\"Test123456\",\"email\":\"test$TIMESTAMP@test.com\",\"fullName\":\"测试用户\"}"

# 3. 用户登录
test_api "用户登录" "POST" "/auth/login" \
  '{"username":"testuser","password":"Test123456"}'

# 获取Token用于后续测试
TOKEN_RESPONSE=$(curl --noproxy localhost,127.0.0.1 -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"testuser","password":"Test123456"}')

TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.data.token // empty' 2>/dev/null)

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    echo -e "${GREEN}✅ 获取到Token: ${TOKEN:0:30}...${NC}"
    echo ""
    
    echo -e "${CYAN}=== 需要认证的功能测试 ===${NC}"
    echo ""
    
    # 4. RAG查询
    echo -e "${YELLOW}测试: RAG查询${NC}"
    curl --noproxy localhost,127.0.0.1 -s -X POST "$BASE_URL/query/ask" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{"query":"什么是人工智能？","topK":5}' | jq . || echo "  ⚠️  Ollama服务可能未运行"
    echo ""
else
    echo -e "${RED}⚠️  未获取到Token，跳过需要认证的测试${NC}"
    echo ""
fi

echo -e "${CYAN}=== N8N集成测试 ===${NC}"
echo ""

# 5. N8N导入知识
test_api "N8N导入知识" "POST" "/knowledge/import" \
  '{"title":"集成测试文章","content":"这是通过集成测试创建的内容","summary":"测试摘要","sourceUrl":"https://test.com","author":"测试","contentType":"test"}'

echo ""
echo -e "${CYAN}=========================================${NC}"
echo -e "${CYAN}测试完成${NC}"
echo -e "${CYAN}=========================================${NC}"
echo ""
echo -e "  通过: ${GREEN}$PASS_COUNT${NC}"
echo -e "  失败: ${RED}$FAIL_COUNT${NC}"
echo ""

if [ $FAIL_COUNT -eq 0 ]; then
    echo -e "${GREEN}🎉 所有测试通过！${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠️  部分测试失败，请检查服务状态${NC}"
    exit 1
fi

