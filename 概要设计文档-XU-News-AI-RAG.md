# XU-News-AI-RAG 个性化新闻智能知识库概要设计文档

**版本：** 1.0  
**日期：** 2025年10月15日  
**项目代号：** xu-ai-news-rag  

---

## 1. 引言

### 1.1 编写目的

本文档是XU-News-AI-RAG个性化新闻智能知识库系统的概要设计文档，目的是从技术架构层面描述系统的整体设计方案，为详细设计和开发实现提供指导。

### 1.2 背景

基于《XU-News-AI-RAG产品需求文档》中定义的功能需求和非功能需求，本文档设计了一个基于本地大模型、向量检索和RAG技术的智能新闻知识库系统。系统采用前后端分离架构，后端采用Java Spring Boot框架，支持本地化部署。

### 1.3 定义与缩略语

| 术语 | 全称 | 说明 |
|-----|------|------|
| RAG | Retrieval-Augmented Generation | 检索增强生成 |
| LLM | Large Language Model | 大语言模型 |
| FAISS | Facebook AI Similarity Search | 向量相似度搜索库 |
| JWT | JSON Web Token | 令牌认证 |
| ORM | Object-Relational Mapping | 对象关系映射 |
| API | Application Programming Interface | 应用程序接口 |

### 1.4 参考资料

- 《XU-News-AI-RAG产品需求文档 v1.0》
- LangChain4j官方文档
- Spring Boot官方文档
- Spring Security官方文档
- Vue 3官方文档

---

## 2. 系统总体架构设计

### 2.1 系统架构概述

系统采用**前后端分离架构**，基于**微服务思想**进行模块划分，采用**本地化部署**方式。整体架构分为四层：

