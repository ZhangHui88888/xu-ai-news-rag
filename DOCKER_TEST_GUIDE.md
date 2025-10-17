# Docker环境测试指南

## 概述

本指南介绍如何在Docker环境中运行XU-News-AI-RAG项目的测试。

---

## Docker环境架构

```
Docker环境
├── MySQL容器 (数据库)
├── Backend容器 (Spring Boot)
└── Frontend容器 (Vue 3)

测试环境
├── H2内存数据库 (后端测试)
└── Mock API (前端测试)
```

---

## 方式一：在宿主机运行测试（推荐）

### 后端测试

```bash
# 进入后端目录
cd backend

# 运行测试（使用H2内存数据库，不依赖Docker）
mvn test

# 生成覆盖率报告
mvn test jacoco:report

# 查看报告
# Windows: start target/site/jacoco/index.html
# Linux: xdg-open target/site/jacoco/index.html
```

**说明**: 后端测试使用H2内存数据库，不需要MySQL容器运行。

### 前端测试

```bash
# 进入前端目录
cd frontend

# 安装测试依赖
npm install -D vitest @vue/test-utils jsdom @vitest/ui

# 运行测试
npm test

# 生成覆盖率
npm run test:coverage
```

---

## 方式二：在Docker容器内运行测试

### 1. 创建测试专用Docker配置

创建 `docker-compose.test.yml`:

```yaml
version: '3.8'

services:
  backend-test:
    build:
      context: ./backend
      dockerfile: Dockerfile
    volumes:
      - ./backend:/app
    working_dir: /app
    command: mvn test
    environment:
      - SPRING_PROFILES_ACTIVE=test
    networks:
      - test-network

  frontend-test:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    volumes:
      - ./frontend:/app
      - /app/node_modules
    working_dir: /app
    command: npm test -- --run
    networks:
      - test-network

networks:
  test-network:
    driver: bridge
```

### 2. 运行容器化测试

```bash
# 运行后端测试
docker-compose -f docker-compose.test.yml run --rm backend-test

# 运行前端测试
docker-compose -f docker-compose.test.yml run --rm frontend-test

# 清理测试容器
docker-compose -f docker-compose.test.yml down
```

---

## 方式三：进入运行中的容器执行测试

### 后端容器

```bash
# 查看运行中的容器
docker ps

# 进入后端容器
docker exec -it <backend-container-id> /bin/bash

# 在容器内运行测试
mvn test

# 退出容器
exit
```

### 前端容器

```bash
# 进入前端容器
docker exec -it <frontend-container-id> /bin/sh

# 安装测试依赖
npm install -D vitest @vue/test-utils jsdom

# 运行测试
npm test

# 退出容器
exit
```

---

## 方式四：创建测试专用Dockerfile

### 后端测试Dockerfile

创建 `backend/Dockerfile.test`:

```dockerfile
FROM maven:3.8.6-openjdk-17-slim

WORKDIR /app

# 复制pom.xml和源代码
COPY pom.xml .
COPY src ./src

# 运行测试
CMD ["mvn", "clean", "test", "jacoco:report"]
```

使用方式:

```bash
# 构建测试镜像
docker build -f backend/Dockerfile.test -t xu-news-backend-test ./backend

# 运行测试
docker run --rm xu-news-backend-test

# 带卷挂载运行（保留报告）
docker run --rm -v $(pwd)/backend/target:/app/target xu-news-backend-test
```

### 前端测试Dockerfile

创建 `frontend/Dockerfile.test`:

```dockerfile
FROM node:18-alpine

WORKDIR /app

# 复制package.json
COPY package*.json ./

# 安装依赖
RUN npm ci

# 安装测试依赖
RUN npm install -D vitest @vue/test-utils jsdom

# 复制源代码和测试文件
COPY . .

# 运行测试
CMD ["npm", "test", "--", "--run"]
```

使用方式:

```bash
# 构建测试镜像
docker build -f frontend/Dockerfile.test -t xu-news-frontend-test ./frontend

# 运行测试
docker run --rm xu-news-frontend-test
```

---

## Docker环境下的测试配置

### 后端测试配置

`backend/src/test/resources/application-test.yml`:

```yaml
# 使用H2内存数据库，不依赖MySQL容器
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb;MODE=MySQL;DATABASE_TO_LOWER=TRUE
    username: sa
    password:

# 禁用不需要的服务
ollama:
  base-url: http://mock-ollama:11434  # Mock地址

faiss:
  index-path: /tmp/faiss-test  # 临时目录
```

### 前端测试配置

确保 `frontend/vitest.config.js` 正确配置:

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./tests/setup.js']
  }
})
```

---

## 完整测试脚本（Docker环境）

创建 `docker-test.sh`:

```bash
#!/bin/bash

echo "======================================"
echo "Docker环境测试执行"
echo "======================================"

# 后端测试
echo "1. 运行后端测试..."
cd backend
mvn clean test
if [ $? -eq 0 ]; then
    echo "✓ 后端测试通过"
    mvn jacoco:report
    echo "覆盖率报告: backend/target/site/jacoco/index.html"
else
    echo "✗ 后端测试失败"
    exit 1
fi

cd ..

# 前端测试
echo ""
echo "2. 运行前端测试..."
cd frontend

# 检查是否安装测试依赖
if [ ! -d "node_modules/vitest" ]; then
    echo "安装测试依赖..."
    npm install -D vitest @vue/test-utils jsdom
fi

