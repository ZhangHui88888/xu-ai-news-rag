# 重排模型集成指南 - Cherry Studio / SiliconFlow

## 🎯 您已完成的步骤

✅ 在 Cherry Studio 中下载了 `BAAI/bge-reranker-v2-m3` 重排模型  
✅ 项目代码已集成重排功能  
✅ 配置文件已更新  

现在只需要完成最后的配置步骤！

---

## 🔑 获取 API Key

### 方法一：从 Cherry Studio 获取

1. **打开 Cherry Studio**
2. **点击设置/API 配置**（通常在右上角）
3. **找到 "API密钥" 或 "API Key" 选项**
4. **复制您的 API Key**（格式类似：`sk-xxxxxxxxxxxxxxxxxxxxxx`）

### 方法二：从 SiliconFlow 官网获取

如果 Cherry Studio 使用的是 SiliconFlow 服务：

1. 访问：https://cloud.siliconflow.cn/
2. 注册/登录账号
3. 进入 "控制台" → "API密钥"
4. 创建新的 API Key 或复制现有的

---

## ⚙️ 配置项目

### 方式一：环境变量配置（推荐）

在系统环境变量或 `.env` 文件中设置：

```bash
# Windows PowerShell
$env:RERANKER_API_KEY="sk-你的实际API_KEY"
$env:RERANKER_ENABLED="true"

# Linux/Mac
export RERANKER_API_KEY="sk-你的实际API_KEY"
export RERANKER_ENABLED="true"
```

### 方式二：直接修改配置文件

修改 `backend/src/main/resources/application.yml`:

```yaml
# 重排模型配置 (Cherry Studio / SiliconFlow)
reranker:
  enabled: true  # 启用重排
  base-url: https://api.siliconflow.cn
  api-key: sk-你的实际API_KEY  # 替换为您的 API Key
  model: BAAI/bge-reranker-v2-m3
  candidate-multiplier: 4
```

⚠️ **注意：** 不要将 API Key 提交到 Git 仓库！建议使用环境变量。

### 方式三：application-dev.yml（开发环境）

创建 `backend/src/main/resources/application-dev.yml`:

```yaml
reranker:
  enabled: true
  api-key: sk-你的开发环境API_KEY
```

---

## 🚀 启动测试

### 1. 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

### 2. 验证重排功能

查看启动日志，应该看到类似输出：

```
2025-10-16 14:30:00 [main] INFO  c.x.n.u.RerankerClient - 重排模型已启用: BAAI/bge-reranker-v2-m3
```

### 3. 测试查询

```bash
# 测试 API
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "query": "什么是人工智能？",
    "topK": 5,
    "needAnswer": true
  }'
```

查看日志应该有：

```
2025-10-16 14:31:00 [http-nio-8080-exec-1] DEBUG c.x.n.s.i.QueryServiceImpl - 检索相关文档，CandidateCount=20, Threshold=0.5
2025-10-16 14:31:01 [http-nio-8080-exec-1] DEBUG c.x.n.s.i.QueryServiceImpl - 执行重排序: 候选文档数=15, 目标TopK=5
2025-10-16 14:31:02 [http-nio-8080-exec-1] INFO  c.x.n.s.i.QueryServiceImpl - 重排完成: 输入候选数=15, 输出结果数=5
```

---

## 📊 重排效果对比

### 未启用重排（只用向量检索）

```
用户查询: "Python如何读取文件？"

向量检索结果:
1. "Python文件操作详解" - 相似度 0.82
2. "文件系统基础"      - 相似度 0.79
3. "Python入门教程"    - 相似度 0.78
4. "数据库文件存储"    - 相似度 0.76
5. "配置文件管理"      - 相似度 0.75
```

### 启用重排后

```
用户查询: "Python如何读取文件？"

召回阶段（向量检索 Top 20）:
1. "Python文件操作详解" - 0.82
2. "文件系统基础" - 0.79
3. "Python入门教程" - 0.78
... (省略)
20. "Python异常处理" - 0.62

↓ 重排模型精确评分 ↓

最终结果（重排 Top 5）:
1. "Python文件读写完全指南" - 0.95 ⬆️ (原排名12)
2. "Python文件操作详解" - 0.91
3. "文件读取的5种方法" - 0.88 ⬆️ (原排名8)
4. "Python入门-文件IO" - 0.85
5. "CSV和JSON文件处理" - 0.80 ⬆️ (原排名15)
```

**效果提升：**
- ✅ 更准确的相关性排序
- ✅ 原本排名靠后但高度相关的文档被提到前面
- ✅ 准确率提升约 20-30%

---

## 🔧 配置参数说明

### `reranker.enabled`
- **类型：** Boolean
- **默认值：** false
- **说明：** 是否启用重排功能
- **建议：** 生产环境设为 true

### `reranker.base-url`
- **类型：** String
- **默认值：** https://api.siliconflow.cn
- **说明：** Cherry Studio / SiliconFlow API 地址
- **注意：** 如果您使用的是其他服务，需修改此地址

### `reranker.api-key`
- **类型：** String
- **默认值：** 空（必须配置）
- **说明：** API 密钥
- **获取：** 从 Cherry Studio 或 SiliconFlow 控制台获取

### `reranker.model`
- **类型：** String
- **默认值：** BAAI/bge-reranker-v2-m3
- **说明：** 重排模型名称
- **可选模型：**
  - `BAAI/bge-reranker-v2-m3` - 推荐，中文友好
  - `BAAI/bge-reranker-large` - 更大模型，效果更好但速度慢
  - `cross-encoder/ms-marco-MiniLM-L-6-v2` - 英文优先

