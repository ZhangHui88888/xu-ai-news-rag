# 🚀 完整配置和测试指南

## 📋 当前状态

✅ **已完成：**
- 后端服务正常运行
- 数据库连接成功
- 重排模型配置已添加到 docker-compose.yml

⚠️ **待完成：**
- 重启服务使重排配置生效
- 导入测试数据到知识库
- 验证重排功能

---

## 🔧 步骤 1: 重启服务（使重排配置生效）

```powershell
# 停止所有服务
docker-compose down

# 重新启动（使用新配置）
docker-compose up -d backend

# 查看日志，验证重排模型已启用
docker-compose logs -f backend
```

**期望看到的日志：**
```
INFO  c.x.n.u.RerankerClient - 重排模型已启用: BAAI/bge-reranker-v2-m3
INFO  c.x.n.XuNewsApplication - Started XuNewsApplication
```

---

## 📚 步骤 2: 导入测试数据

### 方式一：通过 API 快速导入（推荐）

创建测试文件 `test_data.json`:

```json
{
  "entries": [
    {
      "title": "Python 文件读写完全指南",
      "content": "Python 提供了多种读写文件的方法。使用 open() 函数可以打开文件，配合 read()、readline()、readlines() 方法可以读取文件内容。写入文件使用 write() 方法。建议使用 with 语句自动管理文件资源。",
      "tags": ["Python", "编程", "文件操作"],
      "contentType": "article",
      "dataSource": "手动导入"
    },
    {
      "title": "JavaScript 异步编程详解",
      "content": "JavaScript 的异步编程主要通过回调函数、Promise、async/await 实现。Promise 解决了回调地狱问题，async/await 让异步代码看起来像同步代码。事件循环是 JavaScript 异步机制的核心。",
      "tags": ["JavaScript", "异步编程", "前端"],
      "contentType": "article",
      "dataSource": "手动导入"
    },
    {
      "title": "机器学习基础概念",
      "content": "机器学习是人工智能的一个分支，让计算机从数据中学习模式。主要分为监督学习、无监督学习、强化学习三大类。常用算法包括线性回归、决策树、神经网络等。",
      "tags": ["机器学习", "人工智能", "算法"],
      "contentType": "article",
      "dataSource": "手动导入"
    },
    {
      "title": "Docker 容器化技术入门",
      "content": "Docker 是一个开源的容器化平台，可以将应用及其依赖打包成容器。容器比虚拟机更轻量，启动更快。Docker Compose 用于定义和运行多容器应用。Dockerfile 定义镜像构建步骤。",
      "tags": ["Docker", "容器化", "DevOps"],
      "contentType": "article",
      "dataSource": "手动导入"
    },
    {
      "title": "RESTful API 设计最佳实践",
      "content": "RESTful API 是基于 HTTP 协议的 API 设计风格。使用标准 HTTP 方法（GET、POST、PUT、DELETE）对应 CRUD 操作。URL 应该是名词而非动词，使用复数形式。返回合适的 HTTP 状态码。",
      "tags": ["REST", "API", "后端开发"],
      "contentType": "article",
      "dataSource": "手动导入"
    },
    {
      "title": "React Hooks 使用指南",
      "content": "React Hooks 让函数组件也能使用状态和生命周期特性。useState 管理状态，useEffect 处理副作用，useContext 使用上下文，useReducer 管理复杂状态。自定义 Hook 可以复用逻辑。",
      "tags": ["React", "Hooks", "前端框架"],
      "contentType": "article",
      "dataSource": "手动导入"
    },
    {
      "title": "MySQL 索引优化技巧",
      "content": "索引可以大幅提升查询性能。B+树索引是最常用的索引类型。建立索引时要考虑查询频率、数据量、选择性。复合索引遵循最左前缀原则。避免在索引列上使用函数。定期 ANALYZE 表保持统计信息准确。",
      "tags": ["MySQL", "数据库", "性能优化"],
      "contentType": "article",
      "dataSource": "手动导入"
    },
    {
      "title": "Git 分支管理策略",
      "content": "Git Flow 是常用的分支管理模型。主分支 master/main 保持稳定，develop 用于日常开发，feature 分支开发新功能，release 分支准备发布，hotfix 修复紧急问题。使用 Pull Request 进行代码审查。",
      "tags": ["Git", "版本控制", "团队协作"],
      "contentType": "article",
      "dataSource": "手动导入"
    },
    {
      "title": "TDD 测试驱动开发方法",
      "content": "TDD 是一种软件开发方法，先写测试再写代码。流程是：写一个失败的测试、编写最少代码让测试通过、重构代码。优点是提高代码质量、减少 bug、改善设计。常用测试框架有 JUnit、pytest、Jest。",
      "tags": ["TDD", "测试", "软件工程"],
      "contentType": "article",
      "dataSource": "手动导入"
    },
    {
      "title": "Redis 缓存使用场景",
      "content": "Redis 是内存数据库，常用作缓存提升性能。支持字符串、列表、集合、哈希、有序集合等数据结构。适用场景包括：热点数据缓存、会话存储、排行榜、计数器、消息队列。注意缓存穿透、雪崩、击穿问题。",
      "tags": ["Redis", "缓存", "NoSQL"],
      "contentType": "article",
      "dataSource": "手动导入"
    }
  ]
}
```

