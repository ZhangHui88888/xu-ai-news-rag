# ========================================
# XU-News-AI-RAG 测试数据快速导入脚本
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  XU-News 知识库数据导入工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 测试数据（10条）
$testData = @(
    @{
        title = "Python 文件读写完全指南"
        content = "Python 提供了多种读写文件的方法。使用 open() 函数可以打开文件，配合 read()、readline()、readlines() 方法可以读取文件内容。写入文件使用 write() 方法。建议使用 with 语句自动管理文件资源，避免忘记关闭文件。常用模式包括 'r'(读取)、'w'(写入)、'a'(追加)、'r+'(读写)。二进制文件需要加 'b' 模式。"
        summary = "Python 文件操作的完整指南，包括读取、写入和最佳实践"
        tags = @("Python", "编程", "文件操作", "IO")
        contentType = "article"
        sourceUrl = "https://example.com/python-file-io"
        author = "测试作者"
    },
    @{
        title = "JavaScript 异步编程详解"
        content = "JavaScript 的异步编程主要通过三种方式实现：回调函数、Promise、async/await。回调函数是最基础的方式，但容易产生回调地狱。Promise 提供了更优雅的异步处理方案，支持链式调用和错误处理。async/await 是 ES2017 引入的语法糖，让异步代码看起来像同步代码，极大提升了代码可读性。事件循环是 JavaScript 异步机制的核心，理解事件循环对掌握异步编程至关重要。"
        summary = "JavaScript 异步编程的三种方式及其优缺点对比"
        tags = @("JavaScript", "异步编程", "Promise", "async/await")
        contentType = "article"
        sourceUrl = "https://example.com/js-async"
        author = "测试作者"
    },
    @{
        title = "机器学习基础概念入门"
        content = "机器学习是人工智能的核心分支，让计算机从数据中自动学习规律。主要分为三大类：监督学习（有标签数据训练）、无监督学习（无标签数据聚类）、强化学习（通过奖励信号学习策略）。常用算法包括线性回归、逻辑回归、决策树、随机森林、支持向量机、神经网络等。深度学习是机器学习的子集，使用多层神经网络处理复杂问题。特征工程在传统机器学习中非常重要，而深度学习可以自动学习特征。"
        summary = "机器学习的基本概念、分类和常用算法介绍"
        tags = @("机器学习", "人工智能", "算法", "深度学习")
        contentType = "article"
        sourceUrl = "https://example.com/ml-basics"
        author = "AI 专家"
    },
    @{
        title = "Docker 容器化技术实战"
        content = "Docker 是一个开源的容器化平台，可以将应用及其依赖打包成轻量级、可移植的容器。相比虚拟机，容器共享宿主机内核，启动更快、资源占用更少。Dockerfile 用于定义镜像构建步骤，每条指令都会创建一个新的镜像层。Docker Compose 用于定义和运行多容器应用，通过 YAML 文件配置服务。Docker 的分层存储、镜像复用、网络隔离等特性使其成为现代应用部署的标准工具。"
        summary = "Docker 容器技术的核心概念和实战应用"
        tags = @("Docker", "容器化", "DevOps", "微服务")
        contentType = "article"
        sourceUrl = "https://example.com/docker-guide"
        author = "DevOps 工程师"
    },
    @{
        title = "RESTful API 设计最佳实践"
        content = "RESTful API 是基于 HTTP 协议的 API 设计风格，遵循 REST 架构原则。URL 应该是名词而非动词，使用复数形式表示资源集合。使用标准 HTTP 方法对应 CRUD 操作：GET(查询)、POST(创建)、PUT(更新)、DELETE(删除)。返回合适的 HTTP 状态码：200(成功)、201(创建成功)、400(参数错误)、401(未认证)、403(无权限)、404(未找到)、500(服务器错误)。使用 JSON 作为数据交换格式，提供清晰的错误信息和分页支持。API 版本化和文档化也非常重要。"
        summary = "设计 RESTful API 的规范和最佳实践指南"
        tags = @("REST", "API", "后端开发", "HTTP")
        contentType = "article"
        sourceUrl = "https://example.com/restful-api"
        author = "后端架构师"
    },
    @{
        title = "React Hooks 使用指南"
        content = "React Hooks 是 React 16.8 引入的新特性，让函数组件也能使用状态和生命周期特性。useState 用于管理组件状态，useEffect 处理副作用（如数据获取、订阅、DOM 操作）。useContext 访问 Context 上下文，useReducer 管理复杂状态逻辑。useMemo 和 useCallback 用于性能优化，避免不必要的重新计算和渲染。自定义 Hook 可以抽取组件逻辑，实现代码复用。使用 Hooks 需要遵守规则：只在顶层调用、只在 React 函数中调用。"
        summary = "React Hooks 的核心概念和使用方法详解"
        tags = @("React", "Hooks", "前端框架", "组件")
        contentType = "article"
        sourceUrl = "https://example.com/react-hooks"
        author = "前端专家"
    },
    @{
        title = "MySQL 索引优化技巧"
        content = "索引是提升数据库查询性能的关键。MySQL 主要使用 B+树索引，叶子节点包含完整数据或主键值。创建索引时要考虑查询频率、数据量、字段选择性。复合索引遵循最左前缀原则，只有从左边开始的列组合才能使用索引。避免在索引列上使用函数、类型转换，会导致索引失效。定期使用 ANALYZE TABLE 更新统计信息，使用 EXPLAIN 分析查询执行计划。覆盖索引可以避免回表查询，显著提升性能。索引也有代价，会增加写操作开销和存储空间。"
        summary = "MySQL 索引的原理、使用技巧和性能优化方法"
        tags = @("MySQL", "数据库", "索引", "性能优化")
        contentType = "article"
        sourceUrl = "https://example.com/mysql-index"
        author = "DBA"
    },
    @{
        title = "Git 分支管理策略"
        content = "Git Flow 是广泛使用的分支管理模型。主分支 master/main 始终保持稳定可发布状态，develop 分支用于日常开发。feature 分支从 develop 创建，用于开发新功能，完成后合并回 develop。release 分支用于发布准备，进行最后的测试和 bug 修复。hotfix 分支从 master 创建，用于修复紧急问题，修复后同时合并到 master 和 develop。使用 Pull Request 进行代码审查，确保代码质量。分支命名要清晰规范，如 feature/user-auth、hotfix/login-bug。"
        summary = "Git Flow 分支管理模型和团队协作最佳实践"
        tags = @("Git", "版本控制", "团队协作", "分支管理")
        contentType = "article"
        sourceUrl = "https://example.com/git-flow"
        author = "技术经理"
    },
    @{
        title = "TDD 测试驱动开发实践"
        content = "TDD（Test-Driven Development）是一种软件开发方法论，核心思想是先写测试再写代码。开发流程分为三步：Red（编写一个失败的测试）、Green（编写最少代码让测试通过）、Refactor（重构代码保持测试通过）。TDD 的优势包括：提高代码质量、减少 bug、改善代码设计、增强重构信心、提供活文档。常用测试框架有 JUnit(Java)、pytest(Python)、Jest(JavaScript)。编写好的测试用例需要遵循 FIRST 原则：Fast(快速)、Independent(独立)、Repeatable(可重复)、Self-Validating(自我验证)、Timely(及时)。"
        summary = "测试驱动开发的理念、流程和实践方法"
        tags = @("TDD", "测试", "软件工程", "最佳实践")
        contentType = "article"
        sourceUrl = "https://example.com/tdd-practice"
        author = "敏捷教练"
    },
    @{
        title = "Redis 缓存实战应用"
        content = "Redis 是高性能的内存数据库，常用作缓存层提升系统性能。支持丰富的数据结构：String(字符串)、List(列表)、Set(集合)、Hash(哈希)、Sorted Set(有序集合)。典型应用场景包括：热点数据缓存、会话存储、排行榜、计数器、分布式锁、消息队列。需要注意三大缓存问题：缓存穿透（查询不存在的数据）、缓存击穿（热点 key 失效）、缓存雪崩（大量 key 同时失效）。解决方案包括布隆过滤器、互斥锁、设置随机过期时间等。合理设置过期策略和淘汰策略对系统稳定性很重要。"
        summary = "Redis 缓存的数据结构、应用场景和常见问题解决"
        tags = @("Redis", "缓存", "NoSQL", "性能优化")
        contentType = "article"
        sourceUrl = "https://example.com/redis-cache"
        author = "架构师"
    }
)

