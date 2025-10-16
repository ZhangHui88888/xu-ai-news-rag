# Docker 环境测试指南

## 📋 概述

本指南专门针对在 **VMware Docker** 环境中运行 XU-News-AI-RAG 测试套件。

## 🐳 Docker 环境说明

### 当前环境
- **宿主机**: Windows 10/11
- **虚拟化**: VMware
- **容器**: Docker (MySQL, Ollama, n8n, Backend, Frontend)
- **网络**: Docker Bridge 网络

### 服务地址
- **MySQL**: `mysql:3306` (容器内) / `192.168.171.128:3306` (外部)
- **Ollama**: `http://192.168.171.1:11434` (宿主机)
- **Backend**: `backend:8080` (容器内)
- **n8n**: `n8n:5678` (容器内)

## 🧪 测试运行方式

### 方式一：在Docker容器内运行测试（推荐）

#### 1. 进入Backend容器

```bash
# 如果后端容器正在运行
docker exec -it xu-news-backend sh

# 或者启动一个临时容器
docker run --rm -it \
  --network xu-ai-news-rag_default \
  -v D:/github/funNovels/xu-ai-news-rag/backend:/app \
  -w /app \
  maven:3.9-eclipse-temurin-17 \
  sh
```

#### 2. 在容器内运行测试

```bash
# 运行所有测试
mvn test

# 运行测试并生成覆盖率报告
mvn test jacoco:report

# 运行特定测试类
mvn test -Dtest=JwtUtilTest
```

### 方式二：使用Docker Compose运行测试

创建专门的测试服务：

```yaml
# docker-compose.test.yml
version: '3.8'

services:
  backend-test:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: xu-news-backend-test
    environment:
      - SPRING_PROFILES_ACTIVE=test
    networks:
      - xu-news-network
    command: mvn test jacoco:report
    volumes:
      - ./backend:/app
      - maven-repo:/root/.m2

volumes:
  maven-repo:

networks:
  xu-news-network:
    external: true
```

运行测试：

```bash
docker-compose -f docker-compose.test.yml up --abort-on-container-exit
```

### 方式三：在宿主机Windows上运行测试

#### 前置条件
- 已安装 JDK 17+
- 已安装 Maven 3.6+

#### 配置测试环境

由于测试使用H2内存数据库，不需要连接Docker中的MySQL，可以直接在Windows上运行：

```bash
# PowerShell
cd D:\github\funNovels\xu-ai-news-rag\backend

# 设置测试环境
$env:SPRING_PROFILES_ACTIVE="test"

# 运行测试
mvn test

# 运行测试并生成报告
mvn test jacoco:report
```

## 🔧 Docker环境测试配置

### 测试配置文件 (application-test.yml)

测试使用 **H2内存数据库**，完全独立于Docker中的MySQL：

```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:test_xu_news_rag
    username: sa
    password: 
```

### Mock外部依赖

测试中的外部服务都已Mock，不依赖Docker服务：
- ✅ **Ollama** - 使用Mockito模拟
- ✅ **向量存储** - 使用临时文件系统
- ✅ **MySQL** - 使用H2内存数据库
- ✅ **n8n** - 不涉及测试

## 📊 在Docker中查看测试报告

### 1. 运行测试并生成报告

```bash
docker exec -it xu-news-backend sh -c "cd /app && mvn test jacoco:report"
```

### 2. 复制报告到宿主机

```bash
# 复制JaCoCo HTML报告
docker cp xu-news-backend:/app/target/site/jacoco ./test-reports/jacoco

# 复制Surefire报告
docker cp xu-news-backend:/app/target/surefire-reports ./test-reports/surefire
```

### 3. 在Windows浏览器中查看

```bash
# PowerShell
start ./test-reports/jacoco/index.html
```

## 🚀 快速测试脚本

### Linux/Mac (在Docker容器内)