```
┌─────────────────────────────────────────────────────────────┐
│                        展示层 (Presentation Layer)            │
│                     Vue 3 + Element Plus                      │
└─────────────────────────────────────────────────────────────┘
                              ↓ HTTP/HTTPS
┌─────────────────────────────────────────────────────────────┐
│                       应用层 (Application Layer)              │
│                   Spring Boot RESTful API                     │
│  ┌──────────┬──────────┬──────────┬──────────┬─────────┐   │
│  │采集模块  │知识库模块│检索模块  │分析模块  │认证模块 │   │
│  └──────────┴──────────┴──────────┴──────────┴─────────┘   │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                     服务层 (Service Layer)                    │
│  ┌──────────────┬───────────────┬─────────────────────┐    │
│  │ LLM服务      │ 嵌入模型服务   │ 重排模型服务        │    │
│  │ (Ollama)     │ (Sentence-T)  │ (Cross-Encoder)     │    │
│  └──────────────┴───────────────┴─────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                     数据层 (Data Layer)                       │
│  ┌──────────────┬───────────────┬─────────────────────┐    │
│  │ 关系型数据库  │ 向量数据库     │ 文件存储            │    │
│  │ (MySQL)      │ (FAISS)       │ (Local FS)          │    │
│  └──────────────┴───────────────┴─────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 技术选型

#### 2.2.1 前端技术栈

| 技术组件 | 选型 | 版本 | 说明 |
|---------|------|------|------|
| 前端框架 | Vue 3 | 3.3+ | 响应式框架，组合式API |
| UI组件库 | Element Plus | 2.4+ | 基于Vue 3的组件库 |
| 状态管理 | Pinia | 2.1+ | Vue官方推荐状态管理 |
| 路由管理 | Vue Router | 4.2+ | 官方路由解决方案 |
| HTTP客户端 | Axios | 1.6+ | Promise风格HTTP库 |
| 图表可视化 | ECharts | 5.4+ | 数据可视化图表库 |
| 词云组件 | echarts-wordcloud | 2.1+ | 词云插件 |
| Markdown渲染 | markdown-it | 13.0+ | Markdown解析器 |
| 构建工具 | Vite | 5.0+ | 新一代前端构建工具 |

#### 2.2.2 后端技术栈

| 技术组件 | 选型 | 版本 | 说明 |
|---------|------|------|------|
| 开发语言 | Java | 11+ | 企业级开发语言 |
| Web框架 | Spring Boot | 2.7+ / 3.0+ | 主流Java Web框架 |
| 依赖注入 | Spring Core | 5.3+ / 6.0+ | IoC容器 |
| 身份认证 | Spring Security + JWT | 5.7+ / 6.0+ | 安全框架和令牌管理 |
| ORM框架 | MyBatis Plus / JPA | 3.5+ / 2.7+ | Java持久层框架 |
| 数据库 | MySQL | 8.0+ | 企业级关系数据库 |
| 数据库驱动 | mysql-connector-java | 8.0+ | MySQL Java驱动 |
| 数据库连接池 | HikariCP | 5.0+ | 高性能连接池（Spring Boot默认） |
| 向量数据库 | FAISS (JNI) | 1.7+ | 向量相似度搜索 |
| 文档解析 | Apache POI, PDFBox | 5.2+ / 2.0+ | 用户上传文档处理 |
| HTTP客户端 | RestTemplate / OkHttp | - / 4.12+ | 调用外部API（n8n、Ollama） |
| JSON处理 | Jackson / Fastjson2 | 2.15+ / 2.0+ | JSON序列化/反序列化 |
| 参数校验 | Hibernate Validator | 6.2+ | JSR-303参数校验 |
| 日志框架 | Logback / SLF4J | 1.4+ / 2.0+ | 日志管理 |

**说明：** 爬虫和RSS采集功能完全由n8n负责，Spring Boot不需要爬虫相关依赖。

#### 2.2.3 n8n工作流技术栈（课程核心）

| 技术组件 | 选型 | 版本 | 说明 |
|---------|------|------|------|
| **工作流引擎** | **n8n** | **Latest** | **低代码工作流平台（课程要求）** |
| 部署方式 | Docker | - | 容器化部署 |
| 爬虫节点 | HTTP Request | - | 网页内容抓取 |
| RSS节点 | RSS Feed Reader | - | RSS/Atom订阅解析 |
| HTML解析节点 | HTML Extract | - | 提取网页结构化数据 |
| AI节点 | LangChain Nodes | - | 集成Ollama、OpenAI等 |
| 定时触发 | Cron / Schedule | - | 定时执行工作流 |
| Webhook节点 | Webhook | - | 接收外部HTTP请求 |
| 数据库节点 | MySQL | - | 直接操作数据库（可选） |
| 函数节点 | Code (JavaScript) | - | 自定义业务逻辑 |

**n8n的职责：**
- ✅ RSS新闻自动采集
- ✅ 网页全文抓取和解析
- ✅ 调用Qwen生成摘要
- ✅ 调用Ollama生成向量
- ✅ 实现RAG问答流程
- ✅ 定时任务调度
- ✅ 数据预处理和清洗

#### 2.2.4 AI技术栈

| 技术组件 | 选型 | 版本 | 说明 |
|---------|------|------|------|
| LLM平台 | Ollama | 0.1+ | 本地大模型部署 |
| 大语言模型 | qwen2.5:3b | - | 通义千问3B |
| AI框架 | LangChain4j | 0.28+ | Java版LLM应用开发框架 |
| Ollama客户端 | langchain4j-ollama | 0.28+ | Ollama Java客户端 |
| 嵌入模型 | all-MiniLM-L6-v2 | - | Sentence-Transformers |
| 重排模型 | ms-marco-MiniLM-L-6-v2 | - | Cross-Encoder |
| 向量库 | DJL / ONNX Runtime | 0.23+ / 1.15+ | Java深度学习库 |
| FAISS Java绑定 | faiss-java | 1.7+ | FAISS的JNI封装 |

#### 2.2.5 部署与运维

| 技术组件 | 选型 | 说明 |
|---------|------|------|
| 容器化 | Docker + Docker Compose | n8n、MySQL、Ollama统一容器化 |
| 构建工具 | Maven / Gradle | 3.8+ / 7.6+ |
| 应用服务器 | Tomcat（内嵌） | Spring Boot内嵌Tomcat |
| Web服务器 | Nginx | 反向代理和静态资源 |
| 进程管理 | systemd / supervisor | 后台服务守护 |
| n8n部署 | Docker | 推荐使用官方Docker镜像 |

### 2.3 系统部署架构

#### 2.3.1 单机部署架构（推荐）

```
┌───────────────────────────────────────────────────────┐
│                    服务器主机                          │
│                                                        │
│  ┌──────────────────────────────────────────────┐   │
│  │           Nginx (80/443)                     │   │
│  │  - 静态资源服务                               │   │
│  │  - API反向代理                                │   │
│  └──────────────────────────────────────────────┘   │
│                      ↓                                │
│  ┌──────────────────────────────────────────────┐   │
│  │   Spring Boot Backend (8080)                 │   │
│  │  - RESTful API                               │   │
│  │  - 业务逻辑处理                               │   │
│  └──────────────────────────────────────────────┘   │
│         ↓              ↓              ↓               │
│  ┌──────────┐  ┌──────────┐  ┌──────────────┐      │
│  │  MySQL   │  │  FAISS   │  │   Ollama     │      │
│  │  (3306)  │  │  向量库  │  │   (11434)    │      │
│  └──────────┘  └──────────┘  └──────────────┘      │
│                                                        │
│  ┌──────────────────────────────────────────────┐   │
│  │   后台任务 (APScheduler)                      │   │
│  │  - RSS采集                                   │   │
│  │  - 网页抓取                                  │   │
│  └──────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────┘
```

#### 2.3.2 Docker容器化部署架构（可选）

```
┌───────────────────────────────────────────────────────┐
│                  Docker Host                          │
│                                                        │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐  │
│  │   nginx      │  │   backend    │  │  ollama   │  │
│  │  Container   │  │  Container   │  │ Container │  │
│  └──────────────┘  └──────────────┘  └───────────┘  │
│                                                        │
│  ┌────────────────────────────────────────────────┐  │
│  │          Docker Volumes (持久化存储)            │  │
│  │  - sqlite-data                                 │  │
│  │  - faiss-index                                 │  │
│  │  - uploads                                     │  │
│  └────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────┘
```

---

## 3. 系统模块设计

### 3.1 模块划分

系统按照功能职责划分为以下核心模块：

```
xu-ai-news-rag/
├── frontend/                    # 前端项目
│   ├── src/
│   │   ├── views/              # 页面视图
│   │   ├── components/         # 公共组件
│   │   ├── api/                # API接口
│   │   ├── store/              # 状态管理
│   │   ├── router/             # 路由配置
│   │   └── utils/              # 工具函数
│   └── package.json
│
├── backend/                     # 后端项目(Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── xu/
│   │   │   │           └── newsrag/
│   │   │   │               ├── XuNewsRagApplication.java    # 启动类
│   │   │   │               ├── config/                      # 配置类
│   │   │   │               │   ├── SecurityConfig.java      # 安全配置
│   │   │   │               │   ├── FaissConfig.java         # FAISS配置
│   │   │   │               │   └── SchedulerConfig.java     # 调度器配置
│   │   │   │               ├── entity/                      # 实体类
│   │   │   │               │   ├── User.java
│   │   │   │               │   ├── Knowledge.java
│   │   │   │               │   ├── Tag.java
│   │   │   │               │   └── Source.java
│   │   │   │               ├── dto/                         # 数据传输对象
│   │   │   │               ├── controller/                  # 控制器
│   │   │   │               │   ├── AuthController.java      # 认证接口
│   │   │   │               │   ├── CrawlerController.java   # 采集管理
│   │   │   │               │   ├── KnowledgeController.java # 知识库接口
│   │   │   │               │   ├── SearchController.java    # 检索接口
│   │   │   │               │   └── AnalyticsController.java # 分析接口
│   │   │   │               ├── service/                     # 业务逻辑
│   │   │   │               │   ├── CrawlerService.java
│   │   │   │               │   ├── KnowledgeService.java
│   │   │   │               │   ├── LlmService.java
│   │   │   │               │   ├── EmbeddingService.java
│   │   │   │               │   └── SearchService.java
│   │   │   │               ├── mapper/                      # MyBatis Mapper
│   │   │   │               ├── task/                        # 定时任务
│   │   │   │               │   └── CrawlerScheduler.java
│   │   │   │               └── util/                        # 工具类
│   │   │   └── resources/
│   │   │       ├── application.yml                          # 应用配置
│   │   │       ├── application-dev.yml                      # 开发环境配置
│   │   │       ├── application-prod.yml                     # 生产环境配置
│   │   │       └── mapper/                                  # MyBatis XML
│   │   └── test/                                            # 测试代码
│   ├── pom.xml                 # Maven配置(或build.gradle)
│   └── README.md
│
├── data/                        # 数据目录
│   ├── faiss/                  # FAISS索引
│   └── uploads/                # 上传文件
│
├── logs/                        # 日志目录
└── docker-compose.yml          # Docker编排文件
```

### 3.2 前端模块设计

#### 3.2.1 页面结构

| 页面路径 | 页面名称 | 功能描述 |
|---------|---------|---------|
| `/login` | 登录页 | 用户登录、注册 |
| `/dashboard` | 仪表盘 | 数据概览、统计信息 |
| `/knowledge/list` | 知识库列表 | 查看、筛选、管理知识库条目 |
| `/knowledge/detail/:id` | 知识详情 | 查看详细内容、编辑元数据 |
| `/knowledge/upload` | 文件上传 | 批量上传文档 |
| `/search` | 智能检索 | 语义检索、RAG问答 |
| `/analytics` | 数据分析 | 关键词分析、聚类可视化 |
| `/crawler/sources` | 数据源管理 | RSS源、网页配置 |
| `/crawler/logs` | 采集日志 | 查看采集历史和状态 |
| `/settings` | 系统设置 | 邮件配置、任务调度 |

#### 3.2.2 核心组件

| 组件名称 | 组件职责 |
|---------|---------|
| `KnowledgeCard` | 知识条目卡片展示 |
| `SearchBar` | 智能检索输入框 |
| `AnswerDisplay` | RAG答案展示组件 |
| `KeywordCloud` | 关键词云图 |
| `ClusterChart` | 聚类可视化图表 |
| `FileUploader` | 文件上传组件 |
| `FilterPanel` | 多维度筛选面板 |
| `MarkdownViewer` | Markdown内容渲染 |

### 3.3 后端模块设计

#### 3.3.1 数据采集模块 (Crawler Module)

**职责：** 负责从外部数据源自动采集新闻内容

**核心类设计：**

```java
@Service
public class CrawlerManager {
    /**采集管理器*/
    public void addRssSource(String sourceUrl, String category);
    public void addWebSource(String url, SelectorConfig config);
    public void removeSource(Long sourceId);
    public void startScheduledTask(String cronExpression);
    public void stopScheduledTask();
}

@Component
public class RssCrawler {
    /**RSS采集器*/
    public List<RssItem> fetchFeed(String feedUrl);
    public Knowledge parseEntry(RssItem entry);
    public Map<String, Object> extractMetadata(RssItem entry);
}

@Component
public class WebCrawler {
    /**网页采集器*/
    public String fetchPage(String url, boolean useJsRender);
    public String extractContent(String html, String selector);
    public String cleanText(String rawText);
}

