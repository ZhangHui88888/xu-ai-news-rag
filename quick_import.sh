#!/bin/bash
# 快速导入测试数据脚本

echo "========================================="
echo "  开始导入测试数据"
echo "========================================="

API_URL="http://localhost:8080/api/knowledge/import"

# 数据 1
echo "[1/5] 导入: Python 编程入门..."
curl -s -X POST "$API_URL" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{
    "title": "Python 编程入门",
    "content": "Python 是一门简单易学的编程语言，适合初学者学习。支持面向对象、函数式编程等多种编程范式。拥有丰富的标准库和第三方库，可以快速开发 Web、数据分析、人工智能等各种应用。",
    "tags": ["Python", "编程", "教程"],
    "contentType": "article",
    "summary": "Python 编程语言入门介绍"
  }' | grep -o '"code":[0-9]*' && echo "✅ 成功" || echo "❌ 失败"
sleep 2

# 数据 2
echo "[2/5] 导入: JavaScript 异步编程..."
curl -s -X POST "$API_URL" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{
    "title": "JavaScript 异步编程",
    "content": "JavaScript 的异步编程主要通过回调函数、Promise、async/await 实现。Promise 解决了回调地狱问题，提供了更优雅的异步处理方案。async/await 让异步代码看起来像同步代码，极大提升了代码可读性。",
    "tags": ["JavaScript", "异步编程", "前端"],
    "contentType": "article",
    "summary": "JavaScript 异步编程的三种方式"
  }' | grep -o '"code":[0-9]*' && echo "✅ 成功" || echo "❌ 失败"
sleep 2

# 数据 3
echo "[3/5] 导入: Docker 容器化技术..."
curl -s -X POST "$API_URL" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{
    "title": "Docker 容器化技术",
    "content": "Docker 是一个开源的容器化平台，可以将应用及其依赖打包成轻量级、可移植的容器。相比虚拟机，容器共享宿主机内核，启动更快、资源占用更少。Docker Compose 用于定义和运行多容器应用。",
    "tags": ["Docker", "容器化", "DevOps"],
    "contentType": "article",
    "summary": "Docker 容器技术的核心概念"
  }' | grep -o '"code":[0-9]*' && echo "✅ 成功" || echo "❌ 失败"
sleep 2

# 数据 4
echo "[4/5] 导入: MySQL 索引优化..."
curl -s -X POST "$API_URL" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{
    "title": "MySQL 索引优化",
    "content": "索引是提升数据库查询性能的关键。MySQL 主要使用 B+树索引，叶子节点包含完整数据或主键值。创建索引时要考虑查询频率、数据量、字段选择性。复合索引遵循最左前缀原则。定期使用 ANALYZE TABLE 更新统计信息。",
    "tags": ["MySQL", "数据库", "索引", "性能优化"],
    "contentType": "article",
    "summary": "MySQL 索引的原理和优化技巧"
  }' | grep -o '"code":[0-9]*' && echo "✅ 成功" || echo "❌ 失败"
sleep 2

# 数据 5
echo "[5/5] 导入: Redis 缓存应用..."
curl -s -X POST "$API_URL" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{
    "title": "Redis 缓存应用",
    "content": "Redis 是高性能的内存数据库，常用作缓存层提升系统性能。支持字符串、列表、集合、哈希、有序集合等数据结构。典型应用场景包括热点数据缓存、会话存储、排行榜、计数器、分布式锁。需要注意缓存穿透、击穿、雪崩问题。",
    "tags": ["Redis", "缓存", "NoSQL"],
    "contentType": "article",
    "summary": "Redis 缓存的应用场景和常见问题"
  }' | grep -o '"code":[0-9]*' && echo "✅ 成功" || echo "❌ 失败"

echo ""
echo "========================================="
echo "  ✅ 导入完成！"
echo "========================================="
echo "提示: 运行以下命令验证数据"
echo "  docker exec -it xu-news-mysql mysql -uxu_news -pxu_news_pass xu_news_rag -e 'SELECT COUNT(*) FROM knowledge_entry;'"

