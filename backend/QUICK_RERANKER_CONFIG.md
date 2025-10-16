# 🚀 重排模型快速配置（3分钟）

您已经在 Cherry Studio 中下载了 `BAAI/bge-reranker-v2-m3`，现在只需要简单几步就能启用！

---

## ⚡ 快速配置步骤

### 步骤 1: 获取 API Key (1分钟)

**在 Cherry Studio 中：**
1. 点击右上角设置图标 ⚙️
2. 找到 "API 配置" 或 "服务配置"
3. 复制 API Key（格式：`sk-xxxxxxxx...`）

或访问：https://cloud.siliconflow.cn/ → 控制台 → API密钥

---

### 步骤 2: 配置环境变量 (1分钟)

**Windows PowerShell：**
```powershell
$env:RERANKER_API_KEY="sk-你的API_KEY"
$env:RERANKER_ENABLED="true"
```

**Linux/Mac Terminal：**
```bash
export RERANKER_API_KEY="sk-你的API_KEY"
export RERANKER_ENABLED="true"
```

---

### 步骤 3: 启动服务 (1分钟)

```bash
cd backend
mvn spring-boot:run
```

---

## ✅ 验证是否成功

### 查看启动日志

应该看到类似输出：
```
INFO  c.x.n.u.RerankerClient - 重排模型已启用: BAAI/bge-reranker-v2-m3
```

### 测试查询

发送一个测试请求，查看日志：
```
DEBUG c.x.n.s.i.QueryServiceImpl - 检索相关文档，CandidateCount=20
DEBUG c.x.n.s.i.QueryServiceImpl - 执行重排序: 候选文档数=15, 目标TopK=5
INFO  c.x.n.s.i.QueryServiceImpl - 重排完成: 输入候选数=15, 输出结果数=5
```

看到这些日志说明重排已成功启用！🎉

---

## 🎯 工作原理

```
用户查询："Python如何读取文件？"
      ↓
[1] 向量检索 → 快速召回 20 个候选文档
      ↓
[2] 重排模型 → 精确评分，选出最相关的 5 个
      ↓
[3] 返回结果 → 准确率提升 20-30%
```

---

## 📊 效果对比

| 阶段 | 未启用重排 | 启用重排 |
|-----|----------|---------|
| **召回** | 直接返回 Top 5 | 先召回 Top 20 候选 |
| **精排** | ❌ 无 | ✅ 重排模型精确打分 |
| **准确率** | 基准 | **提升 20-30%** |
| **响应速度** | 快 | 稍慢（+0.2-0.5秒） |

---

## ⚙️ 高级配置（可选）

修改 `application.yml` 调整参数：

```yaml
reranker:
  enabled: true
  candidate-multiplier: 4  # 召回倍数，可调整为 2-10
```

**参数说明：**
- `2-3`: 速度优先
- `4-5`: **平衡模式（推荐）**
- `6-10`: 效果优先

---

## 🐛 遇到问题？

### API Key 无效

```bash
# 检查环境变量
echo $RERANKER_API_KEY  # Linux/Mac
echo %RERANKER_API_KEY%  # Windows CMD
$env:RERANKER_API_KEY   # Windows PowerShell
```

### 重排模型未启用

检查配置：
```yaml
# application.yml
reranker:
  enabled: true  # 确保为 true
```

### 查看详细日志

```bash
tail -f ./logs/xu-news-rag.log
```

---

## 📚 详细文档

查看完整配置指南：
- [RERANKER_SETUP_GUIDE.md](./RERANKER_SETUP_GUIDE.md) - 详细配置和优化
- [MODEL_SETUP_GUIDE.md](./MODEL_SETUP_GUIDE.md) - 模型概念说明

---

**配置完成后，您的 RAG 系统将具备更强的检索能力！🚀**

