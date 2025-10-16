# 本地重排服务部署指南

## 🚀 快速开始（免费方案）

如果您不想使用 SiliconFlow API，可以本地运行重排服务，完全免费！

---

## 📦 安装步骤

### 1. 安装 Python 依赖

```bash
# 创建虚拟环境（推荐）
python -m venv venv

# 激活虚拟环境
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# 安装依赖
pip install -r local_reranker_requirements.txt
```

### 2. 启动重排服务

```bash
python local_reranker_service.py
```

**首次启动会自动下载模型（约 550MB），请耐心等待。**

### 3. 验证服务

```bash
# 测试健康检查
curl http://localhost:8000/health

# 测试重排功能
curl -X POST http://localhost:8000/v1/rerank \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Python如何读取文件",
    "documents": ["Python文件操作", "Java IO流", "文件系统基础"],
    "top_n": 2
  }'
```

---

## ⚙️ 配置 Java 后端

修改 `application.yml`:

```yaml
reranker:
  enabled: true
  base-url: http://localhost:8000  # 改为本地地址
  api-key: ""  # 本地服务不需要 API Key
  model: BAAI/bge-reranker-v2-m3
```

---

## 🔄 完整启动流程

### Terminal 1: 启动本地重排服务
```bash
cd backend
python local_reranker_service.py
```

### Terminal 2: 启动 Java 后端
```bash
cd backend
mvn spring-boot:run
```

---

## 📊 方案对比

| 特性 | SiliconFlow API | 本地部署 |
|-----|----------------|---------|
| **成本** | 💰 需付费（有免费额度） | ✅ 完全免费 |
| **部署** | ✅ 无需部署 | 📝 需要安装 Python |
| **性能** | ⚡ 云端高性能 | 💻 取决于本地配置 |
| **网络** | 📡 需要网络 | ✅ 完全离线 |
| **维护** | ✅ 无需维护 | 📝 需要自己维护 |

---

## 🐛 常见问题

### Q: 模型下载太慢

**A:** 使用镜像站点
```bash
# 设置 Hugging Face 镜像
set HF_ENDPOINT=https://hf-mirror.com  # Windows
export HF_ENDPOINT=https://hf-mirror.com  # Linux/Mac
```

### Q: 显存不足

**A:** 该模型需要约 2GB 显存/内存，如果不够可以使用更小的模型：
```python
# 修改 local_reranker_service.py
reranker = CrossEncoder('cross-encoder/ms-marco-MiniLM-L-6-v2')  # 更小的模型
```

### Q: 启动报错

**A:** 确保 Python 版本 >= 3.8，并检查依赖是否正确安装：
```bash
python --version
pip list
```

---

## 📈 性能优化

### 1. 批量处理优化

模型已支持批量处理，无需额外配置。

### 2. GPU 加速

如果有 NVIDIA GPU：
```bash
pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu118
```

### 3. 多进程部署

```bash
uvicorn local_reranker_service:app --host 0.0.0.0 --port 8000 --workers 4
```

---

## ✅ 推荐方案

### 开发/测试环境
→ **本地部署**（免费，离线）

### 生产环境
→ **SiliconFlow API**（稳定，高性能）

---

**选择适合您的方案，两种方式都已经配置好了！**