npm test -- --run
if [ $? -eq 0 ]; then
    echo "✓ 前端测试通过"
else
    echo "✗ 前端测试失败"
fi

cd ..

echo ""
echo "======================================"
echo "测试执行完成"
echo "======================================"
```

使用方式:

```bash
chmod +x docker-test.sh
./docker-test.sh
```

---

## 使用docker-compose运行完整测试

创建 `docker-compose.test.yml`:

```yaml
version: '3.8'

services:
  # 后端测试服务
  backend-test:
    image: maven:3.8.6-openjdk-17-slim
    volumes:
      - ./backend:/app
      - ~/.m2:/root/.m2  # 缓存Maven依赖
    working_dir: /app
    command: >
      sh -c "
        echo '运行后端测试...' &&
        mvn clean test &&
        echo '生成覆盖率报告...' &&
        mvn jacoco:report &&
        echo '后端测试完成！'
      "
    environment:
      - SPRING_PROFILES_ACTIVE=test

  # 前端测试服务
  frontend-test:
    image: node:18-alpine
    volumes:
      - ./frontend:/app
    working_dir: /app
    command: >
      sh -c "
        echo '安装依赖...' &&
        npm ci &&
        echo '安装测试依赖...' &&
        npm install -D vitest @vue/test-utils jsdom &&
        echo '运行前端测试...' &&
        npm test -- --run &&
        echo '前端测试完成！'
      "
```

运行方式:

```bash
# 运行所有测试
docker-compose -f docker-compose.test.yml up --abort-on-container-exit

# 仅运行后端测试
docker-compose -f docker-compose.test.yml run --rm backend-test

# 仅运行前端测试
docker-compose -f docker-compose.test.yml run --rm frontend-test

# 清理
docker-compose -f docker-compose.test.yml down
```

---

## CI/CD集成（Docker环境）

### GitHub Actions示例

```yaml
name: Docker Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v1
      
      - name: Run Backend Tests
        run: |
          docker run --rm \
            -v $PWD/backend:/app \
            -v ~/.m2:/root/.m2 \
            -w /app \
            maven:3.8.6-openjdk-17-slim \
            mvn clean test
      
      - name: Run Frontend Tests
        run: |
          docker run --rm \
            -v $PWD/frontend:/app \
            -w /app \
            node:18-alpine \
            sh -c "npm ci && npm install -D vitest @vue/test-utils jsdom && npm test -- --run"
      
      - name: Upload Coverage Reports
        uses: codecov/codecov-action@v2
```

---

## 常见问题

### Q1: Docker容器内Maven下载依赖很慢？

**解决方案**: 挂载本地Maven仓库

```bash
docker run -v ~/.m2:/root/.m2 -v $(pwd)/backend:/app maven:3.8 mvn test
```

### Q2: 测试报告无法访问？

**解决方案**: 使用卷挂载保存报告到宿主机

```bash
docker run -v $(pwd)/backend/target:/app/target maven:3.8 mvn test
```

### Q3: 前端测试找不到node_modules？

**解决方案**: 
1. 在容器内重新安装依赖
2. 或使用匿名卷: `-v /app/node_modules`

### Q4: H2数据库初始化失败？

**解决方案**: 确保`schema.sql`和`data.sql`兼容H2语法

### Q5: 测试超时？

**解决方案**: 增加测试超时时间

```yaml
# pom.xml
<configuration>
  <forkedProcessTimeoutInSeconds>600</forkedProcessTimeoutInSeconds>
</configuration>
```

---

## 最佳实践

### 1. 测试隔离

✅ 后端测试使用H2内存数据库，不依赖MySQL容器
✅ 每个测试使用`@Transactional`自动回滚
✅ Mock外部服务（Ollama、向量存储）

### 2. 快速反馈

✅ 在宿主机运行测试（方式一，推荐）
✅ 使用Maven/npm缓存加速依赖下载
✅ 并行运行测试

### 3. 持续集成

✅ 在CI/CD中使用Docker运行测试
✅ 生成并上传测试报告
✅ 监控测试覆盖率

### 4. 开发流程

```bash
# 开发时: 在宿主机快速测试
mvn test -Dtest=SpecificTest

# 提交前: 运行完整测试
./docker-test.sh

# CI/CD: 使用Docker确保环境一致
docker-compose -f docker-compose.test.yml up
```

---

## 推荐的测试工作流

### 本地开发

```bash
# 1. 修改代码
# 2. 在IDE中运行相关测试
# 3. 或使用Maven/npm运行单个测试类
cd backend
mvn test -Dtest=UserServiceTest
```

### 提交前检查

```bash
# 运行所有测试
./docker-test.sh

# 或使用宿主机命令
cd backend && mvn test && cd ..
cd frontend && npm test -- --run && cd ..
```

### Docker环境验证

```bash
# 使用docker-compose验证
docker-compose -f docker-compose.test.yml up --abort-on-container-exit
```

---

## 总结

✅ **推荐方式**: 在宿主机运行测试（快速、简单）
✅ **Docker方式**: 用于CI/CD和环境一致性验证
✅ **测试隔离**: 使用H2内存数据库，不依赖外部服务
✅ **完整覆盖**: 单元测试、集成测试、API测试全覆盖

---

**相关文档**:
- [完整测试指南](TESTING_COMPLETE_GUIDE.md)
- [后端测试指南](backend/TEST_GUIDE.md)
- [前端测试指南](frontend/tests/README.md)
- [测试总结](TESTS_SUMMARY.md)