**使用 PowerShell 导入：**

```powershell
# 导入测试数据（需要先登录获取 Token）
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer YOUR_JWT_TOKEN"
}

# 读取测试数据
$testData = Get-Content test_data.json | ConvertFrom-Json

# 逐个导入
foreach ($entry in $testData.entries) {
    $body = $entry | ConvertTo-Json
    
    Invoke-RestMethod -Uri "http://localhost:8080/api/knowledge/import" `
        -Method POST `
        -Headers $headers `
        -Body $body
    
    Write-Host "已导入: $($entry.title)" -ForegroundColor Green
    Start-Sleep -Seconds 2
}
```

### 方式二：通过前端界面导入

1. 访问：http://localhost:5173
2. 登录系统
3. 进入"知识库管理"
4. 点击"导入"或"新增"
5. 逐个添加上述文章

### 方式三：使用 SQL 直接插入

```sql
-- 连接数据库
USE xu_news_rag;

-- 插入测试数据（向量字段需要后端服务处理）
INSERT INTO knowledge_entry (title, content, tags, content_type, data_source, status, created_at, updated_at)
VALUES 
('Python 文件读写完全指南', 'Python 提供了多种读写文件的方法...', '["Python","编程","文件操作"]', 'article', '手动导入', 1, NOW(), NOW()),
('JavaScript 异步编程详解', 'JavaScript 的异步编程主要通过回调函数...', '["JavaScript","异步编程","前端"]', 'article', '手动导入', 1, NOW(), NOW());
-- ... 其他数据
```

**注意：** 通过 SQL 直接插入的数据缺少向量字段，需要后端重新处理才能被检索到。推荐使用 API 导入。

---

## 🧪 步骤 3: 测试重排功能

### 测试查询 1：Python 文件操作

```powershell
curl -X POST http://localhost:8080/api/query `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_TOKEN" `
  -d '{
    "query": "Python如何读取文件？",
    "topK": 5,
    "needAnswer": true
  }'
```

**预期结果：**
- 检索到相关文档
- "Python 文件读写完全指南" 排名第一
- 看到重排日志

### 测试查询 2：异步编程

```powershell
curl -X POST http://localhost:8080/api/query `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_TOKEN" `
  -d '{
    "query": "JavaScript 异步编程有哪些方式？",
    "topK": 3,
    "needAnswer": true
  }'
```

### 测试查询 3：对比效果

**关闭重排：**
```yaml
# docker-compose.yml
- RERANKER_ENABLED=false
```

**启用重排：**
```yaml
# docker-compose.yml
- RERANKER_ENABLED=true
```

对比两次查询结果的相关性和排序。

---

## 📊 验证清单

### ✅ 配置验证

- [ ] docker-compose.yml 已添加重排配置
- [ ] 服务已重启
- [ ] 日志显示 "重排模型已启用"

### ✅ 数据验证

- [ ] 至少导入 5 条测试数据
- [ ] 数据包含标题、内容、标签
- [ ] 向量已生成（vector_id 不为空）

### ✅ 功能验证

- [ ] 查询能返回结果（RetrievedDocs > 0）
- [ ] 日志显示重排过程
- [ ] 结果相关性良好

---

## 🎯 完整操作流程（复制粘贴）

```powershell
# ===== 步骤 1: 重启服务 =====
Write-Host "===== 步骤 1: 重启服务 =====" -ForegroundColor Cyan

cd D:\github\funNovels\xu-ai-news-rag

# 停止服务
docker-compose down

# 启动后端服务
docker-compose up -d backend

# 等待服务启动
Write-Host "等待服务启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# 查看日志（Ctrl+C 退出）
docker-compose logs -f backend
```

**看到 "Started XuNewsApplication" 后按 Ctrl+C，然后继续：**

```powershell
# ===== 步骤 2: 导入测试数据 =====
Write-Host "`n===== 步骤 2: 导入测试数据 =====" -ForegroundColor Cyan

# 先登录获取 Token（替换用户名密码）
$loginBody = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $loginBody

$token = $loginResponse.data.token
Write-Host "登录成功，Token: $token" -ForegroundColor Green

# 导入一条测试数据
$testEntry = @{
    title = "Python 文件读写完全指南"
    content = "Python 提供了多种读写文件的方法。使用 open() 函数可以打开文件，配合 read()、readline()、readlines() 方法可以读取文件内容。"
    tags = @("Python", "编程", "文件操作")
    contentType = "article"
    dataSource = "手动导入"
} | ConvertTo-Json

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/knowledge/import" `
        -Method POST `
        -Headers $headers `
        -Body $testEntry
    
    Write-Host "✅ 数据导入成功！" -ForegroundColor Green
} catch {
    Write-Host "❌ 导入失败: $_" -ForegroundColor Red
}