@Component
public class ContentProcessor {
    /**内容处理器*/
    public boolean deduplicate(String content, Set<String> existingHashes);
    public boolean validateQuality(String content);
    public List<String> extractKeywords(String text);
}
```

**工作流程：**
1. 定时任务触发采集
2. 根据数据源类型调用对应爬虫
3. 解析和清洗内容
4. 去重检查
5. 调用知识库服务入库
6. 发送邮件通知

#### 3.3.2 知识库管理模块 (Knowledge Module)

**职责：** 管理知识库的CRUD操作和文件上传

**核心类设计：**

```java
@Service
public class KnowledgeService {
    /**知识库服务*/
    public Knowledge createKnowledge(String title, String content, Map<String, Object> metadata);
    public PageResult<Knowledge> getKnowledgeList(QueryFilter filters, Pagination pagination);
    public Knowledge getKnowledgeDetail(Long knowledgeId);
    public Knowledge updateKnowledge(Long knowledgeId, UpdateDTO updates);
    public void deleteKnowledge(Long knowledgeId);
    public void batchDelete(List<Long> knowledgeIds);
}

@Component
public class FileParser {
    /**文件解析器*/
    public String parsePdf(String filePath) throws IOException;
    public String parseDocx(String filePath) throws IOException;
    public String parseExcel(String filePath) throws IOException;
    public String parseHtml(String filePath) throws IOException;
}

@Component
public class MetadataManager {
    /**元数据管理器*/
    public void addTags(Long knowledgeId, List<String> tags);
    public void updateSource(Long knowledgeId, String source);
    public List<String> autoGenerateTags(String content);
}
```

#### 3.3.3 向量检索模块 (Search Module)

**职责：** 提供语义检索和RAG问答能力

**核心类设计：**

```java
@Service
public class SearchService {
    /**检索服务*/
    public List<SearchResult> semanticSearch(String query, int topK);
    public List<SearchResult> hybridSearch(String query, QueryFilter filters, int topK);
    public RagResponse ragAnswer(String question, int contextSize);
    public WebSearchResult webSearchFallback(String query);
}

@Service
public class EmbeddingService {
    /**嵌入服务*/
    private String modelName = "all-MiniLM-L6-v2";
    public float[] encodeText(String text);
    public List<float[]> encodeBatch(List<String> texts, int batchSize);
}

@Component
public class VectorStore {
    /**向量存储*/
    public void addVectors(List<Long> ids, List<float[]> vectors, Map<Long, Object> metadata);
    public List<SearchResult> searchSimilar(float[] queryVector, int topK, Predicate<Long> filterFn);
    public void deleteVectors(List<Long> ids);
    public void rebuildIndex();
}

@Service
public class RerankerService {
    /**重排服务*/
    private String modelName = "ms-marco-MiniLM-L-6-v2";
    public List<Document> rerank(String query, List<Document> documents, int topK);
    public double calculateRelevanceScore(String query, Document document);
}

@Service
public class LlmService {
    /**大模型服务*/
    private String modelName = "qwen2.5:3b";
    public String generateAnswer(String prompt, String context, int maxTokens);
    public String summarize(String text, int maxLength);
    public List<String> extractKeywords(String text, int numKeywords);
}
```

**RAG工作流程：**
1. 接收用户问题
2. 问题向量化（Embedding）
3. FAISS向量检索Top-K
4. 重排模型精排（可选）
5. 构建Prompt（问题+检索内容）
6. LLM生成答案
7. 如果置信度低，触发联网搜索
8. 返回答案及引用来源

#### 3.3.4 数据分析模块 (Analytics Module)

**职责：** 提供关键词分析和数据聚类

**核心类设计：**

```java
@Service
public class AnalyticsService {
    /**分析服务*/
    public List<KeywordStat> getTopKeywords(TimeRange timeRange, int topN);
    public List<TrendPoint> getKeywordTrends(String keyword, TimeRange timeRange);
    public ClusterResult clusterAnalysis(int numClusters);
    public Statistics getStatistics(String dimension);
}

@Component
public class KeywordExtractor {
    /**关键词提取器*/
    public List<String> extractByTfidf(List<String> documents, int topN);
    public List<String> extractByTextrank(String text, int topN);
    public List<String> extractByLlm(String text, int numKeywords);
}

@Component
public class ClusterAnalyzer {
    /**聚类分析器*/
    public ClusterResult kmeansClustering(List<float[]> vectors, int numClusters);
    public VisualizationData visualizeClusters(List<float[]> vectors, int[] labels);
    public List<Knowledge> getClusterRepresentatives(int clusterId);
}
```

#### 3.3.5 用户认证模块 (Auth Module)

**职责：** 用户身份认证和权限管理

**核心类设计：**

```java
@Service
public class AuthService {
    /**认证服务*/
    public User register(String email, String password);
    public LoginResponse login(String email, String password);
    public TokenResponse refreshToken(String refreshToken);
    public void resetPassword(String email, String newPassword);
    public boolean verifyToken(String accessToken);
}

@Service
public class UserManager {
    /**用户管理*/
    public User createUser(String email, String hashedPassword);
    public User getUserByEmail(String email);
    public User updateUserProfile(Long userId, Map<String, Object> updates);
    public void changePassword(Long userId, String oldPassword, String newPassword);
}
```

#### 3.3.6 任务调度模块 (Task Module)

**职责：** 管理定时任务和后台作业

**核心类设计：**

```java
@Component
@EnableScheduling
public class TaskScheduler {
    /**任务调度器*/
    public void addCronJob(Runnable task, String cronExpression, String jobId);
    public void addIntervalJob(Runnable task, long intervalSeconds, String jobId);
    public void removeJob(String jobId);
    public void pauseJob(String jobId);
    public void resumeJob(String jobId);
    public List<JobInfo> getAllJobs();
}

@Service
public class EmailNotifier {
    /**邮件通知器*/
    public void sendNotification(String subject, String body, List<String> recipients);
    public void sendBatchSummary(DailyStats stats);
    public void sendErrorAlert(ErrorDetails errorDetails);
}
```

---

## 4. 数据库设计

### 4.1 关系型数据库设计（MySQL）

#### 4.1.1 E-R图

```
┌─────────────┐        ┌──────────────────┐        ┌─────────────┐
│    User     │        │    Knowledge     │        │     Tag     │
├─────────────┤        ├──────────────────┤        ├─────────────┤
│ id (PK)     │        │ id (PK)          │        │ id (PK)     │
│ email       │        │ title            │        │ name        │
│ password    │        │ content          │        │ category    │
│ created_at  │        │ source_url       │────────│ count       │
└─────────────┘        │ source_type      │    M:N │ created_at  │
                       │ author           │────────└─────────────┘
┌─────────────┐        │ publish_time     │
│   Source    │        │ content_hash     │
├─────────────┤        │ vector_id        │
│ id (PK)     │        │ quality_score    │
│ name        │────┐   │ user_id (FK)     │
│ type        │    │   │ source_id (FK)   │
│ url         │    │   │ created_at       │
│ config      │    └───│ updated_at       │
│ is_active   │        └──────────────────┘
└─────────────┘                 │
                                │
                         ┌──────┴──────┐
                         │             │
                  ┌──────────────┐  ┌──────────────┐
                  │SearchHistory │  │CrawlerLog    │
                  ├──────────────┤  ├──────────────┤
                  │id (PK)       │  │id (PK)       │
                  │user_id (FK)  │  │source_id(FK) │
                  │query         │  │status        │
                  │results       │  │count         │
                  │created_at    │  │error_msg     │
                  └──────────────┘  │created_at    │
                                    └──────────────┘
