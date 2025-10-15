# XU-News-AI-RAG 个性化新闻智能知识库

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-brightgreen.svg)](https://vuejs.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

## 📖 项目简介

XU-News-AI-RAG 是一个基于 **检索增强生成（RAG）** 技术的智能新闻知识库系统，支持自动采集、向量化存储、语义检索和AI问答。

### ✨ 核心特性

- 🤖 **智能问答**：基于 RAG 技术，结合本地 LLM（Qwen）生成准确回答
- 📰 **自动采集**：通过 n8n 工作流自动抓取 RSS、网页新闻
- 🔍 **语义搜索**：使用 FAISS 向量数据库实现高效语义检索
- 📚 **知识管理**：支持文档上传、标签管理、全文搜索
- 🔐 **安全认证**：JWT + Spring Security 保障系统安全
- 🐳 **容器化部署**：Docker Compose 一键部署全栈服务

## 🏗️ 技术架构

### 后端技术栈

| 组件 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 开发语言 | Java | 17+ | 企业级开发语言 |
| Web框架 | Spring Boot | 3.2+ | 主流Java Web框架 |
| 安全框架 | Spring Security + JWT | - | 认证授权 |
| ORM框架 | MyBatis Plus | 3.5+ | 数据持久化 |
| 关系数据库 | MySQL | 8.0+ | 数据存储 |
| 向量数据库 | FAISS | - | 向量检索 |
| AI大模型 | Ollama (Qwen) | - | 本地LLM服务 |
| 工作流引擎 | n8n | Latest | 低代码自动化 |

### 前端技术栈

| 组件 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 框架 | Vue 3 | 3.4+ | 渐进式框架 |
| 构建工具 | Vite | 5.0+ | 极速构建 |
| UI组件库 | Element Plus | 2.5+ | 企业级组件库 |
| 状态管理 | Pinia | 2.1+ | 新一代状态管理 |
| 路由 | Vue Router | 4.2+ | 官方路由 |
| HTTP客户端 | Axios | 1.6+ | Promise API |

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                         用户层                               │
│                    (Vue 3 + Element Plus)                   │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP/HTTPS
┌────────────────────────┴────────────────────────────────────┐
│                      应用层 (Spring Boot)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ 认证控制器    │  │ 问答控制器    │  │ 知识库控制器  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ 用户服务      │  │ 查询服务(RAG) │  │ 知识库服务    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└────────────────────────┬────────────────────────────────────┘
                         │
           ┌─────────────┼─────────────┐
           │             │             │
┌──────────▼────┐ ┌─────▼──────┐ ┌───▼────────────┐
│   MySQL        │ │   FAISS    │ │   Ollama       │
│ (元数据存储)    │ │ (向量检索)  │ │ (LLM + Embed)  │
└───────────────┘ └────────────┘ └────────────────┘
           │
┌──────────▼────────────────────────────────────────┐
│              n8n 工作流引擎                         │
│  ┌─────────┐  ┌─────────┐  ┌──────────┐          │
│  │RSS采集   │  │网页爬取  │  │AI处理    │          │
│  └─────────┘  └─────────┘  └──────────┘          │
└───────────────────────────────────────────────────┘
```

## 🚀 快速开始

### 前置要求

- Docker 20.10+
- Docker Compose 2.0+
- (可选) Java 17+, Node.js 18+, Maven 3.9+

### 一键部署（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/yourusername/xu-ai-news-rag.git
cd xu-ai-news-rag

# 2. 启动所有服务
docker-compose up -d

# 3. 初始化Ollama模型
docker exec -it xu-news-ollama ollama pull qwen2.5:3b
docker exec -it xu-news-ollama ollama pull all-minilm-l6-v2

# 4. 访问服务
# 前端：http://localhost:5173
# 后端API：http://localhost:8080/api
# n8n：http://localhost:5678 (admin/admin123)
```

### 本地开发

#### 后端开发

```bash
cd backend

# 安装依赖
mvn clean install

# 启动MySQL（需要先启动Docker）
docker-compose up -d mysql

# 启动应用
mvn spring-boot:run
```

#### 前端开发

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问 http://localhost:5173
```

## 📁 项目结构

```
xu-ai-news-rag/
├── backend/                    # Spring Boot后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/xu/news/
│   │   │   │   ├── controller/    # 控制器层
│   │   │   │   ├── service/       # 服务层
│   │   │   │   ├── mapper/        # 数据访问层
│   │   │   │   ├── entity/        # 实体类
│   │   │   │   ├── dto/           # 数据传输对象
│   │   │   │   ├── config/        # 配置类
│   │   │   │   ├── security/      # 安全配置
│   │   │   │   ├── util/          # 工具类
│   │   │   │   └── XuNewsApplication.java
│   │   │   └── resources/
│   │   │       ├── mapper/        # MyBatis XML
│   │   │       ├── application.yml
│   │   │       └── application-prod.yml
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                   # Vue 3前端
│   ├── src/
│   │   ├── api/               # API接口
│   │   ├── assets/            # 静态资源
│   │   ├── components/        # 组件
│   │   ├── layouts/           # 布局
│   │   ├── router/            # 路由
│   │   ├── stores/            # Pinia状态
│   │   ├── views/             # 页面
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   ├── vite.config.js
│   └── index.html
├── database/                   # 数据库脚本
│   └── init.sql               # 初始化SQL
├── n8n-workflows/             # n8n工作流配置
│   └── rss-news-collector.json
├── prototype/                  # 原型文件
│   ├── index.html             # 登录页
│   ├── home.html              # 首页
│   ├── chat.html              # 问答页
│   ├── search.html            # 搜索页
│   ├── knowledge.html         # 知识库页
│   └── README.md
├── docs/                       # 文档
│   ├── PRD-XU-News-AI-RAG.md
│   ├── 概要设计文档-XU-News-AI-RAG.md
│   ├── 技术架构文档-XU-News-AI-RAG.md
│   └── 产品原型设计文档-XU-News-AI-RAG.md
├── docker-compose.yml          # Docker编排
├── .gitignore
└── README.md
```

## 🔧 配置说明

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_HOST` | MySQL主机 | mysql |
| `DB_PORT` | MySQL端口 | 3306 |
| `DB_NAME` | 数据库名 | xu_news_rag |
| `DB_USERNAME` | 数据库用户名 | xu_news |
| `DB_PASSWORD` | 数据库密码 | xu_news_pass |
| `OLLAMA_URL` | Ollama服务地址 | http://ollama:11434 |
| `N8N_URL` | n8n服务地址 | http://n8n:5678 |
| `JWT_SECRET` | JWT密钥 | (自定义) |

### 修改配置

编辑 `backend/src/main/resources/application-prod.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:mysql}:3306/xu_news_rag
    username: ${DB_USERNAME:xu_news}
    password: ${DB_PASSWORD}

ollama:
  base-url: ${OLLAMA_URL:http://ollama:11434}
  model:
    llm: qwen2.5:3b
    embedding: all-minilm-l6-v2
```

## 📱 功能截图

### 登录页面
![登录页面](docs/screenshots/login.png)

### 首页
![首页](docs/screenshots/home.png)

### 智能问答
![智能问答](docs/screenshots/chat.png)

### 知识库管理
![知识库管理](docs/screenshots/knowledge.png)

## 🧪 测试

### 后端测试

```bash
cd backend
mvn test
```

### 前端测试

```bash
cd frontend
npm run test
```

### API测试

使用Postman或curl测试API：

```bash
# 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 问答
curl -X POST http://localhost:8080/api/query/ask \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"query":"最近有哪些AI新闻？","topK":5}'
```

## 📚 API文档

启动后端后访问：
- Swagger UI: `http://localhost:8080/api/doc.html`
- OpenAPI JSON: `http://localhost:8080/api/v3/api-docs`

## 🤝 贡献指南

欢迎贡献！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 👥 作者

- **XU** - *Initial work* - [GitHub](https://github.com/yourusername)

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Ollama](https://ollama.ai/)
- [n8n](https://n8n.io/)
- [FAISS](https://github.com/facebookresearch/faiss)

## 📧 联系方式

如有问题，请联系：
- Email: your.email@example.com
- GitHub Issues: https://github.com/yourusername/xu-ai-news-rag/issues

---

**© 2025 XU-News-AI-RAG Project. All Rights Reserved.**

