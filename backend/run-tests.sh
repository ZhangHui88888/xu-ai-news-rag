#!/bin/bash

# XU-News-AI-RAG 测试运行脚本
# 用途：运行所有测试并生成报告

echo "=========================================="
echo "XU-News-AI-RAG 测试套件"
echo "=========================================="
echo ""

# 设置环境
export SPRING_PROFILES_ACTIVE=test

# 清理之前的构建
echo "正在清理之前的构建..."
mvn clean

echo ""
echo "=========================================="
echo "运行所有测试..."
echo "=========================================="
echo ""

# 运行测试并生成报告
mvn test jacoco:report

# 检查测试结果
if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "✅ 所有测试通过！"
    echo "=========================================="
    echo ""
    echo "测试报告位置："
    echo "  - HTML报告: target/site/jacoco/index.html"
    echo "  - XML报告: target/site/jacoco/jacoco.xml"
    echo "  - Surefire报告: target/surefire-reports/"
    echo ""
    
    # 显示覆盖率摘要
    if [ -f "target/site/jacoco/index.html" ]; then
        echo "代码覆盖率报告已生成"
        echo "在浏览器中打开: file://$(pwd)/target/site/jacoco/index.html"
    fi
else
    echo ""
    echo "=========================================="
    echo "❌ 测试失败！"
    echo "=========================================="
    echo ""
    echo "请查看测试报告了解详情："
    echo "  target/surefire-reports/"
    exit 1
fi

