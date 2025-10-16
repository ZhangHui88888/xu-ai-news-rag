# 测试运行问题修复指南

## ❌ 问题：容器中找不到Maven

```bash
docker exec -it xu-news-backend sh -c "cd /app && mvn test"
# 错误：sh: mvn: not found
```

### 原因

生产环境的Docker容器使用了**多阶段构建**，运行时只包含JRE（Java运行环境），不包含Maven构建工具。这是最佳实践，可以减小镜像体积。

## ✅ 解决方案

### **方案一：使用临时Maven容器运行测试（推荐）**

使用提供的脚本，它会创建一个临时的Maven容器来运行测试：

```bash
# Linux/Mac
chmod +x run-test-docker.sh

# 运行所有测试
./run-test-docker.sh

# 运行测试并生成覆盖率
./run-test-docker.sh --coverage

# 运行特定测试
./run-test-docker.sh --test JwtUtilTest
```

**工作原理**：
- 使用 `maven:3.9-eclipse-temurin-17` 镜像
- 挂载你的代码目录
- 缓存Maven依赖到Docker卷
- 运行完测试后自动清理容器

### **方案二：在宿主机（Linux服务器）上运行测试**

测试使用H2内存数据库，完全独立，可以直接在服务器上运行：

```bash
# 1. 确保安装了Maven和JDK 17
java -version
mvn -version

# 如果没有安装
sudo apt update
sudo apt install -y openjdk-17-jdk maven

# 2. 进入后端目录
cd ~/xu-ai-news-rag/backend

# 3. 运行测试
mvn test

# 4. 运行测试并生成覆盖率报告
mvn test jacoco:report

# 5. 查看报告
ls -lh target/site/jacoco/
```

### **方案三：构建专门的测试容器**

使用 `Dockerfile.test` 构建包含Maven的测试镜像：

```bash
# 1. 构建测试镜像
cd ~/xu-ai-news-rag
docker build -f backend/Dockerfile.test -t xu-news-backend-test ./backend

# 2. 运行测试
docker run --rm \
  -v $(pwd)/backend:/app \
  xu-news-backend-test \
  mvn test jacoco:report

# 3. 查看报告
ls -lh backend/target/site/jacoco/
```

### **方案四：使用Docker Compose测试服务**

```bash
# 1. 启动测试服务
docker-compose -f docker-compose.test.yml up --abort-on-container-exit

# 2. 查看测试结果
docker-compose -f docker-compose.test.yml logs backend-test

# 3. 清理
docker-compose -f docker-compose.test.yml down
```

## 🎯 推荐方案对比

| 方案 | 优点 | 缺点 | 推荐场景 |
|-----|------|------|---------|
| 方案一 (临时容器) | ✅ 简单快速<br>✅ 不需要安装<br>✅ 自动清理 | ⚠️ 首次下载镜像较慢 | ⭐⭐⭐⭐⭐ 最推荐 |
| 方案二 (宿主机) | ✅ 最快速<br>✅ 直接查看报告 | ❌ 需要安装JDK/Maven | ⭐⭐⭐⭐ CI/CD环境 |
| 方案三 (测试镜像) | ✅ 可复用<br>✅ 环境一致 | ❌ 需要构建镜像 | ⭐⭐⭐ 团队协作 |
| 方案四 (Compose) | ✅ 配置化<br>✅ 易于管理 | ❌ 配置复杂 | ⭐⭐ 复杂测试场景 |

## 📝 快速命令参考

### 运行所有测试（推荐）

```bash
./run-test-docker.sh --coverage
```

### 运行特定测试

```bash
./run-test-docker.sh --test JwtUtilTest --coverage
```

### 查看测试报告

```bash
# 在浏览器中打开（需要先复制到Windows）
# 报告位置: backend/target/site/jacoco/index.html

# 或使用命令行查看摘要
cat backend/target/surefire-reports/*.txt
```

## 🔧 环境检查

### 检查Docker是否可用

```bash
docker --version
docker ps
```

### 检查是否需要sudo

```bash
# 如果需要sudo，将当前用户加入docker组
sudo usermod -aG docker $USER
# 然后重新登录
```

### 检查磁盘空间

```bash
df -h
docker system df
```

## 💡 最佳实践

1. **开发时**：在宿主机上快速运行测试
   ```bash
   cd backend && mvn test -Dtest=MyNewTest
   ```

2. **提交前**：使用Docker容器完整测试
   ```bash
   ./run-test-docker.sh --coverage
   ```

3. **CI/CD**：使用Docker Compose自动化测试
   ```yaml
   # GitHub Actions
   - name: Run Tests
     run: ./run-test-docker.sh --coverage
   ```

## 📞 需要帮助？

如果遇到问题：

1. 查看Docker日志
   ```bash
   docker logs xu-news-test-runner
   ```

2. 检查端口占用
   ```bash
   netstat -tunlp | grep 8080
   ```

3. 清理Docker资源
   ```bash
   docker system prune -a
   docker volume prune
   ```

---

**更新时间**: 2025-10-16  
**适用环境**: Linux (VMware Docker)

