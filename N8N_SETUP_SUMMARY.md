# XU-News-AI-RAG n8n 工作流配置完成

## ✅ 已完成内容

本次为 XU-News-AI-RAG 项目生成了完整的 n8n 工作流配置,包括:

### 📋 工作流文件 (5个)

所有工作流文件位于 `n8n-workflows/` 目录:

1. **rss-news-collector.json** - RSS 新闻采集工作流
   - 🕐 定时触发(每6小时)
   - 📰 从数据库读取 RSS 源配置
   - 🤖 AI 生成摘要
   - 💾 自动入库
   - ✅ 去重检查
   - 📊 统计结果

2. **webpage-scraper.json** - 网页抓取工作流
   - 🌐 Webhook 触发
   - 🔍 智能提取正文
   - 🤖 AI 生成摘要
   - 💾 保存到后端

3. **rag-qa-workflow.json** - RAG 智能问答工作流
   - 🤖 Webhook 触发
   - 🔢 查询向量化
   - 🔍 向量检索
   - 🧠 AI 生成答案
   - 🌍 联网搜索(当本地无结果时)

4. **document-processor.json** - 文档处理工作流
   - 📄 Webhook 触发
   - 📥 支持 PDF/Word/TXT
   - ✂️ 智能分段
   - 🤖 AI 生成摘要
   - 💾 批量入库

5. **email-notification.json** - 邮件通知工作流
   - 📧 Webhook 触发
   - 🎨 精美 HTML 邮件模板
   - 📊 支持多种通知类型:
     - 知识入库通知
     - 错误通知
     - 每日摘要

### 📚 文档文件 (3个)

1. **README.md** - 详细配置指南
   - 📖 概述和架构图
   - 🚀 快速开始步骤
   - ⚙️ 详细配置说明
   - 🔗 Webhook 端点文档
   - ❓ 常见问题解答
   - 💡 最佳实践

2. **N8N_GUIDE.md** - 使用指南
   - 🎯 3分钟快速开始
   - 📊 每个工作流的详细说明
   - 💻 代码集成示例
   - 🔧 高级配置
   - 🐛 故障排查
   - 📈 监控和维护

3. **setup-n8n.sh** - 自动配置脚本(Linux/Mac)
   - ✅ 自动检查服务状态
   - ✅ 验证文件完整性
   - ✅ 分步配置指导
   - ✅ 添加默认 RSS 源

---

## 🎯 快速开始(完整流程)

### 1. 启动 n8n 服务

```bash
# 确保在项目根目录
cd xu-ai-news-rag

# 启动所有服务(如果还没启动)
docker-compose up -d

# 或只启动 n8n
docker-compose up -d n8n

# 查看 n8n 日志确认启动成功
docker logs -f xu-news-n8n
```

### 2. 访问 n8n 管理界面

打开浏览器访问: **http://192.168.171.128:5678**

默认登录凭据:
- **用户名**: `admin`
- **密码**: `admin123`

### 3. 导入工作流

在 n8n 界面中:

1. 点击左侧菜单 **"Workflows"**
2. 点击右上角 **"Import from File"**
3. 依次导入 `n8n-workflows` 目录下的 5 个 JSON 文件:
   - rss-news-collector.json
   - webpage-scraper.json
   - rag-qa-workflow.json
   - document-processor.json
   - email-notification.json

### 4. 配置凭据

#### MySQL 数据库凭据

在 n8n 中添加凭据:
- **名称**: `MySQL - xu_news_rag`
- **类型**: MySQL
- **配置**:
  ```
  Host: mysql
  Port: 3306
  Database: xu_news_rag
  User: xu_news
  Password: xu_news_pass
  ```

#### SMTP 邮件凭据(可选)

如需邮件通知功能:
- **名称**: `SMTP - XU News`
- **类型**: SMTP
- **配置**: (根据你的邮箱服务商填写)
  ```
  Host: smtp.qq.com (QQ邮箱示例)
  Port: 587
  Secure: true
  User: your_email@qq.com
  Password: 授权码
  ```

### 5. 激活工作流

打开每个工作流,点击右上角的 **"Active"** 开关激活。

激活后:
- ✅ **RSS 新闻采集**: 会按定时任务自动运行
- ✅ **其他工作流**: 会生成 Webhook URL 供调用

### 6. 添加 RSS 数据源

在数据库中添加 RSS 订阅源:

```sql
-- 使用 MySQL 客户端或执行以下命令
docker exec xu-news-mysql mysql -u xu_news -pxu_news_pass xu_news_rag -e "
INSERT INTO data_source (name, type, url, status, created_at, updated_at) 
VALUES 
  ('TechCrunch', 'RSS', 'https://techcrunch.com/feed/', 1, NOW(), NOW()),
  ('The Verge', 'RSS', 'https://www.theverge.com/rss/index.xml', 1, NOW(), NOW()),
  ('Hacker News', 'RSS', 'https://news.ycombinator.com/rss', 1, NOW(), NOW());
"
```

### 7. 测试工作流

#### 测试 RSS 采集