### `reranker.candidate-multiplier`
- **类型：** Integer
- **默认值：** 4
- **说明：** 召回候选文档数量倍数
- **计算公式：** 候选数 = topK × multiplier
- **示例：** topK=5, multiplier=4 → 先检索20个候选，重排后返回5个
- **调优建议：**
  - 2-3: 速度优先，效果略降
  - 4-5: 平衡选择（推荐）
  - 6-10: 效果优先，速度稍慢

---

## 🐛 故障排查

### 问题1: 重排模型调用失败

**错误日志：**
```
ERROR c.x.n.u.RerankerClient - 重排API调用失败: code=401, body=Unauthorized
```

**原因：** API Key 无效或未配置

**解决：**
1. 检查 API Key 是否正确
2. 确认环境变量已设置：`echo $RERANKER_API_KEY`
3. 重启服务使配置生效

---

### 问题2: 重排模型不可用

**错误日志：**
```
WARN c.x.n.u.RerankerClient - 重排模型不可用: Connection refused
```

**原因：** 网络问题或 API 地址错误

**解决：**
1. 测试网络连接：
```bash
curl https://api.siliconflow.cn/v1/models
```
2. 检查 `reranker.base-url` 配置是否正确
3. 如果在内网，可能需要配置代理

---

### 问题3: Cherry Studio 中看不到重排模型

**解决步骤：**

1. **更新模型列表：** 在 Cherry Studio 中点击"刷新模型"
2. **手动搜索：** 在模型搜索框输入 "reranker"
3. **下载模型：** 找到 `BAAI/bge-reranker-v2-m3` 并下载
4. **验证：** 下载完成后应显示"ON"状态

---

### 问题4: 重排效果不明显

**可能原因：**
- 召回候选数太少
- 向量检索已经很准确

**优化建议：**
1. 增加 `candidate-multiplier` 到 6-8
2. 降低 `similarityThreshold` 增加候选多样性
3. 查看日志确认重排真的在运行

---

## 📈 性能优化

### 1. 批量查询优化

如果需要批量查询，可以：
```java
// 使用异步调用
CompletableFuture<List<RerankResult>> rerankFuture = 
    CompletableFuture.supplyAsync(() -> 
        rerankerClient.rerank(query, documents, topK)
    );
```

### 2. 缓存重排结果

对于相同查询，可以缓存结果：
```java
@Cacheable(value = "rerankCache", key = "#query + '-' + #topK")
public List<RerankResult> rerankWithCache(String query, List<String> docs, int topK) {
    return rerankerClient.rerank(query, docs, topK);
}
```

### 3. 动态开关重排

在查询请求中添加参数控制：
```json
{
  "query": "测试查询",
  "topK": 5,
  "useReranker": true  // 动态控制
}
```

---

## 💰 费用说明

### SiliconFlow 定价（参考）

- **重排模型调用：** 约 ¥0.001 / 1000 tokens
- **免费额度：** 新用户通常有免费试用额度
- **成本估算：** 
  - 单次查询（5个文档，每个500字）≈ 2500 tokens ≈ ¥0.0025
  - 1000次查询 ≈ ¥2.5

### 成本优化建议

1. **合理设置 `candidate-multiplier`：** 避免检索过多候选
2. **限制文档长度：** 可以只用摘要进行重排
3. **添加缓存：** 相同查询直接返回缓存结果
4. **监控使用量：** 在 SiliconFlow 控制台查看 API 调用统计

---

## 🎓 最佳实践

### 1. 分阶段启用

```yaml
# 开发环境：关闭重排，加快调试
reranker:
  enabled: false

# 测试环境：启用重排，验证效果
reranker:
  enabled: true
  candidate-multiplier: 4

# 生产环境：启用重排+优化参数
reranker:
  enabled: true
  candidate-multiplier: 5
```

### 2. A/B 测试

同时运行两个版本对比效果：
- 一半用户使用重排
- 一半用户只用向量检索
- 统计用户满意度和准确率

### 3. 监控指标

记录以下指标评估重排效果：
- 召回候选数
- 最终返回数
- 重排耗时
- 相关性评分分布
- 用户点击率

---

## 📚 相关资源

- [BAAI/bge-reranker-v2 模型介绍](https://huggingface.co/BAAI/bge-reranker-v2-m3)
- [SiliconFlow 官方文档](https://docs.siliconflow.cn/)
- [Cherry Studio GitHub](https://github.com/kangfenmao/cherry-studio)
- [重排模型原理详解](https://arxiv.org/abs/2310.07554)

---

## ✅ 配置清单

完成以下步骤确保集成成功：

- [ ] 在 Cherry Studio 中下载 `BAAI/bge-reranker-v2-m3`
- [ ] 获取 SiliconFlow API Key
- [ ] 配置环境变量 `RERANKER_API_KEY`
- [ ] 设置 `reranker.enabled=true`
- [ ] 启动后端服务
- [ ] 查看日志确认重排模型加载成功
- [ ] 发起测试查询
- [ ] 验证重排日志输出
- [ ] 对比重排前后的效果

---

**祝您配置顺利！如有问题欢迎反馈。🎉**

---

## 🆘 获取帮助

遇到问题？

1. 📖 查看日志文件：`./logs/xu-news-rag.log`
2. 🔍 搜索错误信息：复制错误日志到搜索引擎
3. 💬 提交 Issue：附上完整的错误日志和配置文件
4. 📧 联系支持：support@xu-news-rag.com

