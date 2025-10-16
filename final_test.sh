#!/bin/bash
# 重排功能最终测试脚本

export no_proxy="localhost,127.0.0.1,::1,172.18.0.0/16,192.168.0.0/16"
cd ~/xu-ai-news-rag

echo "========================================="
echo "  重排功能最终测试"
echo "========================================="

# 1. 同步并重建
echo -e "\n1️⃣  同步并重建容器..."
git pull
docker-compose stop backend
docker-compose rm -f backend
docker-compose up -d backend
sleep 25

# 2. 验证配置
echo -e "\n2️⃣  验证配置..."
echo "OLLAMA_URL:"
docker exec xu-news-backend env | grep OLLAMA_URL

# 3. 导入测试
echo -e "\n3️⃣  导入测试数据（20秒超时）..."
IMPORT=$(timeout 20 curl -s -X POST http://localhost:8080/api/knowledge/import \
  -H "Content-Type: application/json" \
  -d '{"title":"最终测试iPhone15Pro","content":"iPhone15Pro评测内容","tags":["iPhone"],"contentType":"article"}')

if echo "$IMPORT" | grep -q '"vectorId"'; then
  echo "✅ 导入成功"
  echo "$IMPORT" | grep -o '"vectorId":[0-9]*'
else
  echo "❌ 导入失败"
  echo "$IMPORT" | head -c 200
  exit 1
fi

sleep 5

# 4. 查询测试
echo -e "\n4️⃣  查询测试..."
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ZhangHui","password":"123456"}' | \
  grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
  QUERY=$(curl -s -X POST http://localhost:8080/api/query \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"query":"iPhone15Pro","topK":5,"needAnswer":false}')
  
  RETRIEVED=$(echo "$QUERY" | grep -o '"id":[0-9]*' | wc -l)
  echo "检索到: $RETRIEVED 个文档"
  
  sleep 3
  
  echo -e "\n5️⃣  重排日志..."
  docker logs --tail 100 xu-news-backend | grep -E "执行重排|重排完成"
fi

echo -e "\n========================================="
echo "  测试完成"
echo "========================================="