```

#### 4.1.2 表结构设计

**用户表 (users)**

| 字段名 | 类型 | 长度 | 约束 | 说明 |
|-------|------|------|------|------|
| id | BIGINT | - | PRIMARY KEY AUTO_INCREMENT | 用户ID |
| email | VARCHAR | 100 | UNIQUE, NOT NULL | 邮箱（登录名） |
| password_hash | VARCHAR | 255 | NOT NULL | 密码哈希 |
| username | VARCHAR | 50 | NULL | 用户昵称 |
| is_active | TINYINT | 1 | DEFAULT 1 | 是否激活（1=是，0=否） |
| created_at | DATETIME | - | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | - | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_email` (`email`)

**知识条目表 (knowledge)**

| 字段名 | 类型 | 长度 | 约束 | 说明 |
|-------|------|------|------|------|
| id | BIGINT | - | PRIMARY KEY AUTO_INCREMENT | 条目ID |
| title | VARCHAR | 255 | NOT NULL | 标题 |
| content | LONGTEXT | - | NOT NULL | 正文内容 |
| summary | TEXT | - | NULL | 摘要 |
| source_url | VARCHAR | 500 | NULL | 来源URL |
| source_type | VARCHAR | 20 | NOT NULL | 来源类型（rss/web/upload） |
| source_name | VARCHAR | 100 | NULL | 来源名称 |
| author | VARCHAR | 100 | NULL | 作者 |
| publish_time | DATETIME | - | NULL | 发布时间 |
| content_hash | VARCHAR | 64 | NOT NULL | 内容哈希（去重） |
| vector_id | BIGINT | - | NULL | 对应向量ID |
| quality_score | DECIMAL | 3,2 | NULL | 质量评分 |
| view_count | INT | - | DEFAULT 0 | 查看次数 |
| user_id | BIGINT | - | NULL | 所属用户 |
| source_id | BIGINT | - | NULL | 数据源ID |
| created_at | DATETIME | - | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | - | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_content_hash` (`content_hash`)
- UNIQUE KEY `uk_vector_id` (`vector_id`)
- KEY `idx_user_id` (`user_id`)
- KEY `idx_source_type` (`source_type`)
- KEY `idx_publish_time` (`publish_time`)
- KEY `idx_created_at` (`created_at`)
- FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL
- FOREIGN KEY (`source_id`) REFERENCES `sources`(`id`) ON DELETE SET NULL

**标签表 (tags)**

| 字段名 | 类型 | 长度 | 约束 | 说明 |
|-------|------|------|------|------|
| id | BIGINT | - | PRIMARY KEY AUTO_INCREMENT | 标签ID |
| name | VARCHAR | 50 | NOT NULL | 标签名称 |
| category | VARCHAR | 50 | NULL | 标签分类 |
| count | INT | - | DEFAULT 0 | 使用次数 |
| created_at | DATETIME | - | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：**
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_name` (`name`)

**知识标签关联表 (knowledge_tags)**

| 字段名 | 类型 | 长度 | 约束 | 说明 |
|-------|------|------|------|------|
| id | BIGINT | - | PRIMARY KEY AUTO_INCREMENT | 关联ID |
| knowledge_id | BIGINT | - | NOT NULL | 知识ID |
| tag_id | BIGINT | - | NOT NULL | 标签ID |
| created_at | DATETIME | - | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：**
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_knowledge_tag` (`knowledge_id`, `tag_id`)
- KEY `idx_knowledge_id` (`knowledge_id`)
- KEY `idx_tag_id` (`tag_id`)
- FOREIGN KEY (`knowledge_id`) REFERENCES `knowledge`(`id`) ON DELETE CASCADE
- FOREIGN KEY (`tag_id`) REFERENCES `tags`(`id`) ON DELETE CASCADE

**数据源表 (sources)**

| 字段名 | 类型 | 长度 | 约束 | 说明 |
|-------|------|------|------|------|
| id | BIGINT | - | PRIMARY KEY AUTO_INCREMENT | 数据源ID |
| name | VARCHAR | 100 | NOT NULL | 数据源名称 |
| type | VARCHAR | 20 | NOT NULL | 类型（rss/web） |
| url | VARCHAR | 500 | NOT NULL | 源地址 |
| config | JSON | - | NULL | 配置信息（JSON格式） |
| cron_expression | VARCHAR | 50 | NULL | Cron表达式 |
| is_active | TINYINT | 1 | DEFAULT 1 | 是否启用（1=是，0=否） |
| last_crawl_time | DATETIME | - | NULL | 最后采集时间 |
| user_id | BIGINT | - | NULL | 所属用户 |
| created_at | DATETIME | - | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | - | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (`id`)
- KEY `idx_user_id` (`user_id`)
- KEY `idx_type` (`type`)
- KEY `idx_is_active` (`is_active`)
- FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE

**采集日志表 (crawler_logs)**

| 字段名 | 类型 | 长度 | 约束 | 说明 |
|-------|------|------|------|------|
| id | BIGINT | - | PRIMARY KEY AUTO_INCREMENT | 日志ID |
| source_id | BIGINT | - | NOT NULL | 数据源ID |
| status | VARCHAR | 20 | NOT NULL | 状态（success/failed） |
| items_count | INT | - | DEFAULT 0 | 采集条数 |
| new_items_count | INT | - | DEFAULT 0 | 新增条数 |
| error_message | TEXT | - | NULL | 错误信息 |
| duration_seconds | DECIMAL | 10,2 | NULL | 耗时（秒） |
| created_at | DATETIME | - | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：**
- PRIMARY KEY (`id`)
- KEY `idx_source_id` (`source_id`)
- KEY `idx_status` (`status`)
- KEY `idx_created_at` (`created_at`)
- FOREIGN KEY (`source_id`) REFERENCES `sources`(`id`) ON DELETE CASCADE

**检索历史表 (search_history)**

| 字段名 | 类型 | 长度 | 约束 | 说明 |
|-------|------|------|------|------|
| id | BIGINT | - | PRIMARY KEY AUTO_INCREMENT | 历史ID |
| user_id | BIGINT | - | NOT NULL | 用户ID |
| query | TEXT | - | NOT NULL | 查询内容 |
| search_type | VARCHAR | 20 | NOT NULL | 类型（semantic/rag） |
| results_count | INT | - | DEFAULT 0 | 结果数量 |
| response_time | DECIMAL | 10,2 | NULL | 响应时间（秒） |
| created_at | DATETIME | - | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：**
- PRIMARY KEY (`id`)
- KEY `idx_user_id` (`user_id`)
- KEY `idx_created_at` (`created_at`)
- FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE

### 4.2 向量数据库设计（FAISS）

#### 4.2.1 索引结构

```java
// FAISS索引配置
public class FaissConfig {
    public static final String INDEX_TYPE = "IndexFlatIP";  // 内积索引（适用于384维向量）
    public static final int DIMENSION = 384;                // all-MiniLM-L6-v2模型维度
    public static final String METRIC_TYPE = "INNER_PRODUCT";  // 内积度量
}
```

#### 4.2.2 向量存储结构

```java
// 向量数据结构
@Component
public class VectorStoreManager {
    private Index index;                          // FAISS索引对象
    private Map<Long, Long> idMap;                // ID映射: 向量ID -> 知识库ID
    private Map<Long, VectorMetadata> metadata;    // 元数据缓存
    
    @PostConstruct
    public void init() {
        this.index = new IndexFlatIP(384);
        this.idMap = new ConcurrentHashMap<>();
        this.metadata = new ConcurrentHashMap<>();
    }
}