# ===== 步骤 3: 测试查询 =====
Write-Host "`n===== 步骤 3: 测试查询 =====" -ForegroundColor Cyan

Start-Sleep -Seconds 5

$queryBody = @{
    query = "Python如何读取文件？"
    topK = 5
    needAnswer = $true
} | ConvertTo-Json

try {
    $queryResult = Invoke-RestMethod -Uri "http://localhost:8080/api/query" `
        -Method POST `
        -Headers $headers `
        -Body $queryBody
    
    Write-Host "✅ 查询成功！" -ForegroundColor Green
    Write-Host "检索到文档数: $($queryResult.data.retrievedEntries.Count)" -ForegroundColor Yellow
    Write-Host "响应时间: $($queryResult.data.responseTimeMs)ms" -ForegroundColor Yellow
    
    if ($queryResult.data.retrievedEntries.Count -gt 0) {
        Write-Host "`n检索结果：" -ForegroundColor Cyan
        foreach ($entry in $queryResult.data.retrievedEntries) {
            Write-Host "  - $($entry.title) (相似度: $($entry.similarityScore))" -ForegroundColor White
        }
    }
    
    Write-Host "`nAI 回答：" -ForegroundColor Cyan
    Write-Host $queryResult.data.answer -ForegroundColor White
    
} catch {
    Write-Host "❌ 查询失败: $_" -ForegroundColor Red
}

Write-Host "`n===== 完成！=====" -ForegroundColor Green
Write-Host "请查看 Docker 日志验证重排功能是否启用" -ForegroundColor Yellow
```

---

## 📈 预期日志输出

### 重排功能已启用

```
2025-10-17 12:00:00 [main] INFO  c.x.n.u.RerankerClient - 重排模型已启用: BAAI/bge-reranker-v2-m3
2025-10-17 12:00:05 [main] INFO  c.x.n.XuNewsApplication - Started XuNewsApplication in 8.5 seconds
```

### 查询时的重排日志

```
2025-10-17 12:05:00 [http-nio-8080-exec-1] DEBUG c.x.n.s.i.QueryServiceImpl - 生成查询向量: Python如何读取文件？
2025-10-17 12:05:00 [http-nio-8080-exec-1] DEBUG c.x.n.s.i.QueryServiceImpl - 检索相关文档，CandidateCount=20, Threshold=0.5
2025-10-17 12:05:01 [http-nio-8080-exec-1] DEBUG c.x.n.s.i.QueryServiceImpl - 执行重排序: 候选文档数=8, 目标TopK=5
2025-10-17 12:05:02 [http-nio-8080-exec-1] DEBUG c.x.n.u.RerankerClient - 重排请求: query=Python如何读取文件？, documents=8, topK=5
2025-10-17 12:05:03 [http-nio-8080-exec-1] INFO  c.x.n.u.RerankerClient - 重排完成: 输入文档数=8, 输出结果数=5
2025-10-17 12:05:03 [http-nio-8080-exec-1] INFO  c.x.n.s.i.QueryServiceImpl - 重排完成: 输入候选数=8, 输出结果数=5
2025-10-17 12:05:04 [http-nio-8080-exec-1] INFO  c.x.n.s.i.QueryServiceImpl - 查询完成: QueryID=25, ResponseTime=4523ms, RetrievedDocs=5
```

---

## 🎉 成功标志

当您看到以下情况时，说明配置成功：

1. ✅ 日志显示 "重排模型已启用"
2. ✅ 查询返回 RetrievedDocs > 0
3. ✅ 日志显示 "执行重排序" 和 "重排完成"
4. ✅ 响应时间在合理范围（2-5秒）
5. ✅ 检索结果相关性高

---

## 🐛 故障排查

### 问题 1: 重排日志未出现

**检查配置：**
```powershell
docker exec xu-news-backend env | grep RERANKER
```

**预期输出：**
```
RERANKER_ENABLED=true
RERANKER_URL=https://api.siliconflow.cn
RERANKER_API_KEY=sk-rcnm...
```

### 问题 2: API Key 无效

**测试 API Key：**
```powershell
curl -X POST https://api.siliconflow.cn/v1/rerank `
  -H "Authorization: Bearer sk-rcnmpixqkdnfihlgiyqbjeqcolcaigumbczkvkkvgjuxuohu" `
  -H "Content-Type: application/json" `
  -d '{
    "model": "BAAI/bge-reranker-v2-m3",
    "query": "测试",
    "documents": ["文档1", "文档2"]
  }'
```

### 问题 3: 知识库仍然为空

**检查数据：**
```powershell
docker exec -it xu-news-mysql mysql -uxu_news -pxu_news_pass xu_news_rag -e "SELECT COUNT(*) FROM knowledge_entry;"
```

---

**祝您配置顺利！🎉**

需要帮助请随时联系。

