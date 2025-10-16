# XU-News-AI-RAG n8n 使用指南

## 🎯 快速开始(3分钟)

### 方式一:自动配置脚本

```bash
cd n8n-workflows
chmod +x setup-n8n.sh
./setup-n8n.sh
```

脚本会自动:
- ✅ 检查 n8n 服务状态
- ✅ 验证工作流文件完整性
- ✅ 提供分步配置指导
- ✅ 添加默认 RSS 数据源

### 方式二:手动配置

#### 1. 启动 n8n

```bash
# 确保在项目根目录
cd xu-ai-news-rag

# 启动 n8n 服务
docker-compose up -d n8n

# 查看日志
docker logs -f xu-news-n8n
```

#### 2. 访问管理界面

打开浏览器访问: **http://192.168.171.128:5678**

默认登录凭据:
- 用户名: `admin`
- 密码: `admin123`

#### 3. 导入工作流

依次导入 `n8n-workflows` 目录下的 5 个 JSON 文件:

1. `rss-news-collector.json` - RSS新闻采集
2. `webpage-scraper.json` - 网页抓取
3. `rag-qa-workflow.json` - RAG智能问答
4. `document-processor.json` - 文档处理  
5. `email-notification.json` - 邮件通知

导入步骤:
- 点击 "Workflows" → "Import from File"
- 选择 JSON 文件
- 点击 "Import"

#### 4. 配置凭据

##### MySQL 凭据

名称: `MySQL - xu_news_rag`

```
Host: mysql
Port: 3306
Database: xu_news_rag
User: xu_news
Password: xu_news_pass
```

##### SMTP 凭据(可选)

名称: `SMTP - XU News`

```
Host: smtp.qq.com (或其他邮箱服务商)
Port: 587
Secure: true
User: your_email@qq.com
Password: 授权码(不是密码)
```

**QQ邮箱授权码获取:**
1. 登录 QQ 邮箱网页版
2. 设置 → 账户 → POP3/IMAP/SMTP
3. 开启服务并生成授权码

#### 5. 激活工作流

打开每个工作流,点击右上角 **"Active"** 开关激活。

---

## 📊 工作流详解

### 1️⃣ RSS 新闻采集工作流

**触发方式:** 定时触发(每6小时)

**功能:** 自动从配置的 RSS 源抓取新闻,生成摘要并入库

**配置要点:**

```javascript
// 修改定时频率
Cron 表达式: 0 */6 * * *

// 每小时: 0 * * * *
// 每天凌晨2点: 0 2 * * *
// 每周一早上9点: 0 9 * * 1
```

**添加 RSS 源:**

```sql
-- 在数据库中执行
INSERT INTO data_source (name, type, url, status, created_at, updated_at) 
VALUES ('36氪', 'RSS', 'https://36kr.com/feed', 1, NOW(), NOW());
```

**手动触发测试:**
- 打开工作流
- 点击 "Execute Workflow"
- 查看执行结果

---

### 2️⃣ 网页抓取工作流

**触发方式:** Webhook

**Webhook URL:** `http://localhost:5678/webhook/scrape-webpage`

**使用示例:**

```bash
# 抓取单个网页
curl -X POST http://192.168.171.128:5678/webhook/scrape-webpage \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://www.infoq.cn/article/xxx",
    "selector": "article",
    "userId": 1,
    "tags": ["tech", "infoq"]
  }'
```

**Java 调用示例:**

```java
@Service
public class WebScraperService {
    
    @Value("${n8n.webhook.base-url}")
    private String n8nBaseUrl;
    
    private final RestTemplate restTemplate;
    
    public void scrapeWebpage(String url, Long userId, List<String> tags) {
        String webhookUrl = n8nBaseUrl + "/webhook/scrape-webpage";
        
        Map<String, Object> request = Map.of(
            "url", url,
            "selector", "article",
            "userId", userId,
            "tags", tags
        );
        
        ResponseEntity<String> response = 
            restTemplate.postForEntity(webhookUrl, request, String.class);
        
        log.info("网页抓取结果: {}", response.getBody());
    }
}
```

**application.yml 配置:**

```yaml
n8n:
  webhook:
    base-url: http://localhost:5678
```

---

### 3️⃣ RAG 智能问答工作流

**触发方式:** Webhook

**Webhook URL:** `http://localhost:5678/webhook/rag-query`

**使用示例:**

