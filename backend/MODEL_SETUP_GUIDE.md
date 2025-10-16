# XU-News-AI-RAG 模型配置指南

## 📦 模型概念说明

### 1️⃣ 嵌入模型（Embedding Model）= 向量模型

**作用：** 将文本转换为高维向量（数值数组），使计算机能理解文本语义

```
文本："什么是人工智能？"
     ↓ 嵌入模型
向量：[0.23, -0.45, 0.67, ..., 0.12]  # 768维浮点数数组
```

**为什么需要：** 
- RAG 系统通过向量相似度检索相关文档
- 语义相似的文本，向量距离更近

### 2️⃣ 重排模型（Reranker Model）

**作用：** 对初步检索的结果重新精确排序，提高准确率

```
检索阶段：向量相似度快速筛选出 Top 50 个候选文档 (召回)
重排阶段：精确计算相关性，选出最相关的 Top 5 (精排)
```

**为什么需要：** 
- 向量检索速度快但不够精确
- 重排模型计算量大但更准确
- 两者结合达到速度和精度的平衡

### 3️⃣ 大语言模型（LLM）

**作用：** 理解问题，基于检索的文档生成最终答案

```
用户问题 + 检索文档 → LLM → 流畅自然的回答
```

---

## 🚀 快速配置（3种方案）

### 方案一：Ollama 本地部署（推荐）

#### 1. 安装 Ollama

**Windows:**
```powershell
# 下载并安装 Ollama
# https://ollama.com/download

# 验证安装
ollama --version
```

**Linux/Mac:**
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

#### 2. 下载推荐模型

```bash
# 1. 嵌入模型（向量化）
ollama pull nomic-embed-text        # 768维，137MB，速度快
# 或者
ollama pull all-minilm:l6-v2       # 384维，更小但效果稍逊

# 2. 大语言模型（文本生成）
ollama pull qwen2.5:3b             # 中文友好，3B参数
# 或者
ollama pull qwen2.5:7b             # 效果更好，需要更多内存

# 3. 测试模型
ollama run qwen2.5:3b "你好"
```

#### 3. 配置项目

修改 `application.yml`:

```yaml
# Ollama配置
ollama:
  base-url: http://localhost:11434
  model:
    llm: qwen2.5:3b                      # 大语言模型
    embedding: nomic-embed-text:latest   # 嵌入模型（向量模型）
  timeout: 60000

# FAISS向量库配置
faiss:
  index-path: ./data/faiss_index
  dimension: 768  # 必须与嵌入模型维度匹配
```

**注意：** 
- `nomic-embed-text` 输出 768 维向量
- `all-minilm:l6-v2` 输出 384 维向量
- 修改模型时必须同时修改 `dimension` 配置

#### 4. 验证配置

```bash
# 测试嵌入模型
curl http://localhost:11434/api/embeddings \
  -d '{
    "model": "nomic-embed-text",
    "prompt": "测试文本"
  }'

# 测试 LLM
curl http://localhost:11434/api/generate \
  -d '{
    "model": "qwen2.5:3b",
    "prompt": "什么是RAG？",
    "stream": false
  }'
```

---

### 方案二：使用 Sentence-Transformers（Python）

#### 1. 创建 Python 服务

**创建 `embedding_service.py`:**

```python
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer, CrossEncoder
from typing import List
import numpy as np

app = FastAPI()

# 1. 加载嵌入模型
embedding_model = SentenceTransformer('all-MiniLM-L6-v2')  # 384维

# 2. 加载重排模型
reranker = CrossEncoder('cross-encoder/ms-marco-MiniLM-L-6-v2')

# 嵌入请求
class EmbeddingRequest(BaseModel):
    text: str

# 重排请求
class RerankRequest(BaseModel):
    query: str
    documents: List[str]

@app.post("/embed")
def embed(request: EmbeddingRequest):
    """生成文本嵌入向量"""
    try:
        embedding = embedding_model.encode(request.text)
        return {
            "embedding": embedding.tolist(),
            "dimension": len(embedding)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/rerank")
def rerank(request: RerankRequest):
    """对检索结果重新排序"""
    try:
        # 构建查询-文档对
        pairs = [[request.query, doc] for doc in request.documents]
        
        # 计算相关性分数
        scores = reranker.predict(pairs)
        
        # 按分数排序
        ranked_results = sorted(
            enumerate(scores), 
            key=lambda x: x[1], 
            reverse=True
        )
        
        return {
            "ranked_indices": [idx for idx, _ in ranked_results],
            "scores": [float(score) for _, score in ranked_results]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
```

#### 2. 安装依赖

```bash
pip install fastapi uvicorn sentence-transformers torch
```

#### 3. 启动服务

```bash
python embedding_service.py
```

#### 4. 修改 Java 代码

创建 `EmbeddingServiceClient.java`:

```java
@Component
public class EmbeddingServiceClient {
    
    @Value("${embedding.service.url}")
    private String serviceUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public List<Double> generateEmbedding(String text) {
        Map<String, String> request = Map.of("text", text);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            serviceUrl + "/embed", 
            request, 
            Map.class
        );
        
        return (List<Double>) response.getBody().get("embedding");
    }
    
    public List<Integer> rerank(String query, List<String> documents) {
        Map<String, Object> request = Map.of(
            "query", query,
            "documents", documents
        );
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            serviceUrl + "/rerank", 
            request, 
            Map.class
        );
        
        return (List<Integer>) response.getBody().get("ranked_indices");
    }
}
```

配置 `application.yml`:

```yaml
embedding:
  service:
    url: http://localhost:8000
```

---

### 方案三：使用云端 API（OpenAI / 智谱AI）

#### OpenAI 配置