@Data
public class VectorMetadata {
    private String title;
    private String source;
    private LocalDateTime publishTime;
}
```

#### 4.2.3 索引持久化

```java
// 保存索引
public void saveIndex(Index index, String path) {
    swigfaiss.write_index(index, "data/faiss/knowledge.index");
}

// 加载索引
public Index loadIndex(String path) {
    return swigfaiss.read_index("data/faiss/knowledge.index");
}
```

### 4.3 文件存储设计

#### 4.3.1 目录结构

```
data/
├── uploads/                  # 用户上传文件
│   ├── documents/           # 文档文件
│   │   ├── pdf/
│   │   ├── docx/
│   │   └── txt/
│   └── structured/          # 结构化文件
│       ├── excel/
│       └── csv/
└── faiss/
    ├── knowledge.index      # FAISS索引文件
    └── id_map.json         # ID映射文件（JSON）
```

**说明：** MySQL数据库由独立的MySQL服务器管理，不在应用数据目录中。

#### 4.3.2 文件命名规则

```
{knowledge_id}_{timestamp}_{original_filename}
例如: 12345_20251015143022_report.pdf
```

---

## 5. 接口设计

### 5.1 API设计规范

#### 5.1.1 RESTful规范

- 使用HTTP方法语义：GET（查询）、POST（创建）、PUT（更新）、DELETE（删除）
- URL使用名词复数形式：`/api/v1/knowledge`
- 使用HTTP状态码：200（成功）、201（创建成功）、400（请求错误）、401（未授权）、404（未找到）、500（服务器错误）
- 统一响应格式

#### 5.1.2 统一响应格式

```json
{
    "code": 200,
    "message": "success",
    "data": {
        // 业务数据
    },
    "timestamp": 1697384523
}
```

错误响应：
```json
{
    "code": 400,
    "message": "参数错误",
    "error": "title字段不能为空",
    "timestamp": 1697384523
}
```

### 5.2 API接口清单

#### 5.2.1 认证接口

| 接口路径 | 方法 | 功能 | 权限 |
|---------|------|------|------|
| `/api/v1/auth/register` | POST | 用户注册 | 公开 |
| `/api/v1/auth/login` | POST | 用户登录 | 公开 |
| `/api/v1/auth/refresh` | POST | 刷新Token | 需登录 |
| `/api/v1/auth/logout` | POST | 用户登出 | 需登录 |
| `/api/v1/auth/profile` | GET | 获取个人信息 | 需登录 |

**示例：用户登录**

请求：
```http
POST /api/v1/auth/login
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "password123"
}
```

响应：
```json
{
    "code": 200,
    "message": "登录成功",
    "data": {
        "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
        "refresh_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
        "expires_in": 604800,
        "user": {
            "id": 1,
            "email": "user@example.com",
            "username": "用户昵称"
        }
    }
}
```

#### 5.2.2 知识库接口

| 接口路径 | 方法 | 功能 | 权限 |
|---------|------|------|------|
| `/api/v1/knowledge` | GET | 获取知识列表 | 需登录 |
| `/api/v1/knowledge/:id` | GET | 获取知识详情 | 需登录 |
| `/api/v1/knowledge` | POST | 创建知识条目 | 需登录 |
| `/api/v1/knowledge/:id` | PUT | 更新知识条目 | 需登录 |
| `/api/v1/knowledge/:id` | DELETE | 删除知识条目 | 需登录 |
| `/api/v1/knowledge/batch-delete` | POST | 批量删除 | 需登录 |
| `/api/v1/knowledge/upload` | POST | 文件上传 | 需登录 |
| `/api/v1/knowledge/export` | GET | 导出数据 | 需登录 |

**示例：获取知识列表**

请求：
```http
GET /api/v1/knowledge?page=1&size=20&type=rss&start_date=2025-10-01&tag=AI
Authorization: Bearer {access_token}
```

响应：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": 156,
        "page": 1,
        "size": 20,
        "items": [
            {
                "id": 123,
                "title": "大模型最新进展",
                "summary": "摘要内容...",
                "source_type": "rss",
                "source_name": "36氪",
                "author": "张三",
                "publish_time": "2025-10-15T10:30:00",
                "tags": ["AI", "大模型"],
                "view_count": 25,
                "created_at": "2025-10-15T11:00:00"
            }
        ]
    }
}
```

#### 5.2.3 检索接口

| 接口路径 | 方法 | 功能 | 权限 |
|---------|------|------|------|
| `/api/v1/search/semantic` | POST | 语义检索 | 需登录 |
| `/api/v1/search/rag` | POST | RAG问答 | 需登录 |
| `/api/v1/search/history` | GET | 检索历史 | 需登录 |

**示例：RAG问答**

请求：
```http
POST /api/v1/search/rag
Authorization: Bearer {access_token}
Content-Type: application/json

{
    "question": "最近关于人工智能监管的讨论有哪些?",
    "top_k": 5,
    "use_rerank": true,
    "enable_web_search": true
}
```

响应：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "answer": "根据知识库内容，最近关于人工智能监管的讨论主要集中在以下几个方面：\n1. 欧盟AI法案...\n2. 中国生成式AI管理办法...",
        "sources": [
            {
                "knowledge_id": 456,
                "title": "欧盟AI法案正式通过",
                "similarity": 0.89,
                "snippet": "...相关段落..."
            }
        ],
        "from_web": false,
        "confidence": "high",
        "response_time": 3.2
    }
}
```

#### 5.2.4 分析接口

| 接口路径 | 方法 | 功能 | 权限 |
|---------|------|------|------|
| `/api/v1/analytics/keywords` | GET | Top关键词 | 需登录 |
| `/api/v1/analytics/trends` | GET | 关键词趋势 | 需登录 |
| `/api/v1/analytics/clusters` | GET | 聚类分析 | 需登录 |
| `/api/v1/analytics/statistics` | GET | 统计数据 | 需登录 |

**示例：获取Top关键词**

请求：
```http
GET /api/v1/analytics/keywords?top_n=10&time_range=30d
Authorization: Bearer {access_token}
```

响应：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "keywords": [
            {"word": "人工智能", "count": 156, "growth": 0.23},
            {"word": "大模型", "count": 132, "growth": 0.45},
            {"word": "ChatGPT", "count": 98, "growth": -0.12}
        ],
        "time_range": "2025-09-15 - 2025-10-15"
    }
}
```

#### 5.2.5 数据源管理接口

| 接口路径 | 方法 | 功能 | 权限 |
|---------|------|------|------|
| `/api/v1/sources` | GET | 获取数据源列表 | 需登录 |
| `/api/v1/sources` | POST | 添加数据源 | 需登录 |
| `/api/v1/sources/:id` | PUT | 更新数据源 | 需登录 |
| `/api/v1/sources/:id` | DELETE | 删除数据源 | 需登录 |
| `/api/v1/sources/:id/trigger` | POST | 手动触发采集 | 需登录 |
| `/api/v1/sources/logs` | GET | 获取采集日志 | 需登录 |

### 5.3 外部API集成

#### 5.3.1 Ollama API

```java
// LLM调用 (使用LangChain4j)
@Service
public class OllamaClient {
    private final OllamaChatModel chatModel;
    
    public OllamaClient() {
        this.chatModel = OllamaChatModel.builder()
            .baseUrl("http://localhost:11434")
            .modelName("qwen2.5:3b")
            .temperature(0.7)
            .build();
    }
    
    public String generate(String prompt) {
        return chatModel.generate(prompt);
    }
}
```

#### 5.3.2 百度搜索API（联网查询）