```bash
#!/bin/bash
# test-in-docker.sh

echo "在Docker容器内运行测试..."

docker exec -it xu-news-backend sh -c "
    cd /app && \
    mvn clean test jacoco:report && \
    echo '测试完成！' && \
    echo '报告位置: /app/target/site/jacoco/index.html'
"

echo "复制报告到宿主机..."
docker cp xu-news-backend:/app/target/site/jacoco ./test-reports/

echo "测试报告已复制到: ./test-reports/jacoco/index.html"
```

### Windows (PowerShell)

```powershell
# test-in-docker.ps1

Write-Host "在Docker容器内运行测试..." -ForegroundColor Green

docker exec -it xu-news-backend sh -c @"
    cd /app && \
    mvn clean test jacoco:report && \
    echo '测试完成！' && \
    echo '报告位置: /app/target/site/jacoco/index.html'
"@

Write-Host "复制报告到宿主机..." -ForegroundColor Green
docker cp xu-news-backend:/app/target/site/jacoco ./test-reports/

Write-Host "测试报告已复制到: ./test-reports/jacoco/index.html" -ForegroundColor Cyan
Start-Process "./test-reports/jacoco/index.html"
```

## 🔍 常见问题

### Q1: Docker容器内Maven下载依赖很慢

**解决方案**: 配置Maven镜像

```bash
# 在容器内创建 settings.xml
docker exec -it xu-news-backend sh -c "mkdir -p /root/.m2"

cat > /tmp/settings.xml << EOF
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <name>Aliyun Maven</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
EOF

docker cp /tmp/settings.xml xu-news-backend:/root/.m2/settings.xml
```

### Q2: 测试时连接不到Docker中的MySQL

**答案**: 测试不需要连接MySQL！

测试使用H2内存数据库，完全独立运行，不依赖Docker中的MySQL服务。

### Q3: 如何在测试中Mock Ollama服务

测试中已经Mock了Ollama客户端：

```java
@MockBean
private OllamaClient ollamaClient;

when(ollamaClient.generate(anyString(), anyString()))
    .thenReturn("Mock AI Response");
```

### Q4: 测试失败提示找不到Java或Maven

**解决方案**: 确保使用正确的容器

```bash
# 检查容器中的Java版本
docker exec -it xu-news-backend java -version

# 检查Maven版本
docker exec -it xu-news-backend mvn -version
```

### Q5: 如何在CI/CD中运行测试

GitHub Actions示例：

```yaml
name: Test in Docker

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build Docker Image
        run: |
          cd backend
          docker build -t xu-news-backend:test .
      
      - name: Run Tests
        run: |
          docker run --rm \
            -v $(pwd)/backend:/app \
            xu-news-backend:test \
            mvn test jacoco:report
      
      - name: Upload Coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./backend/target/site/jacoco/jacoco.xml
```

## 📝 测试最佳实践（Docker环境）

1. **使用卷挂载** - 将代码挂载到容器中，便于实时更新
2. **缓存Maven依赖** - 使用Docker卷缓存 `.m2` 目录
3. **独立测试网络** - 测试不依赖其他Docker服务
4. **并行测试** - 可以同时在多个容器中运行不同的测试
5. **报告导出** - 测试完成后及时导出报告到宿主机

## 🎯 推荐测试流程

### 开发阶段

```bash
# 1. 在Windows IDE中编写代码和测试
# 2. 在Windows本地快速运行测试
mvn test -Dtest=MyNewTest

# 3. 提交前在Docker中完整测试
docker exec -it xu-news-backend mvn test
```

### 集成阶段

```bash
# 1. 清理构建
docker exec -it xu-news-backend mvn clean

# 2. 完整测试
docker exec -it xu-news-backend mvn test jacoco:report

# 3. 导出报告
docker cp xu-news-backend:/app/target/site/jacoco ./test-reports/

# 4. 查看覆盖率
start ./test-reports/jacoco/index.html
```

## 📞 技术支持

遇到问题？
1. 查看测试日志: `docker logs xu-news-backend`
2. 查看Surefire报告: `backend/target/surefire-reports/`
3. 联系项目负责人

---

**最后更新**: 2025-10-16  
**适用环境**: VMware Docker  
**版本**: 1.0.0