```bash
# 发起查询
curl -X POST http://192.168.171.128:5678/webhook/rag-query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "什么是向量数据库?",
    "userId": 1,
    "topK": 5,
    "enableWebSearch": true
  }'
```

**响应示例:**

```json
{
  "query": "什么是向量数据库?",
  "answer": "向量数据库是专门用于存储和检索高维向量的数据库系统...",
  "retrievedDocs": [
    {
      "title": "向量数据库详解",
      "content": "...",
      "sourceUrl": "https://...",
      "similarity": 0.89
    }
  ],
  "source": "knowledge_base",
  "timestamp": "2025-10-16T12:00:00Z"
}
```

**集成到 Spring Boot Controller:**

```java
@RestController
@RequestMapping("/api/query")
public class QueryController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${n8n.webhook.base-url}")
    private String n8nBaseUrl;
    
    @PostMapping("/ask")
    public ResponseEntity<QueryResponse> query(@RequestBody QueryRequest request) {
        String webhookUrl = n8nBaseUrl + "/webhook/rag-query";
        
        Map<String, Object> payload = Map.of(
            "query", request.getQuery(),
            "userId", request.getUserId(),
            "topK", 5
        );
        
        ResponseEntity<QueryResponse> response = 
            restTemplate.postForEntity(webhookUrl, payload, QueryResponse.class);
        
        return response;
    }
}
```

---

### 4️⃣ 文档处理工作流

**触发方式:** Webhook

**Webhook URL:** `http://localhost:5678/webhook/process-document`

**支持格式:** PDF, Word, TXT

**使用流程:**

1. 用户通过前端上传文件到后端
2. 后端保存文件并生成访问 URL
3. 后端调用 n8n Webhook 处理文件
4. n8n 下载、解析、向量化、入库

**后端实现示例:**

```java
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private N8nWebhookService n8nService;
    
    @PostMapping("/upload")
    public ResponseEntity<Result> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tags") String tagsJson) {
        
        // 1. 保存文件
        String fileName = fileStorageService.save(file);
        String fileUrl = "http://backend:8080/uploads/" + fileName;
        
        // 2. 解析标签
        List<String> tags = JSON.parseArray(tagsJson, String.class);
        
        // 3. 调用 n8n 处理
        n8nService.processDocument(fileUrl, fileName, getFileType(file), 
                                    getUserId(), tags);
        
        return Result.success("文档上传成功,正在处理中...");
    }
    
    private String getFileType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    }
}
```

---

### 5️⃣ 邮件通知工作流

**触发方式:** Webhook

**Webhook URL:** `http://localhost:5678/webhook/send-notification`

**通知类型:**

1. **知识入库通知** (`knowledge_import`)
2. **错误通知** (`error`)
3. **每日摘要** (`daily_digest`)

**使用示例:**

```bash
# 发送知识入库通知
curl -X POST http://192.168.171.128:5678/webhook/send-notification \
  -H "Content-Type: application/json" \
  -d '{
    "type": "knowledge_import",
    "recipientEmail": "user@example.com",
    "data": {
      "title": "GPT-4 最新进展",
      "source": "TechCrunch",
      "contentType": "news",
      "summary": "OpenAI 发布了 GPT-4 的最新更新..."
    }
  }'
```

**集成到业务代码:**

```java
@Service
public class NotificationService {
    
    @Autowired
    private N8nWebhookService n8nService;
    
    // 知识入库后发送通知
    public void notifyKnowledgeImport(KnowledgeEntry entry, String userEmail) {
        Map<String, Object> data = Map.of(
            "title", entry.getTitle(),
            "source", entry.getDataSource(),
            "contentType", entry.getContentType(),
            "summary", entry.getSummary()
        );
        
        n8nService.sendNotification("knowledge_import", userEmail, data);
    }
    
    // 每日摘要(定时任务调用)
    @Scheduled(cron = "0 0 9 * * ?") // 每天早上9点
    public void sendDailyDigest() {
        List<User> users = userService.getAllActiveUsers();
        
        for (User user : users) {
            // 统计用户数据
            Map<String, Object> stats = knowledgeService.getDailyStats(user.getId());
            
            n8nService.sendNotification("daily_digest", user.getEmail(), stats);
        }
    }
}
```

---

## 🔧 高级配置

### 自定义 Ollama 地址

如果 Ollama 部署在其他主机:

1. 打开工作流
2. 找到 "AI生成摘要" 或 "查询向量化" 节点
3. 修改 URL: `http://your-ollama-host:11434/api/generate`