1. 打开 "RSS新闻采集工作流"
2. 点击右上角 "Execute Workflow" 手动触发
3. 查看执行结果和日志
4. 检查数据库是否有新数据入库

#### 测试 RAG 问答

```bash
curl -X POST http://192.168.171.128:5678/webhook/rag-query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "什么是人工智能?",
    "userId": 1,
    "topK": 5
  }'
```

#### 测试网页抓取

```bash
curl -X POST http://192.168.171.128:5678/webhook/scrape-webpage \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://news.ycombinator.com/",
    "userId": 1,
    "tags": ["tech", "news"]
  }'
```

---

## 🔗 重要 URL 和端点

### n8n 管理界面
- **URL**: http://192.168.171.128:5678
- **账号**: admin / admin123

### Webhook 端点

| 功能 | Webhook URL |
|-----|-------------|
| 网页抓取 | `http://192.168.171.128:5678/webhook/scrape-webpage` |
| RAG 问答 | `http://192.168.171.128:5678/webhook/rag-query` |
| 文档处理 | `http://192.168.171.128:5678/webhook/process-document` |
| 邮件通知 | `http://192.168.171.128:5678/webhook/send-notification` |

### 其他服务

| 服务 | URL |
|-----|-----|
| 后端 API | http://192.168.171.128:8080 |
| 前端界面 | http://192.168.171.128:5173 |
| MySQL | 192.168.171.128:3306 |
| Ollama | http://192.168.171.1:11434 |

---

## 🏗️ n8n 在项目中的架构角色

```
┌──────────────────────────────────────────────────────┐
│                    用户/前端                          │
└────────────────┬─────────────────────────────────────┘
                 │
                 ↓
┌──────────────────────────────────────────────────────┐
│              Spring Boot 后端                         │
│  • 用户认证  • 知识库管理  • API 网关               │
└────────┬─────────────────────────────────┬───────────┘
         │                                 │
         ↓                                 ↓
┌────────────────────┐         ┌──────────────────────┐
│      n8n           │         │    MySQL + FAISS     │
│  工作流编排引擎    │         │    数据存储层        │
│                    │         └──────────────────────┘
│ • RSS 新闻采集    │
│ • 网页抓取        │
│ • RAG 问答        │
│ • 文档处理        │
│ • 邮件通知        │
└────────┬───────────┘
         │
         ↓
┌────────────────────┐
│   Ollama AI        │
│  • Qwen 2.5:3b     │
│  • all-MiniLM      │
└────────────────────┘
```

### n8n 的职责

1. **数据采集层**
   - 定时执行 RSS 采集任务
   - 接收网页抓取请求
   - 处理文档上传

2. **AI 处理层**
   - 调用 Ollama 生成摘要
   - 调用 Embedding 模型向量化
   - 执行 RAG 问答流程

3. **业务编排层**
   - 可视化配置业务流程
   - 错误处理和重试
   - 日志记录和监控

4. **通知服务层**
   - 发送邮件通知
   - 任务状态推送

---

## 💻 与 Spring Boot 集成

### 1. 添加配置

`application.yml`:

```yaml
n8n:
  enabled: true
  base-url: http://n8n:5678
  webhook:
    scrape-webpage: /webhook/scrape-webpage
    rag-query: /webhook/rag-query
    process-document: /webhook/process-document
    send-notification: /webhook/send-notification
```

### 2. 创建 N8n 服务类

```java
@Service
@Slf4j
public class N8nWebhookService {
    
    @Value("${n8n.base-url}")
    private String n8nBaseUrl;
    
    private final RestTemplate restTemplate;
    
    /**
     * 触发网页抓取
     */
    public void scrapeWebpage(String url, Long userId, List<String> tags) {
        String webhookUrl = n8nBaseUrl + "/webhook/scrape-webpage";
        
        Map<String, Object> request = Map.of(
            "url", url,
            "userId", userId,
            "tags", tags
        );
        
        try {
            restTemplate.postForEntity(webhookUrl, request, String.class);
            log.info("网页抓取任务已提交: {}", url);
        } catch (Exception e) {
            log.error("网页抓取失败", e);
        }
    }
    
    /**
     * RAG 智能问答
     */
    public QueryResponse queryRAG(String query, Long userId) {
        String webhookUrl = n8nBaseUrl + "/webhook/rag-query";
        
        Map<String, Object> request = Map.of(
            "query", query,
            "userId", userId,
            "topK", 5,
            "enableWebSearch", true
        );
        
        ResponseEntity<QueryResponse> response = 
            restTemplate.postForEntity(webhookUrl, request, QueryResponse.class);
        
        return response.getBody();
    }
    
    /**
     * 处理文档
     */
    public void processDocument(String fileUrl, String fileName, 
                                 String fileType, Long userId, List<String> tags) {
        String webhookUrl = n8nBaseUrl + "/webhook/process-document";
        
        Map<String, Object> request = Map.of(
            "fileUrl", fileUrl,
            "fileName", fileName,
            "fileType", fileType,
            "userId", userId,
            "tags", tags
        );
        
        restTemplate.postForEntity(webhookUrl, request, String.class);
    }
    
    /**
     * 发送通知
     */
    public void sendNotification(String type, String email, Map<String, Object> data) {
        String webhookUrl = n8nBaseUrl + "/webhook/send-notification";
        
        Map<String, Object> request = Map.of(
            "type", type,
            "recipientEmail", email,
            "data", data
        );
        
        restTemplate.postForEntity(webhookUrl, request, String.class);
    }
}
```

