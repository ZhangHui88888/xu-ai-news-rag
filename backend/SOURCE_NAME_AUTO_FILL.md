# 来源名称自动填充功能

## 📋 功能说明

系统现在会自动为导入的知识条目设置来源名称，避免出现空值。

## ✨ 自动填充规则

### 1. N8N导入
- **接口**：`POST /api/knowledge/import`
- **规则**：如果请求中没有提供 `sourceName` 或值为空，自动设置为 **"n8n自动读取"**
- **代码位置**：`KnowledgeController.java` 的 `importFromN8N` 方法

```java
// 设置来源名称，如果为空则使用默认值
String sourceName = (String) rawData.get("sourceName");
if (sourceName == null || sourceName.isEmpty()) {
    sourceName = "n8n自动读取";
}
entry.setSourceName(sourceName);
```

### 2. 文件上传
- **接口**：`POST /api/knowledge/upload`
- **规则**：自动设置为 **"用户上传"**
- **代码位置**：`KnowledgeEntryServiceImpl.java` 的 `createFromUploadedFile` 方法

```java
KnowledgeEntry entry = new KnowledgeEntry()
    .setTitle(originalFilename)
    .setContent(content)
    .setContentType("document")
    .setSourceName("用户上传");  // 标记来源
```

### 3. 手动创建
- **接口**：`POST /api/knowledge`
- **规则**：使用客户端提供的值，如果客户端没提供则为空（可以在前端设置默认值）

## 🔧 现有数据处理

如果您的数据库中已经有来源为空的记录，可以执行以下SQL脚本进行批量更新：

```bash
# 在虚拟机中执行
docker exec -i xu-news-mysql mysql -u xu_news -pxu_news_pass xu_news_rag < database/update_empty_source_names.sql
```

或者直接连接数据库执行：

```sql
UPDATE knowledge_entry 
SET source_name = 'n8n自动读取'
WHERE (source_name IS NULL OR source_name = '') 
  AND deleted = 0;
```

## 📊 数据统计

更新后，您可以通过以下SQL查看来源分布：

```sql
SELECT 
    source_name,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM knowledge_entry WHERE deleted = 0), 2) as percentage
FROM knowledge_entry 
WHERE deleted = 0
GROUP BY source_name
ORDER BY count DESC;
```

## 🚀 部署说明

### 修改的文件
```
backend/
└── src/main/java/com/xu/news/controller/KnowledgeController.java

database/
└── update_empty_source_names.sql (新增)
```

### 部署步骤

1. **重启后端服务**：
```bash
cd /path/to/xu-ai-news-rag
docker-compose up -d --build backend
```

2. **更新现有数据（可选）**：
```bash
docker exec -i xu-news-mysql mysql -u xu_news -pxu_news_pass xu_news_rag < database/update_empty_source_names.sql
```

## 📝 N8N工作流配置建议

在N8N工作流中，建议在发送到后端API之前添加 `sourceName` 字段：

### 示例1：RSS采集工作流
```json
{
  "title": "{{ $json.title }}",
  "content": "{{ $json.content }}",
  "summary": "{{ $json.summary }}",
  "sourceUrl": "{{ $json.link }}",
  "sourceName": "RSS订阅源",  // 添加这个字段
  "author": "{{ $json.author }}",
  "contentType": "news"
}
```

### 示例2：网页抓取工作流
```json
{
  "title": "{{ $json.title }}",
  "content": "{{ $json.content }}",
  "sourceUrl": "{{ $json.url }}",
  "sourceName": "网页抓取",  // 添加这个字段
  "contentType": "article"
}
```

### 示例3：动态来源
如果您想根据RSS源设置不同的来源名称：

```json
{
  "title": "{{ $json.title }}",
  "content": "{{ $json.content }}",
  "sourceName": "{{ $json.feed_title || 'RSS订阅' }}",  // 动态来源
  "contentType": "news"
}
```

## ⚠️ 注意事项

1. **向后兼容**：修改后仍然支持在请求中提供 `sourceName`，只是在没有提供时才使用默认值
2. **历史数据**：已有的空来源数据不会自动更新，需要手动执行SQL脚本
3. **优先级**：如果N8N工作流中设置了 `sourceName`，将使用工作流中的值而不是默认值

## 🎯 效果对比

### 修改前
| ID | 标题 | 来源 | 类型 |
|----|------|------|------|
| 28 | 苹果推出新品 | (空) | news |
| 29 | 借助语言模型 | (空) | news |

### 修改后（新导入的数据）
| ID | 标题 | 来源 | 类型 |
|----|------|------|------|
| 28 | 苹果推出新品 | (空) | news |
| 29 | 借助语言模型 | (空) | news |
| 40 | 最新科技动态 | n8n自动读取 | news |
| 41 | AI技术突破 | n8n自动读取 | news |

### 执行SQL更新后
| ID | 标题 | 来源 | 类型 |
|----|------|------|------|
| 28 | 苹果推出新品 | n8n自动读取 | news |
| 29 | 借助语言模型 | n8n自动读取 | news |
| 40 | 最新科技动态 | n8n自动读取 | news |
| 41 | AI技术突破 | n8n自动读取 | news |

