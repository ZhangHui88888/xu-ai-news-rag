# XU-News-AI-RAG 完整测试指南

## 概述

本项目提供了完整的测试套件，包括后端Java测试和前端Vue测试。

## 项目测试覆盖

### 后端测试（Java + Spring Boot）

#### 测试类型
1. **单元测试** - 测试单个类/方法
2. **API测试** - 测试REST接口
3. **集成测试** - 测试完整业务流程
4. **Mapper测试** - 测试数据库操作

#### 测试覆盖范围

| 层次 | 测试文件 | 覆盖内容 |
|------|---------|---------|
| Controller | 3个测试类 | AuthController, KnowledgeController, QueryController |
| Service | 3个测试类 | UserService, KnowledgeEntryService, QueryService |
| Mapper | 4个测试类 | UserMapper, KnowledgeEntryMapper, DataSourceMapper, TagMapper |
| Util | 3个测试类 | JwtUtil, FileProcessor, VectorStore |
| Integration | 3个测试类 | 用户认证流程、知识库管理流程、RAG查询流程 |

#### 测试统计
- **测试类总数**: 16个
- **测试方法总数**: 150+个
- **预期覆盖率**: ≥80%

### 前端测试（Vue 3 + Vitest）

#### 测试类型
1. **单元测试** - 测试函数和模块
2. **Store测试** - 测试状态管理
3. **API测试** - 测试API调用
4. **组件测试** - 测试Vue组件
5. **E2E测试** - 端到端测试（示例）

#### 测试覆盖范围

| 类型 | 测试文件 | 覆盖内容 |
|------|---------|---------|
| Store | 1个测试文件 | User Store |
| API | 3个测试文件 | auth, knowledge, query API |
| E2E | 1个测试文件 | 用户认证流程（示例） |

## 快速开始

### 后端测试

```bash
cd backend

# 运行所有测试
mvn test

# 运行并生成覆盖率报告
mvn test jacoco:report

# 查看覆盖率报告
open target/site/jacoco/index.html

# 运行特定测试
mvn test -Dtest=UserServiceTest
```

### 前端测试

```bash
cd frontend

# 安装测试依赖
npm install -D vitest @vue/test-utils jsdom @vitest/ui

# 更新package.json添加测试脚本
# 参考: frontend/tests/README.md

# 运行测试
npm test

# 运行测试并生成覆盖率
npm run test:coverage

# 使用UI运行测试
npm run test:ui
```

## 测试环境配置

### 后端测试环境

**配置文件**: `backend/src/test/resources/application-test.yml`

- 使用H2内存数据库
- 自动初始化测试数据
- Mock外部服务（Ollama、向量存储等）

**关键配置**:
```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb;MODE=MySQL
    
jwt:
  secret: TEST_SECRET_KEY...
  expiration: 3600000
```

### 前端测试环境

**配置文件**: `frontend/vitest.config.js`

- 使用jsdom模拟浏览器环境
- Mock localStorage和API请求
- 支持Vue组件测试

## 测试架构

### 后端测试架构

```
测试基类
├── BaseTest (所有测试的基类)
└── BaseControllerTest (Controller测试基类)

测试工具类
├── MockDataGenerator (生成Mock数据)
├── TestDataBuilder (构建测试数据)
└── AssertionUtils (自定义断言)

测试层次
├── Controller层 (API测试)
│   └── 使用MockMvc测试HTTP请求
├── Service层 (单元测试)
│   └── 使用Mockito模拟依赖
├── Mapper层 (数据库测试)
│   └── 使用H2内存数据库
└── Integration (集成测试)
    └── 测试完整业务流程
```

### 前端测试架构

```
测试配置
├── vitest.config.js (Vitest配置)
└── tests/setup.js (测试环境设置)

测试类型
├── Unit Tests (单元测试)
│   ├── API测试
│   ├── Store测试
│   └── Utils测试
├── Component Tests (组件测试)
│   └── Vue组件测试
└── E2E Tests (端到端测试)
    └── 完整用户流程测试
```

## 测试示例

### 后端测试示例