# 导入 API 地址
$apiUrl = "http://localhost:8080/api/knowledge/import"

# 计数器
$successCount = 0
$failCount = 0
$total = $testData.Count

Write-Host "准备导入 $total 条测试数据..." -ForegroundColor Yellow
Write-Host ""

# 逐条导入
for ($i = 0; $i -lt $testData.Count; $i++) {
    $entry = $testData[$i]
    $num = $i + 1
    
    Write-Host "[$num/$total] 正在导入: $($entry.title)" -ForegroundColor Cyan
    
    try {
        # 转换为 JSON
        $jsonBody = $entry | ConvertTo-Json -Depth 10
        
        # 发送请求
        $response = Invoke-RestMethod -Uri $apiUrl `
            -Method POST `
            -ContentType "application/json; charset=utf-8" `
            -Body ([System.Text.Encoding]::UTF8.GetBytes($jsonBody)) `
            -ErrorAction Stop
        
        if ($response.code -eq 200) {
            Write-Host "  ✅ 成功！ID: $($response.data.id)" -ForegroundColor Green
            $successCount++
        } else {
            Write-Host "  ❌ 失败: $($response.message)" -ForegroundColor Red
            $failCount++
        }
        
        # 每条数据间隔 2 秒，避免向量化处理过载
        if ($i -lt $testData.Count - 1) {
            Write-Host "  ⏱️  等待 2 秒..." -ForegroundColor Gray
            Start-Sleep -Seconds 2
        }
        
    } catch {
        Write-Host "  ❌ 请求失败: $_" -ForegroundColor Red
        $failCount++
    }
    
    Write-Host ""
}

# 总结
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  导入完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ 成功: $successCount 条" -ForegroundColor Green
Write-Host "❌ 失败: $failCount 条" -ForegroundColor Red
Write-Host "📊 总计: $total 条" -ForegroundColor Yellow
Write-Host ""

if ($successCount -gt 0) {
    Write-Host "提示: 现在可以进行查询测试了！" -ForegroundColor Green
    Write-Host "测试查询: curl -X POST http://localhost:8080/api/query ..." -ForegroundColor Gray
}

