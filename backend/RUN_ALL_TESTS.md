# 运行所有测试 - 完整指南

## 📊 测试套件概览

现在项目包含完整的测试套件：

### ✅ 测试类清单（共9个）

#### 1. 工具类测试 (4个)
- `JwtUtilTest` - JWT工具类测试 (11个用例)
- `VectorStoreTest` - 向量存储测试 (10个用例)
- `OllamaClientTest` - Ollama客户端测试 (5个用例)
- `FileProcessorTest` - 文件处理器测试 (10个用例)

#### 2. 服务层测试 (1个)
- `UserServiceTest` - 用户服务测试 (10个用例)

#### 3. 控制器测试 (3个)
- `AuthControllerTest` - 认证控制器测试 (7个用例)
- `QueryControllerTest` - 查询控制器测试 (5个用例)
- `KnowledgeControllerTest` - 知识库控制器测试 (4个用例)

#### 4. 集成测试 (2个)
- `UserMapperTest` - 用户Mapper测试 (7个用例)
- `FullWorkflowTest` - 完整工作流测试 (8个用例)
- `SecurityTest` - 安全性测试 (6个用例)

**总计**: 9个测试类，80+个测试用例

## 🚀 运行测试

### 方式一：使用脚本（Linux服务器）

```bash
cd ~/xu-ai-news-rag

# 赋予执行权限
chmod +x run-test-docker.sh

# 运行所有测试
./run-test-docker.sh

# 运行测试并生成覆盖率报告
./run-test-docker.sh --coverage

# 运行特定测试类
./run-test-docker.sh --test JwtUtilTest
```

### 方式二：直接使用Docker命令

```bash
# 运行所有测试
docker run --rm \
  -v $(pwd)/backend:/app \
  -v maven-repo-test:/root/.m2 \
  -w /app \
  maven:3.9-eclipse-temurin-17 \
  mvn test

# 运行测试并生成覆盖率报告
docker run --rm \
  -v $(pwd)/backend:/app \
  -v maven-repo-test:/root/.m2 \
  -w /app \
  maven:3.9-eclipse-temurin-17 \
  mvn test jacoco:report

# 运行特定测试
docker run --rm \
  -v $(pwd)/backend:/app \
  -v maven-repo-test:/root/.m2 \
  -w /app \
  maven:3.9-eclipse-temurin-17 \
  mvn test -Dtest=JwtUtilTest
```

### 方式三：在服务器上直接运行（需要安装Maven）

```bash
cd ~/xu-ai-news-rag/backend

# 运行所有测试
mvn test

# 生成覆盖率报告
mvn test jacoco:report

# 查看报告
firefox target/site/jacoco/index.html
```

## 📋 测试报告位置

测试完成后，报告在以下位置：

```
backend/
├── target/
│   ├── surefire-reports/           # 测试结果报告（TXT + XML）
│   │   ├── TEST-*.xml              # 详细测试结果
│   │   └── *.txt                   # 文本格式报告
│   └── site/
│       └── jacoco/                 # 代码覆盖率报告
│           ├── index.html          # 主页（在浏览器中打开）
│           ├── jacoco.xml          # XML格式
│           └── jacoco.csv          # CSV格式
```

### 复制报告到Windows查看

```bash
# 在Linux服务器上
cd ~/xu-ai-news-rag
mkdir -p test-reports
cp -r backend/target/surefire-reports test-reports/
cp -r backend/target/site/jacoco test-reports/

# 然后在Windows上使用WinSCP或其他工具下载test-reports目录
```

## 📊 预期测试结果

### 完全通过的测试
- ✅ JwtUtilTest (11/11)
- ✅ FileProcessorTest (10/10)
- ✅ UserServiceTest (10/10)
- ✅ AuthControllerTest (7/7)
- ✅ QueryControllerTest (5/5)
- ✅ KnowledgeControllerTest (4/4)
- ✅ UserMapperTest (7/7)
- ✅ FullWorkflowTest (8/8)
- ✅ SecurityTest (6/6)

### 可能失败的测试（依赖外部服务）
- ⚠️ VectorStoreTest - 某些测试依赖FAISS初始化
- ⚠️ OllamaClientTest - 需要Ollama服务运行
- ⚠️ FullWorkflowTest中的RAG查询 - 需要Ollama和向量数据

## ⚠️ 注意事项

### 1. 依赖外部服务的测试

某些测试需要外部服务：
- **Ollama** - OllamaClientTest 和 RAG相关测试
- **FAISS** - VectorStoreTest 部分测试

这些测试在外部服务不可用时会优雅地失败，不会阻塞整个测试流程。

### 2. 测试数据库

测试使用 **H2内存数据库**，完全独立于生产MySQL：
- ✅ 自动初始化表结构（schema.sql）
- ✅ 自动加载测试数据（data.sql）
- ✅ 每次测试后自动清理
- ✅ 不影响生产数据

### 3. 首次运行较慢

第一次运行测试需要下载依赖：
- JaCoCo插件
- H2数据库
- 测试框架

后续运行会很快（依赖已缓存）。

## 🎯 测试覆盖率目标

根据PRD文档：
- **目标覆盖率**: > 80%
- **API测试通过率**: > 95%

预期当前测试套件能达到：
- **整体覆盖率**: ~70-75%
- **核心功能覆盖率**: ~85%

## 🐛 调试失败的测试

### 查看详细错误

```bash
# 查看测试失败摘要
cat backend/target/surefire-reports/*.txt

# 查看特定测试的详细日志
cat backend/target/surefire-reports/TEST-com.xu.news.util.JwtUtilTest.xml
```

### 重新运行失败的测试

```bash
# 只运行失败的测试
mvn test -Dsurefire.rerunFailingTestsCount=2

# 运行特定测试方法
mvn test -Dtest=JwtUtilTest#testGenerateToken_Success
```

## 📈 查看覆盖率报告

```bash
# 生成报告
mvn jacoco:report

# 在浏览器中打开（需要图形界面）
xdg-open backend/target/site/jacoco/index.html

# 或复制到Windows查看
```

## ✅ 测试通过清单

测试运行成功后检查：

- [ ] 所有测试类都已执行
- [ ] 关键测试用例通过
- [ ] 无编译错误
- [ ] 覆盖率报告已生成
- [ ] Surefire报告已生成

## 🚀 CI/CD集成

### GitHub Actions示例

```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run Tests
        run: |
          cd backend
          mvn clean test jacoco:report
      
      - name: Upload Coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./backend/target/site/jacoco/jacoco.xml
```

---

**更新时间**: 2025-10-16  
**测试类数**: 9个  
**测试用例数**: 80+  
**预期覆盖率**: 70-75%