```java
// 百度搜索API（需申请）
@Service
public class BaiduSearchService {
    @Value("${baidu.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    
    public SearchResult search(String query) {
        String url = String.format(
            "https://api.baidu.com/json?q=%s&key=%s", 
            query, apiKey
        );
        return restTemplate.getForObject(url, SearchResult.class);
    }
}
```

---

## 6. 安全设计

### 6.1 身份认证

#### 6.1.1 JWT认证流程

```
1. 用户登录 → 验证用户名密码
2. 验证通过 → 生成Access Token（有效期7天）+ Refresh Token（有效期30天）
3. 客户端存储Token → localStorage
4. 请求API → Header携带: Authorization: Bearer {access_token}
5. 服务端验证Token → 解析payload，验证签名和过期时间
6. Token即将过期 → 使用Refresh Token刷新
```

#### 6.1.2 JWT Payload结构

```json
{
    "user_id": 1,
    "email": "user@example.com",
    "exp": 1697984523,
    "iat": 1697384523,
    "type": "access"
}
```

### 6.2 数据安全

#### 6.2.1 密码存储

- 使用BCrypt算法加密
- Salt轮次：12轮
- 不存储明文密码

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class PasswordService {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    
    // 加密
    public String encode(String password) {
        return encoder.encode(password);
    }
    
    // 验证
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
```

#### 6.2.2 敏感配置加密

```java
// 使用Jasypt加密API Key等敏感信息
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

@Configuration
public class EncryptionConfig {
    public String encryptApiKey(String apiKey) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("encryption-key");
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        return encryptor.encrypt(apiKey);
    }
}
```

### 6.3 接口安全

#### 6.3.1 请求频率限制

```java
// 使用Bucket4j或自定义拦截器实现频率限制
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        String clientIp = request.getRemoteAddr();
        AtomicInteger count = requestCounts.computeIfAbsent(
            clientIp, k -> new AtomicInteger(0)
        );
        
        if (count.incrementAndGet() > 100) {
            response.setStatus(429); // Too Many Requests
            return false;
        }
        return true;
    }
}

// 特定接口限制使用注解
@RateLimit(value = 10, duration = 60) // 10次/分钟
@PostMapping("/search/rag")
public ResponseEntity<?> ragAnswer(@RequestBody RagRequest request) {
    // ...
}
```

#### 6.3.2 输入验证

- 使用Hibernate Validator进行参数校验
- 防止SQL注入（使用MyBatis/JPA参数化查询）
- 防止XSS攻击（前端输出转义，后端使用ESAPI）
- 文件上传白名单：.pdf, .docx, .txt, .xlsx, .csv

```java
@Data
public class KnowledgeCreateDTO {
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题不能超过255字符")
    private String title;
    
    @NotBlank(message = "内容不能为空")
    private String content;
    
    @Pattern(regexp = "^https?://.*", message = "URL格式不正确")
    private String sourceUrl;
}
```

### 6.4 HTTPS加密

生产环境使用Nginx配置SSL证书：

```nginx
server {
    listen 443 ssl;
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
}
```

---

## 7. 性能设计

### 7.1 数据库优化

#### 7.1.1 索引设计

**说明：** 索引已在表结构定义时创建（见4.1.2节），以下为额外的性能优化索引：

```sql
-- knowledge表额外优化索引
-- 复合索引：用于常见的组合查询
CREATE INDEX idx_knowledge_user_type ON knowledge(user_id, source_type);
CREATE INDEX idx_knowledge_user_time ON knowledge(user_id, created_at DESC);

-- search_history表额外优化索引
CREATE INDEX idx_search_user_time ON search_history(user_id, created_at DESC);

-- sources表优化索引
CREATE INDEX idx_sources_active_type ON sources(is_active, type);

-- 全文索引（用于标题和内容搜索）
ALTER TABLE knowledge ADD FULLTEXT INDEX ft_idx_title (title);
ALTER TABLE knowledge ADD FULLTEXT INDEX ft_idx_content (content);
```

#### 7.1.2 查询优化

- 分页查询使用LIMIT + OFFSET
- 避免SELECT *，按需查询字段
- 使用JOIN代替多次查询

### 7.2 向量检索优化

#### 7.2.1 FAISS索引选择

- 数据量 < 1万：IndexFlatIP（精确检索）
- 数据量 1万-10万：IndexIVFFlat（倒排索引）
- 数据量 > 10万：IndexHNSWFlat（图索引）

```java
// 根据数据量动态选择索引
@Service
public class FaissIndexFactory {
    public Index createIndex(int dimension, long vectorCount) {
        if (vectorCount < 10000) {
            return new IndexFlatIP(dimension);
        } else if (vectorCount < 100000) {
            int nlist = 100;
            IndexFlatIP quantizer = new IndexFlatIP(dimension);
            return new IndexIVFFlat(quantizer, dimension, nlist);
        } else {
            return new IndexHNSWFlat(dimension, 32);
        }
    }
}
```

#### 7.2.2 向量缓存

- 使用Redis缓存热点查询的向量结果
- TTL设置为1小时

### 7.3 LLM推理优化

#### 7.3.1 Prompt优化

- 控制上下文长度 < 2000 tokens
- 使用简洁的Prompt模板

```java
@Component
public class PromptTemplate {
    private static final String RAG_TEMPLATE = """
        基于以下内容回答问题：
        %s
        
        问题：%s
        答案：
        """;
    
    public String buildRagPrompt(String context, String question) {
        return String.format(RAG_TEMPLATE, context, question);
    }
}
```

#### 7.3.2 批处理

- 嵌入模型支持批量向量化（batch_size=32）
- 减少模型调用次数

### 7.4 前端性能优化

- 组件懒加载：`defineAsyncComponent`
- 虚拟列表：长列表使用虚拟滚动
- 图片懒加载
- Gzip压缩
- CDN加速静态资源

---

## 8. 部署方案

### 8.1 环境要求

#### 8.1.1 硬件要求

| 配置项 | 最低要求 | 推荐配置 |
|-------|---------|---------|
| CPU | 4核 | 8核+ |
| 内存 | 8GB | 16GB+ |
| 硬盘 | 50GB SSD | 200GB SSD |
| GPU | 无 | NVIDIA GPU（可选） |

#### 8.1.2 软件要求

| 软件 | 版本 |
|------|------|
| 操作系统 | Ubuntu 20.04+ / Windows 10+ / macOS 11+ |
| JDK | 11+ / 17+ (推荐) |
| Maven | 3.8+ (或Gradle 7.6+) |
| MySQL | 8.0+ |
| Node.js | 16+ |
| Ollama | 0.1+ |
| Nginx | 1.18+ |

### 8.2 Docker部署

#### 8.2.1 Dockerfile（后端）

```dockerfile
# 多阶段构建
FROM maven:3.8-openjdk-17 AS builder

WORKDIR /app

# 复制pom.xml并下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:17-jdk-slim

WORKDIR /app

# 从构建阶段复制jar包
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 8.2.2 docker-compose.yml

```yaml
version: '3.8'

services:
  nginx:
    image: nginx:latest
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./frontend/dist:/usr/share/nginx/html
    depends_on:
      - backend
    networks:
      - app-network

  mysql:
    image: mysql:8.0
    container_name: xu-news-mysql
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-root123456}
      - MYSQL_DATABASE=xu_news_rag
      - MYSQL_USER=xu_user
      - MYSQL_PASSWORD=${MYSQL_PASSWORD:-xu_password}
      - TZ=Asia/Shanghai
    volumes:
      - mysql-data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
      - --default-authentication-plugin=mysql_native_password
    networks:
      - app-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend
    container_name: xu-news-backend
    ports:
      - "8080:8080"
    volumes:
      - ./data:/app/data
      - ./logs:/app/logs
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/xu_news_rag?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
      - SPRING_DATASOURCE_USERNAME=xu_user
      - SPRING_DATASOURCE_PASSWORD=${MYSQL_PASSWORD:-xu_password}
      - OLLAMA_BASE_URL=http://ollama:11434
    depends_on:
      mysql:
        condition: service_healthy
      ollama:
        condition: service_started
    networks:
      - app-network

  ollama:
    image: ollama/ollama:latest
    container_name: xu-news-ollama
    ports:
      - "11434:11434"
    volumes:
      - ollama-data:/root/.ollama
    networks:
      - app-network

volumes:
  mysql-data:
  ollama-data:

networks:
  app-network:
    driver: bridge
```

