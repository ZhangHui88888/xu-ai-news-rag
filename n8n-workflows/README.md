# XU-News-AI-RAG n8n 工作流配置指南

## 📋 目录

1. [概述](#概述)
2. [工作流列表](#工作流列表)
3. [快速开始](#快速开始)
4. [详细配置](#详细配置)
5. [Webhook 端点](#webhook-端点)
6. [常见问题](#常见问题)
7. [最佳实践](#最佳实践)

---

## 📖 概述

本目录包含 XU-News-AI-RAG 项目的所有 n8n 工作流配置文件。n8n 是一个可视化的工作流自动化工具,在本项目中负责:

- **数据采集**: RSS 订阅源采集、网页抓取
- **AI 处理**: 内容摘要生成、向量化处理
- **RAG 问答**: 向量检索、智能问答生成
- **文档处理**: PDF/Word/TXT 文件解析和入库
- **通知服务**: 邮件通知、任务状态推送

---

## 📂 工作流列表

| 工作流文件 | 功能说明 | 触发方式 | 状态 |
|-----------|---------|---------|------|
| `rss-news-collector.json` | RSS 新闻采集 | 定时触发(每6小时) | ✅ 可用 |
| `webpage-scraper.json` | 网页内容抓取 | Webhook 触发 | ✅ 可用 |
| `rag-qa-workflow.json` | RAG 智能问答 | Webhook 触发 | ✅ 可用 |
| `document-processor.json` | 文档处理入库 | Webhook 触发 | ✅ 可用 |
| `email-notification.json` | 邮件通知 | Webhook 触发 | ✅ 可用 |

---

## 🚀 快速开始

### 1. 启动 n8n 服务

```bash
# 使用 docker-compose 启动
cd xu-ai-news-rag
docker-compose up -d n8n

# 访问 n8n 管理界面
# http://localhost:5678
# 默认账号: admin / admin123
```

### 2. 导入工作流

1. 登录 n8n 管理界面 (http://localhost:5678)
2. 点击左侧菜单 **"Workflows"**
3. 点击右上角 **"Import from File"**
4. 选择 `n8n-workflows` 目录下的 JSON 文件
5. 依次导入所有工作流

### 3. 配置凭据

在使用工作流前,需要配置以下凭据:

#### MySQL 数据库凭据

- **名称**: MySQL - xu_news_rag
- **类型**: MySQL
- **配置**:
  ```
  Host: mysql
  Port: 3306
  Database: xu_news_rag
  User: xu_news
  Password: xu_news_pass
  ```

#### SMTP 邮件凭据 (可选)

- **名称**: SMTP - XU News
- **类型**: SMTP
- **配置**:
  ```
  Host: smtp.example.com
  Port: 587
  Secure: true
  User: noreply@xu-news-rag.com
  Password: your_smtp_password
  ```

### 4. 激活工作流

1. 打开每个工作流
2. 点击右上角 **"Active"** 开关激活
3. 定时任务会自动运行,Webhook 工作流会生成 URL

---

## ⚙️ 详细配置

### 🗞️ RSS 新闻采集工作流

**文件**: `rss-news-collector.json`

#### 工作流程

```
定时触发(每6小时) → 获取RSS源配置 → 读取RSS订阅 → 去重检查 
→ 获取完整内容 → 提取正文 → AI生成摘要 → 保存到后端 → 统计结果
```

#### 配置项

1. **定时触发器**
   - Cron 表达式: `0 */6 * * *` (每6小时)
   - 可修改为其他频率,如每小时: `0 * * * *`

2. **RSS 源配置**
   - 从 `data_source` 表读取 RSS 订阅源
   - 可在数据库中添加/修改 RSS 源

3. **Ollama 配置**
   - 模型: `qwen2.5:3b`
   - 地址: `http://192.168.171.1:11434`
   - 根据实际部署调整 IP 地址

4. **后端 API**
   - 地址: `http://backend:8080/api/knowledge/import`
   - 用于保存处理后的内容

#### 使用示例

```sql
-- 在数据库中添加 RSS 源
INSERT INTO data_source (name, type, url, status, created_at, updated_at) 
VALUES 
  ('TechCrunch', 'RSS', 'https://techcrunch.com/feed/', 1, NOW(), NOW()),
  ('36氪', 'RSS', 'https://36kr.com/feed', 1, NOW(), NOW());
```

---

### 🌐 网页抓取工作流

**文件**: `webpage-scraper.json`

#### 工作流程

```
Webhook触发 → 解析请求 → 抓取网页 → 提取内容 
→ 生成摘要 → 保存到后端 → 返回响应
```

#### Webhook 调用

```bash
# POST 请求
curl -X POST http://localhost:5678/webhook/scrape-webpage \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://example.com/article",
    "selector": "article",
    "dataSourceId": 1,
    "userId": 1,
    "tags": ["tech", "AI"]
  }'
```

#### 参数说明

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| url | string | ✅ | 目标网页 URL |
| selector | string | ❌ | CSS 选择器,默认 `body` |
| dataSourceId | number | ❌ | 数据源 ID |
| userId | number | ✅ | 用户 ID |
| tags | array | ❌ | 标签数组 |

---

### 🤖 RAG 智能问答工作流

**文件**: `rag-qa-workflow.json`

#### 工作流程

```
Webhook触发 → 解析查询 → 查询向量化 → 向量检索 
→ 检查结果 ┬→ [有结果] 构建RAG提示 → 生成答案 → 返回响应
           └→ [无结果] 联网搜索 → 返回响应
```

#### Webhook 调用

```bash
# POST 请求
curl -X POST http://localhost:5678/webhook/rag-query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "什么是 RAG 技术?",
    "userId": 1,
    "topK": 5,
    "enableWebSearch": true
  }'
```

#### 参数说明

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| query | string | ✅ | 用户查询 |
| userId | number | ✅ | 用户 ID |
| topK | number | ❌ | 返回结果数,默认 5 |
| enableWebSearch | boolean | ❌ | 是否启用联网搜索,默认 true |

#### 响应示例

```json
{
  "query": "什么是 RAG 技术?",
  "answer": "RAG(检索增强生成)是一种结合信息检索和文本生成的AI技术...",
  "retrievedDocs": [
    {
      "title": "RAG 技术详解",
      "content": "...",
      "similarity": 0.85
    }
  ],
  "source": "knowledge_base",
  "timestamp": "2025-10-16T10:30:00Z"
}
```

---

### 📄 文档处理工作流

**文件**: `document-processor.json`

#### 工作流程

```
Webhook触发 → 解析请求 → 下载文件 → 判断文件类型 
→ [PDF/Word/TXT] 提取内容 → 内容分段 → 生成摘要 
→ 保存到后端 → 返回响应
```

#### Webhook 调用

```bash
# POST 请求
curl -X POST http://localhost:5678/webhook/process-document \
  -H "Content-Type: application/json" \
  -d '{
    "fileUrl": "http://backend:8080/uploads/document.pdf",
    "fileName": "research-paper.pdf",
    "fileType": "pdf",
    "userId": 1,
    "tags": ["research", "AI"]
  }'
```

#### 支持的文件类型

- ✅ PDF (.pdf)
- ✅ Word (.docx, .doc)
- ✅ 文本文件 (.txt)
- 🔜 Excel (.xlsx, .xls)
- 🔜 Markdown (.md)

#### 内容分段

- 每段最大长度: 2000 字符
- 按段落自动分割
- 每段独立生成摘要和向量

---

### 📧 邮件通知工作流

**文件**: `email-notification.json`

#### 工作流程

```
Webhook触发 → 解析请求 → 判断通知类型 
→ [知识入库/错误通知/每日摘要] 构建邮件 → 发送邮件 → 返回响应
```

#### Webhook 调用

```bash
# 知识入库通知
curl -X POST http://localhost:5678/webhook/send-notification \
  -H "Content-Type: application/json" \
  -d '{
    "type": "knowledge_import",
    "recipientEmail": "user@example.com",
    "subject": "新内容入库通知",
    "message": "您的知识库已更新",
    "data": {
      "title": "AI 新闻",
      "source": "TechCrunch",
      "contentType": "news",
      "summary": "..."
    }
  }'

# 错误通知
curl -X POST http://localhost:5678/webhook/send-notification \
  -H "Content-Type: application/json" \
  -d '{
    "type": "error",
    "recipientEmail": "admin@example.com",
    "subject": "采集任务失败",
    "data": {
      "taskType": "RSS采集",
      "errorMessage": "连接超时",
      "url": "https://example.com/feed"
    }
  }'

# 每日摘要
curl -X POST http://localhost:5678/webhook/send-notification \
  -H "Content-Type: application/json" \
  -d '{
    "type": "daily_digest",
    "recipientEmail": "user@example.com",
    "data": {
      "newCount": 15,
      "totalCount": 1523,
      "queryCount": 42,
      "newItems": [...]
    }
  }'
```

#### 邮件模板

工作流内置了三种精美的 HTML 邮件模板:
1. **知识入库通知**: 渐变蓝紫色主题
2. **错误通知**: 渐变粉红色主题
3. **每日摘要**: 统计数据卡片 + 内容列表

---

## 🔗 Webhook 端点

所有 Webhook 工作流激活后会生成唯一的 URL:

| 工作流 | Webhook 路径 | 完整 URL |
|-------|-------------|----------|
| 网页抓取 | `/webhook/scrape-webpage` | `http://localhost:5678/webhook/scrape-webpage` |
| RAG 问答 | `/webhook/rag-query` | `http://localhost:5678/webhook/rag-query` |
| 文档处理 | `/webhook/process-document` | `http://localhost:5678/webhook/process-document` |
| 邮件通知 | `/webhook/send-notification` | `http://localhost:5678/webhook/send-notification` |

### 在 Spring Boot 中调用 Webhook

```java
@Service
public class N8nService {
    
    @Value("${n8n.webhook.base-url}")
    private String n8nBaseUrl;
    
    private final RestTemplate restTemplate;
    
    public void triggerWebScraping(String url, Long userId) {
        String webhookUrl = n8nBaseUrl + "/webhook/scrape-webpage";
        
        Map<String, Object> request = Map.of(
            "url", url,
            "userId", userId,
            "tags", List.of("manual")
        );
        
        restTemplate.postForEntity(webhookUrl, request, String.class);
    }
    
    public QueryResponse queryRAG(String query, Long userId) {
        String webhookUrl = n8nBaseUrl + "/webhook/rag-query";
        
        Map<String, Object> request = Map.of(
            "query", query,
            "userId", userId,
            "topK", 5
        );
        
        ResponseEntity<QueryResponse> response = 
            restTemplate.postForEntity(webhookUrl, request, QueryResponse.class);
        
        return response.getBody();
    }
}
```

---

## ❓ 常见问题

### 1. 工作流无法连接 MySQL

**问题**: 工作流执行报错 "Cannot connect to MySQL"

**解决方案**:
1. 检查 MySQL 容器是否运行: `docker ps | grep mysql`
2. 确认凭据配置正确 (host: `mysql`, port: `3306`)
3. 在 n8n 容器中测试连接: `docker exec xu-news-n8n ping mysql`

### 2. Ollama 调用失败

**问题**: AI 生成摘要时报错 "Connection timeout"

**解决方案**:
1. 确认 Ollama 服务运行中
2. 检查 IP 地址是否正确 (Windows 主机: `http://192.168.171.1:11434`)
3. 测试连接: `curl http://192.168.171.1:11434/api/version`
4. 如果使用 Docker 内部网络,修改为 `http://ollama:11434`

### 3. 邮件发送失败

**问题**: 邮件通知工作流报错

**解决方案**:
1. 检查 SMTP 凭据配置
2. 确认邮箱服务允许第三方客户端
3. 尝试使用 Gmail/QQ邮箱/163邮箱
4. 检查防火墙是否阻止 SMTP 端口 (587/465)

### 4. RSS 采集重复内容

**问题**: 同一篇文章被重复采集

**解决方案**:
1. 检查去重逻辑是否正常
2. 在后端添加唯一索引: `CREATE UNIQUE INDEX idx_source_url ON knowledge_entry(source_url)`
3. 确认工作流中的去重检查节点运行正常

### 5. Webhook 调用 404 错误

**问题**: 调用 Webhook 返回 404

**解决方案**:
1. 确认工作流已激活 (Active 开关打开)
2. 检查 Webhook 路径是否正确
3. 查看 n8n 日志: `docker logs xu-news-n8n`
4. 重新保存并激活工作流

---

## 💡 最佳实践

### 1. 错误处理

所有工作流都内置了错误处理机制:
- 使用 **Error Trigger** 节点捕获错误
- 记录错误日志到控制台
- 通过 Webhook 返回友好的错误信息

建议:
- 监控 n8n 执行历史
- 关键失败发送邮件通知
- 设置自动重试机制

### 2. 性能优化

- **批量处理**: RSS 采集使用批量保存,减少 API 调用
- **超时设置**: HTTP 请求设置合理超时 (30-60秒)
- **并发控制**: 避免同时运行多个高负载工作流
- **缓存机制**: 频繁查询的向量可以缓存

### 3. 安全建议

- 🔒 启用 n8n Basic Auth 认证
- 🔒 使用环境变量存储敏感信息
- 🔒 Webhook 添加签名验证
- 🔒 限制 API 调用频率
- 🔒 生产环境使用 HTTPS

### 4. 监控和日志

```bash
# 查看 n8n 日志
docker logs -f xu-news-n8n

# 查看执行历史
# 访问 http://localhost:5678/executions

# 导出工作流备份
# Settings → Import/Export → Export workflow
```

### 5. 工作流版本管理

- 定期导出工作流 JSON 文件
- 使用 Git 管理工作流版本
- 测试环境验证后再部署到生产

---

## 📊 工作流架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        n8n 工作流引擎                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────┐      ┌──────────────────┐            │
│  │  RSS 新闻采集    │      │   网页抓取       │            │
│  │  (定时触发)      │      │  (Webhook)       │            │
│  └────────┬─────────┘      └────────┬─────────┘            │
│           │                         │                       │
│           └────────┬────────────────┘                       │
│                    │                                        │
│                    ↓                                        │
│           ┌────────────────┐                                │
│           │   Ollama AI    │                                │
│           │  (摘要生成)    │                                │
│           └────────┬───────┘                                │
│                    │                                        │
│                    ↓                                        │
│           ┌────────────────┐                                │
│           │  Spring Boot   │                                │
│           │   (数据存储)   │                                │
│           └────────────────┘                                │
│                                                             │
│  ┌──────────────────┐      ┌──────────────────┐            │
│  │  RAG 智能问答    │      │   文档处理       │            │
│  │  (Webhook)       │      │  (Webhook)       │            │
│  └────────┬─────────┘      └────────┬─────────┘            │
│           │                         │                       │
│           └────────┬────────────────┘                       │
│                    │                                        │
│                    ↓                                        │
│           ┌────────────────┐                                │
│           │   向量检索     │                                │
│           │   (FAISS)      │                                │
│           └────────────────┘                                │
│                                                             │
│  ┌──────────────────┐                                       │
│  │   邮件通知       │                                       │
│  │  (Webhook)       │                                       │
│  └──────────────────┘                                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 📝 配置检查清单

部署前请确认:

- [ ] n8n 服务正常运行 (`docker ps | grep n8n`)
- [ ] MySQL 凭据已配置
- [ ] SMTP 凭据已配置(如需邮件功能)
- [ ] Ollama 服务可访问
- [ ] 所有工作流已导入
- [ ] 所有工作流已激活
- [ ] Webhook URL 可以正常访问
- [ ] RSS 数据源已在数据库中配置
- [ ] 后端 API 接口正常

---

## 🔄 更新日志

### v1.0.0 (2025-10-16)

- ✅ 初始版本发布
- ✅ RSS 新闻采集工作流
- ✅ 网页抓取工作流
- ✅ RAG 智能问答工作流
- ✅ 文档处理工作流
- ✅ 邮件通知工作流

---

## 📞 技术支持

如有问题请查看:

- 📖 [项目文档](../README.md)
- 📖 [技术架构文档](../技术架构文档-XU-News-AI-RAG.md)
- 🐛 [GitHub Issues](https://github.com/xu/xu-ai-news-rag/issues)
- 📧 邮件: support@xu-news-rag.com

---

**XU-News-AI-RAG Project © 2025**