### 3. 在 Controller 中使用

```java
@RestController
@RequestMapping("/api/query")
public class QueryController {
    
    @Autowired
    private N8nWebhookService n8nService;
    
    @PostMapping
    public Result<QueryResponse> query(@RequestBody QueryRequest request) {
        QueryResponse response = n8nService.queryRAG(
            request.getQuery(), 
            request.getUserId()
        );
        
        return Result.success(response);
    }
}
```

---

## 📊 预期效果

配置完成后,系统会自动:

### ✅ 自动采集新闻
- 每6小时自动从配置的 RSS 源抓取新闻
- 自动生成中文摘要
- 自动向量化并存储到 FAISS
- 去重避免重复采集

### ✅ 智能问答
- 前端/后端可以通过 Webhook 调用 RAG 问答
- 自动检索知识库
- AI 生成专业回答
- 本地无结果时自动联网搜索

### ✅ 文档处理
- 用户上传 PDF/Word 等文档
- 自动解析提取内容
- 智能分段处理
- 生成摘要并向量化

### ✅ 邮件通知
- 新内容入库后发送通知
- 任务失败时发送告警
- 每日摘要推送(可选)

---

## 🔍 监控和调试

### 查看执行历史

访问: http://192.168.171.128:5678/executions

可以查看:
- ✅ 成功执行的工作流
- ❌ 失败执行的详情
- ⏱️ 执行时间
- 📋 详细日志

### 查看日志

```bash
# n8n 日志
docker logs -f xu-news-n8n

# 后端日志
docker logs -f xu-news-backend

# MySQL 日志
docker logs -f xu-news-mysql

# 查看所有容器状态
docker-compose ps
```

### 测试连接

```bash
# 测试 n8n 访问
curl http://192.168.171.128:5678

# 测试 Ollama
curl http://192.168.171.1:11434/api/version

# 测试 MySQL
docker exec xu-news-mysql mysql -u xu_news -pxu_news_pass -e "SELECT 1"

# 测试 Webhook
curl -X POST http://192.168.171.128:5678/webhook-test/rag-query \
  -H "Content-Type: application/json" \
  -d '{"query":"test"}'
```

---

## ⚠️ 注意事项

### 1. Ollama IP 地址

工作流中的 Ollama 地址默认为 `http://192.168.171.1:11434`

如果你的环境不同,请在每个工作流中修改:
- 打开工作流
- 找到 "AI生成摘要" 或 "查询向量化" 节点
- 修改 URL 为实际的 Ollama 地址

### 2. 首次运行可能较慢

- Ollama 首次生成需要加载模型(可能需要几秒)
- RSS 采集首次运行会处理较多历史文章
- 建议先手动测试单个工作流

### 3. 邮件功能需要配置

如需使用邮件通知功能,必须配置 SMTP 凭据:
- QQ邮箱: smtp.qq.com:587
- 163邮箱: smtp.163.com:25
- Gmail: smtp.gmail.com:587

并且需要使用授权码,不是密码!

### 4. 生产环境建议

- 🔒 修改 n8n 默认密码
- 🔒 启用 HTTPS
- 🔒 Webhook 添加签名验证
- 📊 配置监控告警
- 💾 定期备份工作流配置

---

## 📚 相关文档

- [n8n 工作流 README](./n8n-workflows/README.md) - 详细技术文档
- [n8n 使用指南](./n8n-workflows/N8N_GUIDE.md) - 实用指南
- [项目技术架构](./技术架构文档-XU-News-AI-RAG.md) - 整体架构
- [测试指南](./测试指南.md) - 测试说明

---

## ✅ 验收清单

配置完成后,请检查:

- [ ] n8n 服务正常运行 (`docker ps | grep n8n`)
- [ ] 可以访问 n8n 管理界面 (http://192.168.171.128:5678)
- [ ] 5 个工作流都已导入
- [ ] MySQL 凭据配置正确
- [ ] 所有工作流都已激活
- [ ] Webhook URL 可以正常访问
- [ ] RSS 数据源已添加
- [ ] RSS 采集工作流测试成功
- [ ] RAG 问答 Webhook 测试成功
- [ ] 能看到执行历史记录

---

## 🎉 完成!

n8n 工作流配置已全部完成!现在你的 XU-News-AI-RAG 项目具备了:

✅ **自动化新闻采集能力**
✅ **智能 RAG 问答能力**
✅ **文档处理能力**
✅ **通知服务能力**
✅ **可视化工作流管理**

开始使用吧! 🚀

---

**项目**: XU-News-AI-RAG  
**配置完成时间**: 2025-10-16  
**版本**: v1.0.0