**环境变量配置 (.env)：**
```bash
# MySQL配置
MYSQL_ROOT_PASSWORD=your_root_password
MYSQL_PASSWORD=your_user_password

# JWT配置
JWT_SECRET=your_jwt_secret_key

# 邮件配置
MAIL_PASSWORD=your_mail_password
```

### 8.3 传统部署

#### 8.3.1 后端部署步骤

```bash
# 1. 安装并启动MySQL
# Ubuntu/Debian:
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql

# 创建数据库和用户
mysql -u root -p << EOF
CREATE DATABASE xu_news_rag CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'xu_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON xu_news_rag.* TO 'xu_user'@'localhost';
FLUSH PRIVILEGES;
EOF

# 2. 初始化数据库表结构（可选，也可以用Flyway自动迁移）
mysql -u xu_user -p xu_news_rag < init.sql

# 3. 配置应用
# 编辑 application.yml 或使用环境变量
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/xu_news_rag"
export SPRING_DATASOURCE_USERNAME="xu_user"
export SPRING_DATASOURCE_PASSWORD="your_password"

# 4. 构建项目
cd backend
mvn clean package -DskipTests

# 5. 启动应用
java -jar target/xu-news-rag-1.0.0.jar

# 或者使用Maven插件运行
mvn spring-boot:run

# 生产环境推荐使用systemd管理
# 创建服务文件 /etc/systemd/system/xu-news-rag.service
```

**systemd服务配置示例：**
```ini
[Unit]
Description=XU News RAG Service
After=network.target

[Service]
Type=simple
User=app
WorkingDirectory=/opt/xu-news-rag
ExecStart=/usr/bin/java -jar /opt/xu-news-rag/xu-news-rag-1.0.0.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

#### 8.3.2 前端部署步骤

```bash
# 1. 安装依赖
cd frontend
npm install

# 2. 构建生产版本
npm run build

