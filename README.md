# XU-News-AI-RAG

<div align="center">

**个性化新闻智能知识库系统**

*基于RAG技术的本地AI新闻管理平台*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue 3](https://img.shields.io/badge/Vue-3.4.0-4FC08D.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![n8n](https://img.shields.io/badge/n8n-latest-orange.svg)](https://n8n.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

[功能特性](#功能特性) • [快速开始](#快速开始) • [技术架构](#技术架构) • [部署指南](#部署指南) • [使用文档](#使用文档)

</div>

---

## 📖 项目简介

XU-News-AI-RAG 是一个基于AI技术的个性化新闻智能知识库系统，通过自动化新闻采集、本地大模型部署、向量化检索和智能问答等技术，为用户提供一个高效、智能、可定制的新闻知识管理平台。

### 核心价值

- 🤖 **智能检索** - 基于语义理解而非关键词匹配的精准检索
- 🔒 **数据隐私** - 完全本地化部署，数据完全自主可控
- 🚀 **自动采集** - 支持RSS源和网页自动抓取，无需手动收集
- 💡 **AI问答** - 结合RAG技术提供智能问答和内容分析
- 📊 **数据分析** - 提供关键词提取、聚类分析等数据洞察

### 适用场景

- **知识工作者** - 持续跟踪特定领域新闻和信息
- **内容创作者** - 积累素材、寻找创作灵感
- **企业用户** - 行业动态监控和竞品分析
- **个人学习者** - 建立个人知识库、深度学习特定主题

---

## ✨ 功能特性

### 核心功能

| 功能模块 | 功能描述 |
|---------|---------|
| 🔄 **自动化采集** | RSS订阅、网页抓取、定时任务调度、智能去重 |
| 📚 **知识库管理** | 数据CRUD、标签分类、批量导入导出、全文搜索 |
| 🔍 **智能检索** | 语义检索、相似度排序、重排优化、结果高亮 |
| 💬 **RAG问答** | 本地知识库问答、联网扩展查询、引用标注 |
| 📈 **数据分析** | Top10关键词、聚类分析、趋势统计、可视化图表 |
| 👤 **用户系统** | 注册登录、JWT认证、权限管理、会话管理 |
| 🔔 **通知提醒** | 邮件通知、入库提醒、采集失败告警 |
| 📄 **文件处理** | 支持PDF、Word、TXT、Excel等多种格式 |

### AI能力

- **大语言模型**: Ollama + Qwen3:0.6b（中文优化，轻量级）
- **向量化模型**: nomic-embed-text（768维向量）
- **重排模型**: BAAI/bge-reranker-v2-m3（SiliconFlow API）
- **向量检索**: FAISS高性能向量数据库

---

## 🏗️ 技术架构

### 技术栈

```
┌────────────────────────────────────────────────┐
│              前端层 (Vue 3)                     │
│     Element Plus + Vue Router + Pinia          │
└────────────┬───────────────────────────────────┘
             │ REST API
             ↓
┌────────────────────────────────────────────────┐
│           工作流编排层 (n8n)                    │
│   RSS采集 | AI处理 | RAG问答 | 定时任务        │
└────────────┬───────────────────────────────────┘
             │ HTTP/Webhook
             ↓
┌────────────────────────────────────────────────┐
│         应用服务层 (Spring Boot)                │
│  数据管理 | 用户认证 | 向量检索 | API网关       │
└────────────┬───────────────────────────────────┘
             │
       ┌─────┴─────┐
       ↓           ↓
┌──────────┐  ┌──────────┐
│  MySQL   │  │  FAISS   │
│ 关系数据  │  │ 向量数据  │
└──────────┘  └──────────┘
```

### 核心技术

| 层级 | 技术选型 | 版本 |
|-----|---------|------|
| **前端** | Vue 3 + Element Plus | 3.4.0 / 2.5.0 |
| **后端** | Spring Boot + MyBatis Plus | 3.1.5 / 3.5.7 |
| **数据库** | MySQL + FAISS | 8.0 / Latest |
| **工作流** | n8n | Latest |
| **AI引擎** | Ollama + Qwen3 | Latest / 0.6b |
| **向量化** | nomic-embed-text | Latest (768维) |
| **重排模型** | bge-reranker-v2-m3 | BAAI |
| **安全** | Spring Security + JWT | 3.1.5 / 0.12.3 |

---

## 🚀 快速开始

### 系统要求

| 项目 | 最低配置 | 推荐配置 |
|------|---------|---------|
| **CPU** | 4核 | 8核+ |
| **内存** | 8GB | 16GB+ |
| **硬盘** | 50GB SSD | 200GB SSD |
| **GPU** | 无 | NVIDIA GPU（可选加速） |
| **操作系统** | Windows 10/Ubuntu 20.04/macOS 11 | 最新版本 |

### 前置要求

- ✅ Docker 20.10+
- ✅ Docker Compose 2.0+
- ✅ Git
- ✅ Ollama（用于本地AI模型）

### 一键部署

```bash
# 1. 克隆项目
git clone https://github.com/your-username/xu-ai-news-rag.git
cd xu-ai-news-rag

# 2. 配置环境变量（可选）
cp .env.example .env
# 编辑 .env 文件设置密码和密钥

# 3. 安装并启动 Ollama（如果使用本地模型）
# Windows: 下载 https://ollama.com/download
# Linux/macOS:
curl -fsSL https://ollama.com/install.sh | sh
ollama serve

# 4. 拉取AI模型
ollama pull qwen3:0.6b
ollama pull nomic-embed-text

# 5. 启动所有服务
docker-compose up -d

# 6. 查看服务状态
docker-compose ps
```

### 访问系统

启动成功后，可以通过以下地址访问：

| 服务 | 地址 | 说明 |
|-----|------|------|
| 🌐 **前端界面** | http://localhost:5173 | Vue 3 Web应用 |
| 🔌 **后端API** | http://localhost:8080 | Spring Boot REST API |
| ⚙️ **n8n工作流** | http://localhost:5678 | n8n管理界面 |
| 🗄️ **MySQL** | localhost:3306 | 数据库连接 |
| 🤖 **Ollama** | http://localhost:11434 | AI模型服务 |

**默认账号**:
- n8n: `admin` / `admin123`
- MySQL: `xu_news` / `xu_news_pass`

---

## 📦 部署指南

### Docker Compose 部署（推荐）

项目提供完整的 Docker Compose 配置，支持一键启动所有服务。

#### 服务说明

```yaml
services:
  mysql:      # MySQL 8.0 数据库
  n8n:        # n8n 工作流引擎
  backend:    # Spring Boot 后端服务
  frontend:   # Vue 3 前端应用
```

#### 详细步骤

```bash
# 1. 启动所有服务
docker-compose up -d

# 2. 查看日志
docker-compose logs -f backend

# 3. 停止服务
docker-compose down

# 4. 重启服务
docker-compose restart backend

# 5. 清理数据并重启（谨慎使用）
docker-compose down -v
docker-compose up -d
```

### 手动部署

<details>
<summary>点击展开手动部署指南</summary>

#### 1. 部署 MySQL

```bash
# 安装 MySQL 8.0
sudo apt-get install mysql-server

# 创建数据库
mysql -u root -p < database-init.sql
```

#### 2. 部署后端

```bash
cd backend

# 编译项目
mvn clean package -DskipTests

# 运行
java -jar target/news-ai-rag.jar
```

#### 3. 部署前端

```bash
cd frontend

# 安装依赖
npm install

# 开发环境
npm run dev

# 生产环境
npm run build
# 将 dist 目录部署到 Nginx
```

#### 4. 部署 n8n

```bash
# 使用 Docker 运行
docker run -d \
  --name n8n \
  -p 5678:5678 \
  -v n8n_data:/home/node/.n8n \
  n8nio/n8n
```

</details>

---

## 📖 使用文档

### 配置数据源

1. 登录 n8n 管理界面（http://localhost:5678）
2. 导入提供的工作流模板：
   - `n8n-workflows/rss-news-collector-final.json` - RSS采集
   - `n8n-workflows/rag-qa-workflow.json` - RAG问答
   - `n8n-workflows/document-processor.json` - 文档处理
3. 配置RSS源或网页抓取规则
4. 设置定时任务（推荐：每小时一次）

### 知识库管理

#### 添加数据

1. **自动采集**: 通过 n8n 工作流自动抓取
2. **手动上传**: 在知识库页面上传文件（支持PDF、Word、TXT、Excel）
3. **批量导入**: 使用API批量导入JSON/CSV数据

#### 数据检索

```javascript
// 语义检索示例
POST /api/query/search
{
  "query": "人工智能的最新进展",
  "topK": 10,
  "minScore": 0.7
}
```

#### 智能问答

```javascript
// RAG问答示例
POST /api/query/ask
{
  "question": "最近关于GPT-4的新闻有哪些？",
  "useInternet": true
}
```

### API文档

完整的API文档请参考：[API Documentation](docs/API.md)

主要接口：

| 接口 | 方法 | 说明 |
|-----|------|------|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/register` | POST | 用户注册 |
| `/api/knowledge/**` | * | 知识库管理 |
| `/api/query/**` | * | 检索和问答 |
| `/api/statistics/**` | GET | 数据统计 |

---

## 🧪 测试说明

项目包含完整的测试体系，包括单元测试、集成测试和API测试。

### 运行测试

```bash
# 后端测试
cd backend

# 运行所有测试
mvn test

# 使用Docker运行测试
docker-compose -f docker-compose.test.yml run --rm backend-test

# 查看测试覆盖率
mvn test jacoco:report
# 报告位置: target/site/jacoco/index.html

# 前端测试
cd frontend

# 运行单元测试
npm run test

# 运行E2E测试
npm run test:e2e

# 查看测试覆盖率
npm run test:coverage
```

### 测试覆盖

- ✅ **单元测试**: Service、Util、Mapper层
- ✅ **集成测试**: 完整业务流程测试
- ✅ **API测试**: Controller层接口测试
- ✅ **前端测试**: API、Store、组件测试

详细测试文档：[README_TEST.md](README_TEST.md)

---

## 📊 项目结构

```
xu-ai-news-rag/
├── backend/                    # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/xu/news/
│   │   │   │   ├── controller/    # REST API控制器
│   │   │   │   ├── service/       # 业务逻辑层
│   │   │   │   ├── mapper/        # MyBatis数据访问层
│   │   │   │   ├── entity/        # 实体类
│   │   │   │   ├── dto/           # 数据传输对象
│   │   │   │   ├── config/        # 配置类
│   │   │   │   ├── security/      # 安全配置
│   │   │   │   └── util/          # 工具类
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── mapper/        # MyBatis XML映射
│   │   └── test/                  # 测试代码
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── views/             # 页面组件
│   │   ├── layouts/           # 布局组件
│   │   ├── api/               # API封装
│   │   ├── stores/            # Pinia状态管理
│   │   ├── router/            # Vue Router路由
│   │   ├── App.vue
│   │   └── main.js
│   ├── tests/                 # 测试代码
│   ├── package.json
│   ├── vite.config.js
│   └── Dockerfile
│
├── n8n-workflows/             # n8n工作流模板
│   ├── rss-news-collector-final.json
│   ├── rag-qa-workflow.json
│   ├── document-processor.json
│   └── README.md
│
├── database/                  # 数据库脚本
│   ├── init.sql              # 初始化脚本
│   └── *.sql                 # 其他SQL脚本
│
├── docs/                      # 项目文档
│   ├── PRD-XU-News-AI-RAG.md
│   ├── 技术架构文档-XU-News-AI-RAG.md
│   ├── 概要设计文档-XU-News-AI-RAG.md
│   └── 产品原型设计文档-XU-News-AI-RAG.md
│
├── prototype/                 # 原型页面
│   ├── index.html
│   ├── home.html
│   ├── search.html
│   ├── chat.html
│   └── knowledge.html
│
├── docker-compose.yml         # Docker编排配置
├── docker-compose.test.yml    # 测试环境配置
├── docker-test.sh            # 测试脚本
├── README.md                 # 本文件
└── README_TEST.md            # 测试文档
```

---

## ⚙️ 配置说明

### 环境变量

项目支持通过环境变量配置关键参数：

```bash
# 数据库配置
DB_HOST=mysql
DB_PORT=3306
DB_NAME=xu_news_rag
DB_USERNAME=xu_news
DB_PASSWORD=xu_news_pass

# Ollama配置
OLLAMA_URL=http://localhost:11434
OLLAMA_EMBEDDING_MODEL=nomic-embed-text:latest

# JWT配置
JWT_SECRET=your-secret-key

# n8n配置
N8N_URL=http://n8n:5678
N8N_BASIC_AUTH_USER=admin
N8N_BASIC_AUTH_PASSWORD=admin123

# 重排模型配置（可选）
RERANKER_ENABLED=true
RERANKER_URL=https://api.siliconflow.cn
RERANKER_API_KEY=your-api-key
```

### application.yml

后端配置文件位于 `backend/src/main/resources/application.yml`

主要配置项：

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

ollama:
  base-url: ${OLLAMA_URL}
  embedding-model: ${OLLAMA_EMBEDDING_MODEL}

jwt:
  secret: ${JWT_SECRET}
  expiration: 604800  # 7天（秒）
```

---

## 🔧 常见问题

<details>
<summary><b>Q: Ollama连接失败怎么办？</b></summary>

A: 
1. 确保Ollama服务已启动：`ollama serve`
2. 检查端口是否被占用：`lsof -i :11434`
3. 在Docker环境中，使用主机IP而非localhost
4. Windows用户：使用 `http://host.docker.internal:11434`
</details>

<details>
<summary><b>Q: MySQL连接失败？</b></summary>

A:
1. 检查MySQL服务是否启动：`docker-compose ps mysql`
2. 查看MySQL日志：`docker-compose logs mysql`
3. 确认用户名密码正确
4. 检查数据库是否已创建：`docker exec -it xu-news-mysql mysql -u root -p`
</details>

<details>
<summary><b>Q: 前端页面无法访问后端API？</b></summary>

A:
1. 检查后端服务状态：`curl http://localhost:8080/actuator/health`
2. 检查CORS配置
3. 查看浏览器控制台错误信息
4. 确认 `VITE_API_BASE_URL` 环境变量设置正确
</details>

<details>
<summary><b>Q: n8n工作流执行失败？</b></summary>

A:
1. 检查n8n日志：`docker-compose logs n8n`
2. 确认Webhook URL配置正确
3. 测试网络连接
4. 检查RSS源或目标网站是否可访问
</details>

<details>
<summary><b>Q: 向量检索速度慢？</b></summary>

A:
1. 优化FAISS索引类型（使用IVF索引）
2. 减少topK返回结果数量
3. 考虑使用GPU加速
4. 增加服务器内存
</details>

<details>
<summary><b>Q: AI模型推理慢？</b></summary>

A:
1. 已使用轻量级模型（qwen3:0.6b）
2. 优化Prompt，减少输入token
3. 使用GPU加速（如果有NVIDIA显卡）
4. 考虑使用更大模型（如qwen3:2b）以提升准确率
</details>

---

## 📝 开发指南

### 后端开发

```bash
cd backend

# 开发模式运行（热重载）
mvn spring-boot:run

# 打包
mvn clean package

# 代码格式化
mvn spotless:apply
```

### 前端开发

```bash
cd frontend

# 安装依赖
npm install

# 开发模式
npm run dev

# 构建生产版本
npm run build

# 代码检查
npm run lint
```

### 数据库迁移

```bash
# 执行迁移脚本
mysql -u xu_news -p xu_news_rag < database/migration_v1.1.sql
```

---

## 🤝 贡献指南

欢迎贡献代码、报告问题或提出建议！

### 贡献流程

1. Fork本仓库
2. 创建特性分支：`git checkout -b feature/amazing-feature`
3. 提交更改：`git commit -m 'Add amazing feature'`
4. 推送到分支：`git push origin feature/amazing-feature`
5. 提交Pull Request

### 代码规范

- **Java**: 遵循Google Java Style Guide
- **Vue**: 遵循Vue官方风格指南
- **提交信息**: 使用Conventional Commits规范

---

## 📄 文档资源

- 📘 [产品需求文档 (PRD)](PRD-XU-News-AI-RAG.md)
- 🏗️ [技术架构文档](技术架构文档-XU-News-AI-RAG.md)
- 📐 [概要设计文档](概要设计文档-XU-News-AI-RAG.md)
- 🎨 [产品原型设计文档](产品原型设计文档-XU-News-AI-RAG.md)
- 🧪 [测试文档](README_TEST.md)
- 📖 [n8n工作流指南](n8n-workflows/README.md)

---

## 🗓️ 版本历史

- **v1.0.0** (2025-10-15)
  - ✨ 初始版本发布
  - ✅ 核心功能完成
  - ✅ 文档完善
  - ✅ 测试覆盖

---

## 📮 联系方式

- **项目主页**: https://github.com/your-username/xu-ai-news-rag
- **问题反馈**: [GitHub Issues](https://github.com/your-username/xu-ai-news-rag/issues)
- **讨论区**: [GitHub Discussions](https://github.com/your-username/xu-ai-news-rag/discussions)

---

## 📜 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [n8n](https://n8n.io/)
- [Ollama](https://ollama.com/)
- [FAISS](https://github.com/facebookresearch/faiss)
- [MyBatis Plus](https://baomidou.com/)

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给一个Star！⭐**

Made with ❤️ by XU Team

</div>