```yaml
openai:
  api-key: ${OPENAI_API_KEY}
  base-url: https://api.openai.com/v1
  models:
    embedding: text-embedding-3-small  # 1536维
    llm: gpt-4o-mini
```

#### 智谱AI 配置（国内推荐）

```yaml
zhipu:
  api-key: ${ZHIPU_API_KEY}
  base-url: https://open.bigmodel.cn/api/paas/v4
  models:
    embedding: embedding-2  # 1024维
    llm: glm-4-flash
```

---

## 🔍 添加重排模型（Reranker）

### 为什么需要重排？

```
场景：用户问"Python如何读取文件？"

向量检索结果（Top 5）：
1. "Python文件操作详解" - 相似度 0.82
2. "文件系统原理"      - 相似度 0.79
3. "Python基础教程"    - 相似度 0.78
4. "读取Excel文件"     - 相似度 0.76
5. "Python文件读写"    - 相似度 0.75

重排后：
1. "Python文件读写"    - 精确相关度 0.95 ⬆️
2. "Python文件操作详解" - 精确相关度 0.91
3. "读取Excel文件"     - 精确相关度 0.73 ⬆️
4. "Python基础教程"    - 精确相关度 0.65
5. "文件系统原理"      - 精确相关度 0.58 ⬇️
```

### 实现步骤

#### 1. 修改 `QueryServiceImpl.java`

```java
@Service
public class QueryServiceImpl implements QueryService {
    
    @Autowired
    private OllamaClient ollamaClient;
    
    @Autowired
    private VectorStore vectorStore;
    
    @Autowired
    private KnowledgeEntryService knowledgeEntryService;
    
    @Autowired
    private EmbeddingServiceClient embeddingService;  // 新增
    
    @Override
    public QueryResponse query(QueryRequest request, Long userId) throws IOException {
        
        // Step 1: 向量检索（召回更多候选）
        List<Double> queryVector = ollamaClient.generateEmbedding(request.getQuery());
        
        // 检索 Top 20 个候选（而不是直接取 Top 5）
        int candidateCount = request.getTopK() * 4;  // 扩大召回范围
        List<VectorStore.SearchResult> searchResults = vectorStore.search(
            queryVector, 
            candidateCount
        );
        
        // Step 2: 获取候选文档
        List<Long> vectorIds = searchResults.stream()
            .map(VectorStore.SearchResult::getVectorId)
            .collect(Collectors.toList());
        
        List<KnowledgeEntry> entries = knowledgeEntryService.findByVectorIds(vectorIds);
        
        // Step 3: 重排序（精排）
        List<String> documents = entries.stream()
            .map(e -> e.getTitle() + "\n" + e.getContent())
            .collect(Collectors.toList());
        
        List<Integer> rankedIndices = embeddingService.rerank(
            request.getQuery(), 
            documents
        );
        
        // 按重排结果重新排序文档
        List<KnowledgeEntry> rerankedEntries = rankedIndices.stream()
            .limit(request.getTopK())  // 只取 Top K
            .map(entries::get)
            .collect(Collectors.toList());
        
        // Step 4: 构建 RAG 提示词
        List<String> context = rerankedEntries.stream()
            .map(KnowledgeEntry::getContent)
            .collect(Collectors.toList());
        
        // Step 5: 生成答案
        String answer = ollamaClient.generateAnswer(request.getQuery(), context);
        
        // 返回响应...
    }
}
```

---

## 📊 性能对比

| 方案 | 嵌入模型 | 向量维度 | 速度 | 效果 | 成本 |
|-----|---------|---------|------|------|------|
| **Ollama + nomic-embed-text** | nomic-embed-text | 768 | ⚡⚡⚡ | ⭐⭐⭐⭐ | 💰 免费 |
| **Sentence-Transformers** | all-MiniLM-L6-v2 | 384 | ⚡⚡⚡ | ⭐⭐⭐ | 💰 免费 |
| **OpenAI API** | text-embedding-3-small | 1536 | ⚡⚡ | ⭐⭐⭐⭐⭐ | 💰💰 $0.02/1M tokens |
| **智谱AI** | embedding-2 | 1024 | ⚡⚡ | ⭐⭐⭐⭐ | 💰 $0.005/1K tokens |

**推荐组合：**
- 💡 **开发/小型项目：** Ollama (nomic-embed-text + qwen2.5:3b)
- 🚀 **生产环境：** Ollama + Python 重排服务
- ☁️ **企业级：** OpenAI API / 智谱AI

---

## 🔧 故障排查

### 问题1: 向量维度不匹配

**错误：** `向量维度不匹配，期望: 768, 实际: 384`

**原因：** 更换了嵌入模型但未更新配置

**解决：**
```yaml
faiss:
  dimension: 384  # 改为新模型的维度
```

### 问题2: Ollama 调用失败

**检查清单：**
```bash
# 1. 检查 Ollama 是否运行
curl http://localhost:11434/api/version

# 2. 检查模型是否已下载
ollama list

# 3. 测试嵌入模型
ollama run nomic-embed-text
```

### 问题3: 检索效果不好

**优化建议：**
1. **增加向量维度：** 使用更大的嵌入模型
2. **添加重排模型：** 提高精确度
3. **调整检索参数：** 
   - 增加 `topK` 数量
   - 降低 `similarityThreshold`
4. **优化文本预处理：** 清理无用字符、分段等

---

## 📚 参考资源

- [Ollama 官方文档](https://ollama.com/library)
- [Sentence-Transformers 文档](https://www.sbert.net/)
- [FAISS 向量库](https://github.com/facebookresearch/faiss)
- [Hugging Face 模型库](https://huggingface.co/models)

---

**祝您配置顺利！如有问题欢迎反馈。🎉**