#### 1. Service单元测试
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    @DisplayName("用户登录 - 成功")
    void testLogin_Success() {
        // Given
        when(userMapper.findByUsername(anyString())).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        
        // When
        String token = userService.login(loginRequest);
        
        // Then
        assertNotNull(token);
    }
}
```

#### 2. Controller API测试
```java
@DisplayName("认证控制器API测试")
class AuthControllerTest extends BaseControllerTest {
    @Test
    void testRegister_Success() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(registerRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}
```

#### 3. 集成测试
```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserAuthIntegrationTest {
    @Test
    void testCompleteAuthFlow() throws Exception {
        // 1. 注册
        mockMvc.perform(post("/auth/register")...)
            .andExpect(status().isOk());
        
        // 2. 登录
        mockMvc.perform(post("/auth/login")...)
            .andExpect(status().isOk());
    }
}
```

### 前端测试示例

#### 1. Store测试
```javascript
import { useUserStore } from '@/stores/user'

describe('User Store', () => {
  it('should set token', () => {
    const store = useUserStore()
    store.setToken('test-token')
    expect(store.token).toBe('test-token')
    expect(store.isLoggedIn).toBe(true)
  })
})
```

#### 2. API测试
```javascript
import { login } from '@/api/auth'
import request from '@/api/request'

vi.mock('@/api/request')

describe('Auth API', () => {
  it('should login successfully', async () => {
    request.post.mockResolvedValue({ code: 200, data: { token: 'xxx' } })
    const result = await login({ username: 'test', password: 'test' })
    expect(result.code).toBe(200)
  })
})
```

## 测试最佳实践

### 1. 测试命名规范

- 使用描述性的名称
- 遵循`test[MethodName]_[Scenario]`格式
- 使用`@DisplayName`注解（Java）

```java
@Test
@DisplayName("用户登录 - 密码错误")
void testLogin_WrongPassword() { }
```

### 2. AAA模式

```java
@Test
void testExample() {
    // Arrange - 准备测试数据
    User user = new User();
    
    // Act - 执行测试操作
    User result = service.save(user);
    
    // Assert - 验证结果
    assertNotNull(result);
}
```

### 3. 测试隔离

- 每个测试独立运行
- 使用`@Transactional`自动回滚
- 清理共享状态

### 4. Mock外部依赖

- Mock数据库（使用H2）
- Mock外部API（Ollama、向量服务）
- Mock文件系统

### 5. 测试数据管理

- 使用`TestDataBuilder`构建测试数据
- 使用`MockDataGenerator`生成随机数据
- 避免硬编码测试数据

## 测试覆盖率目标

| 层次 | 目标覆盖率 |
|------|-----------|
| 整体项目 | ≥ 80% |
| Service层 | ≥ 90% |
| Controller层 | ≥ 85% |
| Mapper层 | ≥ 80% |
| Util工具类 | ≥ 85% |

## CI/CD集成

### GitHub Actions配置

```yaml
name: Tests
on: [push, pull_request]
jobs:
  backend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
      - run: cd backend && mvn test
      
  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-node@v2
      - run: cd frontend && npm ci && npm test
```

## 常见问题

### 后端测试问题

**Q: H2数据库初始化失败**
A: 检查`schema.sql`和`data.sql`文件，确保SQL语法兼容H2。

**Q: Mock不生效**
A: 确保使用正确的Mock注解，参数匹配正确。

**Q: 测试数据污染**
A: 使用`@Transactional`注解自动回滚。

### 前端测试问题

**Q: localStorage未定义**
A: 在`tests/setup.js`中已经Mock了localStorage。

**Q: 组件测试失败**
A: 确保正确Mock Router和Store。

**Q: API测试网络错误**
A: 使用`vi.mock()`模拟request模块。

## 测试文档

- [后端测试详细指南](backend/TEST_GUIDE.md)
- [前端测试详细指南](frontend/tests/README.md)

## 相关资源

### 后端
- [JUnit 5](https://junit.org/junit5/)
- [Mockito](https://site.mockito.org/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)

### 前端
- [Vitest](https://vitest.dev/)
- [Vue Test Utils](https://test-utils.vuejs.org/)
- [Testing Library](https://testing-library.com/)

## 贡献测试

欢迎为项目添加更多测试！请遵循以下准则：

1. 为新功能添加对应的测试
2. 确保测试通过且覆盖率达标
3. 遵循测试命名规范
4. 添加必要的注释和文档

## 总结

本项目提供了完整的测试套件，包括：

✅ **后端测试**
- 16个测试类
- 150+个测试方法
- 覆盖Controller、Service、Mapper、Util、Integration

✅ **前端测试**
- Store测试
- API测试
- 组件测试框架
- E2E测试示例

✅ **测试工具**
- 测试基类
- Mock数据生成器
- 测试数据构建器
- 自定义断言工具

✅ **测试文档**
- 完整的测试指南
- 示例代码
- 最佳实践
- 常见问题解答

开始测试您的代码吧！🚀

