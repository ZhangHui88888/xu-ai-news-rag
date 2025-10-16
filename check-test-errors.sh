#!/bin/bash
# 在Linux服务器上运行此脚本查看测试错误

cd ~/xu-ai-news-rag/backend

if [ -f "target/surefire-reports/com.xu.news.controller.AuthControllerTest.txt" ]; then
    echo "=== AuthControllerTest 错误 ==="
    cat target/surefire-reports/com.xu.news.controller.AuthControllerTest.txt
    echo ""
fi

if [ -f "target/surefire-reports/com.xu.news.service.UserServiceTest.txt" ]; then
    echo "=== UserServiceTest 错误 ==="
    cat target/surefire-reports/com.xu.news.service.UserServiceTest.txt
    echo ""
fi

if [ -f "target/surefire-reports/com.xu.news.util.VectorStoreTest.txt" ]; then
    echo "=== VectorStoreTest 错误 ==="
    cat target/surefire-reports/com.xu.news.util.VectorStoreTest.txt
    echo ""
fi

# 查看所有测试摘要
echo "=== 测试摘要 ==="
grep -h "Tests run:" target/surefire-reports/*.txt | tail -1