# 3. 部署到Nginx
cp -r dist/* /var/www/html/
```

#### 8.3.3 Nginx配置

```nginx
server {
    listen 80;
    server_name localhost;

    # 前端静态资源
    location / {
        root /var/www/html;
        try_files $uri $uri/ /index.html;
    }

    # API反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 文件上传大小限制
    client_max_body_size 50M;
}
```

---

## 9. 运维监控

### 9.1 日志管理

#### 9.1.1 日志分类

```
logs/
├── app.log              # 应用日志
├── error.log            # 错误日志
├── access.log           # 访问日志
├── crawler.log          # 采集日志
└── performance.log      # 性能日志
```

#### 9.1.2 日志格式

**Logback配置文件 (logback-spring.xml):**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- 控制台输出 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>
    
    <!-- 应用日志 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/app.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/app.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>
    
    <!-- 错误日志 -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/error.log</file>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/error.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
        <appender-ref ref="ERROR_FILE"/>
    </root>
</configuration>
```

**Java代码中使用日志：**

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ExampleService {
    public void doSomething() {
        log.info("执行业务操作");
        log.warn("警告信息");
        log.error("错误信息", exception);
    }
}
```

### 9.2 监控指标

#### 9.2.1 系统指标

- CPU使用率
- 内存使用率
- 磁盘使用率
- 网络流量

#### 9.2.2 应用指标

- API响应时间
- API错误率
- QPS（每秒请求数）
- 采集成功率
- 知识库条目数
- 用户活跃度

### 9.3 告警机制

```java
// 邮件告警示例
@Service
public class AlertService {
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${alert.email.to}")
    private String adminEmail;
    
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void checkSystemHealth() {
        double cpuUsage = getSystemCpuUsage();
        
        if (cpuUsage > 90) {
            sendAlert(
                "[告警] CPU使用率过高",
                String.format("当前CPU使用率: %.2f%%", cpuUsage)
            );
        }
    }
    
    private void sendAlert(String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(adminEmail);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("noreply@xu-news-rag.com");
        
        mailSender.send(message);
        log.warn("发送告警邮件: {}", subject);
    }
    
    private double getSystemCpuUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        return osBean.getSystemLoadAverage() / osBean.getAvailableProcessors() * 100;
    }
}
```

---

## 10. 附录

### 10.1 技术风险与应对

| 风险项 | 风险等级 | 应对措施 |
|-------|---------|---------|
| 本地LLM推理慢 | 中 | 优化Prompt长度，考虑GPU加速 |
| FAISS索引重建耗时 | 中 | 增量更新，定期后台重建 |
| 网页抓取失败率高 | 中 | 增加重试机制，使用代理IP |
| MySQL连接池耗尽 | 低 | 使用HikariCP连接池，合理配置参数 |
| 数据库锁竞争 | 低 | 优化索引，使用乐观锁，避免长事务 |

### 10.2 后续优化方向

1. **性能优化**
   - 引入Redis缓存
   - 数据库读写分离
   - CDN加速静态资源

2. **功能增强**
   - 支持多用户协作
   - 移动端适配
   - 语音交互

3. **运维优化**
   - CI/CD流水线
   - 自动化测试
   - 性能监控面板

### 10.3 核心配置文件示例

#### 10.3.1 数据库初始化脚本 (init.sql)

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS xu_news_rag 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

USE xu_news_rag;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
  username VARCHAR(50) COMMENT '用户昵称',
  is_active TINYINT(1) DEFAULT 1 COMMENT '是否激活',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 知识条目表
CREATE TABLE IF NOT EXISTS knowledge (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '条目ID',
  title VARCHAR(255) NOT NULL COMMENT '标题',
  content LONGTEXT NOT NULL COMMENT '正文内容',
  summary TEXT COMMENT '摘要',
  source_url VARCHAR(500) COMMENT '来源URL',
  source_type VARCHAR(20) NOT NULL COMMENT '来源类型',
  source_name VARCHAR(100) COMMENT '来源名称',
  author VARCHAR(100) COMMENT '作者',
  publish_time DATETIME COMMENT '发布时间',
  content_hash VARCHAR(64) NOT NULL COMMENT '内容哈希',
  vector_id BIGINT COMMENT '对应向量ID',
  quality_score DECIMAL(3,2) COMMENT '质量评分',
  view_count INT DEFAULT 0 COMMENT '查看次数',
  user_id BIGINT COMMENT '所属用户',
  source_id BIGINT COMMENT '数据源ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_content_hash (content_hash),
  UNIQUE KEY uk_vector_id (vector_id),
  INDEX idx_user_id (user_id),
  INDEX idx_source_type (source_type),
  INDEX idx_publish_time (publish_time),
  INDEX idx_created_at (created_at),
  FULLTEXT INDEX ft_idx_title (title),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
  FOREIGN KEY (source_id) REFERENCES sources(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识条目表';

-- 标签表
CREATE TABLE IF NOT EXISTS tags (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
  name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
  category VARCHAR(50) COMMENT '标签分类',
  count INT DEFAULT 0 COMMENT '使用次数',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 知识标签关联表
CREATE TABLE IF NOT EXISTS knowledge_tags (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
  knowledge_id BIGINT NOT NULL COMMENT '知识ID',
  tag_id BIGINT NOT NULL COMMENT '标签ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_knowledge_tag (knowledge_id, tag_id),
  INDEX idx_knowledge_id (knowledge_id),
  INDEX idx_tag_id (tag_id),
  FOREIGN KEY (knowledge_id) REFERENCES knowledge(id) ON DELETE CASCADE,
  FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识标签关联表';

-- 数据源表
CREATE TABLE IF NOT EXISTS sources (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据源ID',
  name VARCHAR(100) NOT NULL COMMENT '数据源名称',
  type VARCHAR(20) NOT NULL COMMENT '类型',
  url VARCHAR(500) NOT NULL COMMENT '源地址',
  config JSON COMMENT '配置信息',
  cron_expression VARCHAR(50) COMMENT 'Cron表达式',
  is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  last_crawl_time DATETIME COMMENT '最后采集时间',
  user_id BIGINT COMMENT '所属用户',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_id (user_id),
  INDEX idx_type (type),
  INDEX idx_is_active (is_active),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据源表';

-- 采集日志表
CREATE TABLE IF NOT EXISTS crawler_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
  source_id BIGINT NOT NULL COMMENT '数据源ID',
  status VARCHAR(20) NOT NULL COMMENT '状态',
  items_count INT DEFAULT 0 COMMENT '采集条数',
  new_items_count INT DEFAULT 0 COMMENT '新增条数',
  error_message TEXT COMMENT '错误信息',
  duration_seconds DECIMAL(10,2) COMMENT '耗时',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_source_id (source_id),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at),
  FOREIGN KEY (source_id) REFERENCES sources(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采集日志表';

-- 检索历史表
CREATE TABLE IF NOT EXISTS search_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  query TEXT NOT NULL COMMENT '查询内容',
  search_type VARCHAR(20) NOT NULL COMMENT '类型',
  results_count INT DEFAULT 0 COMMENT '结果数量',
  response_time DECIMAL(10,2) COMMENT '响应时间',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_user_id (user_id),
  INDEX idx_created_at (created_at),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='检索历史表';
```

#### 10.3.2 MySQL数据库备份与恢复

**备份命令：**
```bash
# 备份整个数据库
mysqldump -u xu_user -p xu_news_rag > backup_$(date +%Y%m%d_%H%M%S).sql

# 只备份数据（不含表结构）
mysqldump -u xu_user -p --no-create-info xu_news_rag > data_backup.sql

# 备份单个表
mysqldump -u xu_user -p xu_news_rag knowledge > knowledge_backup.sql
```

**恢复命令：**
```bash
# 恢复数据库
mysql -u xu_user -p xu_news_rag < backup_20250115_120000.sql

# 恢复到新数据库
mysql -u root -p -e "CREATE DATABASE xu_news_rag_restore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u xu_user -p xu_news_rag_restore < backup_20250115_120000.sql
```

**自动备份脚本（backup.sh）：**
```bash
#!/bin/bash
BACKUP_DIR="/var/backups/mysql"
DB_NAME="xu_news_rag"
DB_USER="xu_user"
DB_PASS="your_password"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_${DATE}.sql"

# 创建备份目录
mkdir -p ${BACKUP_DIR}

# 执行备份
mysqldump -u ${DB_USER} -p${DB_PASS} ${DB_NAME} > ${BACKUP_FILE}

# 压缩备份
gzip ${BACKUP_FILE}

# 删除7天前的备份
find ${BACKUP_DIR} -name "${DB_NAME}_*.sql.gz" -mtime +7 -delete

echo "备份完成: ${BACKUP_FILE}.gz"
```

**添加到crontab（每天凌晨2点自动备份）：**
```bash
0 2 * * * /path/to/backup.sh >> /var/log/mysql_backup.log 2>&1
```

#### 10.3.3 Maven依赖配置 (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.5</version>
    </parent>
    
    <groupId>com.xu</groupId>
    <artifactId>xu-news-rag</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
        <langchain4j.version>0.28.0</langchain4j.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.3</version>
        </dependency>
        
        <!-- MyBatis Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.5.4</version>
        </dependency>
        
        <!-- MySQL Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.0.33</version>
        </dependency>
        
        <!-- HikariCP 连接池（Spring Boot已包含） -->
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
        </dependency>
        
        <!-- Flyway 数据库迁移（可选） -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
        </dependency>
        
        <!-- LangChain4j -->
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j</artifactId>
            <version>${langchain4j.version}</version>
        </dependency>
        
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-ollama</artifactId>
            <version>${langchain4j.version}</version>
        </dependency>
        
        <!-- FAISS Java绑定 -->
        <dependency>
            <groupId>com.github.jelmerk</groupId>
            <artifactId>hnswlib-core</artifactId>
            <version>1.1.0</version>
        </dependency>
        
        <!-- Jsoup (网页解析) -->
        <dependency>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
            <version>1.16.2</version>
        </dependency>
        
        <!-- Rome (RSS解析) -->
        <dependency>
            <groupId>com.rometools</groupId>
            <artifactId>rome</artifactId>
            <version>2.1.0</version>
        </dependency>
        
        <!-- Apache POI (文档解析) -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml</artifactId>
            <version>5.2.4</version>
        </dependency>
        
        <!-- PDFBox (PDF解析) -->
        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>2.0.29</version>
        </dependency>
        
        <!-- OkHttp -->
        <dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
            <version>4.12.0</version>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Mail -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
        
        <!-- Quartz (定时任务) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-quartz</artifactId>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 10.3.4 应用配置文件 (application.yml)

```yaml
spring:
  application:
    name: xu-news-rag
  
  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/xu_news_rag?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: xu_user
    password: ${MYSQL_PASSWORD:your_password}
    
    # HikariCP 连接池配置
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 30000
      connection-test-query: SELECT 1
    
  # MyBatis Plus配置
  mybatis-plus:
    mapper-locations: classpath:mapper/*.xml
    type-aliases-package: com.xu.newsrag.entity
    configuration:
      map-underscore-to-camel-case: true
      log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
    global-config:
      db-config:
        id-type: auto
        logic-delete-field: deleted
        logic-delete-value: 1
        logic-not-delete-value: 0
  
  # Flyway 数据库迁移配置（可选）
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    encoding: UTF-8
      
  # 文件上传配置
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
      
  # 邮件配置
  mail:
    host: smtp.example.com
    port: 587
    username: noreply@xu-news-rag.com
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

# 服务器配置
server:
  port: 8080
  servlet:
    context-path: /

# JWT配置
jwt:
  secret: ${JWT_SECRET:your-secret-key-change-in-production}
  expiration: 604800000  # 7天(毫秒)
  refresh-expiration: 2592000000  # 30天(毫秒)

# Ollama配置
ollama:
  base-url: http://localhost:11434
  model-name: qwen2.5:3b
  timeout: 60

# FAISS配置
faiss:
  index-path: data/faiss/knowledge.index
  dimension: 384
  
# 告警配置
alert:
  email:
    to: admin@example.com
    enabled: true

# 日志配置
logging:
  level:
    root: INFO
    com.xu.newsrag: DEBUG
  file:
    name: logs/app.log
    
# 定时任务配置
scheduler:
  crawler:
    enabled: true
    cron: "0 0 */1 * * ?"  # 每小时执行一次
```

---

**文档版本：** 1.0  
**最后更新：** 2025年10月15日  
**编写人员：** 技术架构团队

**审批流程：**
- [ ] 技术负责人审批
- [ ] 架构师审批
- [ ] 项目经理审批

