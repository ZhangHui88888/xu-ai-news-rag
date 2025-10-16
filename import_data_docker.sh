#!/bin/bash
# Docker 容器内数据导入脚本

echo "========================================="
echo "  XU-News 数据导入工具 (Docker 版本)"
echo "========================================="

# API 地址（容器内部）
API_URL="http://localhost:8080/api/knowledge/import"

# 测试数据
declare -a titles=(
  "Python 文件读写完全指南"
  "JavaScript 异步编程详解"
  "机器学习基础概念入门"
  "Docker 容器化技术实战"
  "RESTful API 设计最佳实践"
)

declare -a contents=(
  "Python 提供了多种读写文件的方法。使用 open() 函数可以打开文件，配合 read()、readline()、readlines() 方法可以读取文件内容。"
  "JavaScript 的异步编程主要通过回调函数、Promise、async/await 实现。Promise 解决了回调地狱问题。"
  "机器学习是人工智能的核心分支，让计算机从数据中自动学习规律。主要分为监督学习、无监督学习、强化学习。"
  "Docker 是一个开源的容器化平台，可以将应用及其依赖打包成轻量级、可移植的容器。"
  "RESTful API 是基于 HTTP 协议的 API 设计风格，遵循 REST 架构原则。"
)

success=0
fail=0

# 导入数据
for i in "${!titles[@]}"; do
  num=$((i + 1))
  echo ""
  echo "[$num/${#titles[@]}] 导入: ${titles[$i]}"
  
  # 构建 JSON
  json=$(cat <<EOF
{
  "title": "${titles[$i]}",
  "content": "${contents[$i]}",
  "tags": ["测试", "导入"],
  "contentType": "article",
  "summary": "测试数据"
}
EOF
)
  
  # 发送请求
  response=$(curl -s -X POST "$API_URL" \
    -H "Content-Type: application/json" \
    -d "$json")
  
  # 检查结果
  if echo "$response" | grep -q '"code":200'; then
    echo "  ✅ 成功！"
    ((success++))
  else
    echo "  ❌ 失败: $response"
    ((fail++))
  fi
  
  # 等待 2 秒
  sleep 2
done

echo ""
echo "========================================="
echo "  导入完成！"
echo "========================================="
echo "✅ 成功: $success 条"
echo "❌ 失败: $fail 条"
echo "📊 总计: ${#titles[@]} 条"