### 调整并发和性能

```javascript
// 在 Code 节点中添加延迟,避免请求过快
await new Promise(resolve => setTimeout(resolve, 1000)); // 延迟1秒
```

### 添加请求签名验证

在 Webhook 节点后添加验证逻辑:

```javascript
// Code 节点
const body = $input.item.json.body;
const signature = $input.item.json.headers['x-signature'];
const secret = 'your-secret-key';

const crypto = require('crypto');
const expectedSignature = crypto
    .createHmac('sha256', secret)
    .update(JSON.stringify(body))
    .digest('hex');

if (signature !== expectedSignature) {
    throw new Error('Invalid signature');
}

return body;
```

---

## 🐛 故障排查

### 问题1: 工作流执行失败

**排查步骤:**

```bash
# 1. 查看 n8n 日志
docker logs -f xu-news-n8n

# 2. 检查依赖服务
docker ps | grep xu-news

# 3. 测试网络连通性
docker exec xu-news-n8n ping mysql
docker exec xu-news-n8n ping backend

# 4. 查看 MySQL 连接
docker exec xu-news-n8n nslookup mysql
```

### 问题2: Ollama 调用超时

```bash
# 1. 检查 Ollama 服务
curl http://192.168.171.1:11434/api/version

# 2. 测试模型
curl http://192.168.171.1:11434/api/generate \
  -d '{"model":"qwen2.5:3b","prompt":"你好"}'

# 3. 查看 Ollama 日志
ollama logs

# 4. 重启 Ollama
ollama serve
```

### 问题3: Webhook 无响应

**检查清单:**

- [ ] 工作流是否已激活? (Active 开关)
- [ ] Webhook URL 是否正确?
- [ ] 请求格式是否正确? (JSON)
- [ ] n8n 服务是否运行正常?

```bash
# 测试 Webhook
curl -v http://192.168.171.128:5678/webhook-test/test

# 查看 n8n 执行历史
# 访问: http://192.168.171.128:5678/executions
```

---

## 📈 监控和维护

### 查看执行历史

访问: **http://192.168.171.128:5678/executions**

可以查看:
- ✅ 成功的执行
- ❌ 失败的执行
- ⏱️ 执行时间
- 📊 执行详情

### 导出工作流备份

```bash
# 在 n8n 界面中
Settings → Import/Export → Export all workflows

# 或使用 API
curl -u admin:admin123 http://localhost:5678/rest/workflows > workflows-backup.json
```

### 定期清理执行历史

```bash
# 进入 n8n 容器
docker exec -it xu-news-n8n sh

# 删除30天前的执行记录
# (n8n 会自动清理,也可以手动设置)
```

---

## 🎓 学习资源

### 官方文档

- [n8n 官方文档](https://docs.n8n.io/)
- [n8n Workflow 示例](https://n8n.io/workflows)
- [n8n 社区论坛](https://community.n8n.io/)

### 视频教程

- [n8n 入门教程](https://www.youtube.com/c/n8n-io)
- [构建自动化工作流](https://www.youtube.com/watch?v=example)

### 项目文档

- [项目 README](../README.md)
- [技术架构文档](../技术架构文档-XU-News-AI-RAG.md)
- [测试指南](../测试指南.md)

---

## 💡 最佳实践

### 1. 工作流设计原则

- ✅ **单一职责**: 每个工作流专注一个功能
- ✅ **错误处理**: 所有工作流都应有错误处理节点
- ✅ **日志记录**: 关键步骤记录日志便于调试
- ✅ **幂等性**: 避免重复执行产生副作用

### 2. 性能优化

- 🚀 使用批量操作减少 API 调用
- 🚀 合理设置超时时间
- 🚀 避免过度复杂的工作流
- 🚀 定期清理执行历史

### 3. 安全建议

- 🔒 修改默认密码
- 🔒 使用环境变量存储敏感信息
- 🔒 Webhook 添加签名验证
- 🔒 生产环境启用 HTTPS

---

## 📞 获取帮助

遇到问题?

1. 📖 查看 [README.md](./README.md) 详细文档
2. 🔍 搜索 [n8n 社区](https://community.n8n.io/)
3. 🐛 提交 [GitHub Issue](https://github.com/xu/xu-ai-news-rag/issues)
4. 📧 邮件: support@xu-news-rag.com

---

**祝你使用愉快! 🎉**

