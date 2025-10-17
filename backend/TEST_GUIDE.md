# 后端测试指南

## 测试框架

本项目使用以下测试框架和工具：

- **JUnit 5**: 单元测试框架
- **Mockito**: Mock框架
- **Spring Boot Test**: Spring Boot测试支持
- **MockMvc**: Web层测试
- **H2 Database**: 内存数据库（测试环境）

## 测试结构

```
src/test/java/com/xu/news/
├── base/                           # 测试基类
│   ├── BaseTest.java              # 所有测试的基类
│   └── BaseControllerTest.java    # Controller测试基类
├── controller/                     # Controller API测试
│   ├── AuthControllerTest.java
│   ├── KnowledgeControllerTest.java
│   └── QueryControllerTest.java
├── service/                        # Service单元测试
│   ├── UserServiceTest.java
│   ├── KnowledgeEntryServiceTest.java
│   └── QueryServiceTest.java
├── mapper/                         # Mapper测试
│   ├── UserMapperTest.java
│   ├── KnowledgeEntryMapperTest.java
│   ├── DataSourceMapperTest.java
│   └── TagMapperTest.java
├── util/                          # 工具类测试
│   ├── JwtUtilTest.java
│   ├── FileProcessorTest.java
│   └── VectorStoreTest.java
├── integration/                   # 集成测试
│   ├── UserAuthIntegrationTest.java
│   ├── KnowledgeManagementIntegrationTest.java
│   └── RAGQueryIntegrationTest.java
└── utils/                         # 测试工具类
    ├── MockDataGenerator.java
    ├── TestDataBuilder.java
    └── AssertionUtils.java
```

## 运行测试

### 1. 运行所有测试

```bash
# 使用Maven
mvn test

# 使用Maven（跳过测试）
mvn clean install -DskipTests

# 运行测试并生成报告
mvn test jacoco:report
```

### 2. 运行特定测试类

```bash
# 运行单个测试类
mvn test -Dtest=UserServiceTest

# 运行特定测试方法
mvn test -Dtest=UserServiceTest#testLogin_Success
```

### 3. 运行特定类型的测试

```bash
# 仅运行单元测试
mvn test -Dgroups=unit

# 仅运行集成测试
mvn test -Dgroups=integration

# 仅运行API测试
mvn test -Dgroups=api
```

### 4. 使用IDE运行测试

#### IntelliJ IDEA
- 右键点击测试类/方法 → Run 'TestName'
- 使用快捷键：Ctrl+Shift+F10 (Windows/Linux) 或 Cmd+Shift+R (Mac)

#### Eclipse
- 右键点击测试类/方法 → Run As → JUnit Test

## 测试类型

### 1. 单元测试（Unit Tests）

测试单个类或方法的功能，使用Mock隔离依赖。

**特点：**
- 快速执行
- 隔离性强
- 使用Mockito模拟依赖

**示例：**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    void testLogin_Success() {
        // Given
        when(userMapper.findByUsername(anyString())).thenReturn(testUser);
        
        // When
        String token = userService.login(loginRequest);
        
        // Then
        assertNotNull(token);
    }
}
```

### 2. API测试（Controller Tests）

测试REST API的HTTP请求和响应。

**特点：**
- 使用MockMvc
- 测试完整的HTTP交互
- 验证JSON响应

**示例：**
```java
@DisplayName("认证控制器API测试")
class AuthControllerTest extends BaseControllerTest {
    @Test
    void testLogin_Success() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}
```

### 3. 集成测试（Integration Tests）

测试多个组件协同工作的完整流程。

**特点：**
- 使用真实的Spring上下文
- 使用H2内存数据库
- 测试端到端流程

**示例：**
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class UserAuthIntegrationTest {
    @Test
    void testCompleteAuthFlow_Success() throws Exception {
        // 1. 注册
        mockMvc.perform(post("/auth/register")...)
            .andExpect(status().isOk());
        
        // 2. 登录
        mockMvc.perform(post("/auth/login")...)
            .andExpect(status().isOk());
    }
}
```

### 4. Mapper测试

测试MyBatis映射器的数据库操作。

**特点：**
- 使用H2内存数据库
- 测试SQL查询
- 验证数据一致性

**示例：**
```java
@Transactional
class UserMapperTest extends BaseTest {
    @Autowired
    private UserMapper userMapper;
    
    @Test
    void testInsert_Success() {
        int result = userMapper.insert(testUser);
        assertEquals(1, result);
    }
}
```

## 测试配置

### 1. 测试配置文件

`src/test/resources/application-test.yml`

```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb
    
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
      data-locations: classpath:data.sql

jwt:
  secret: TEST_SECRET_KEY...
  expiration: 3600000
```

### 2. 测试数据

- `src/test/resources/schema.sql`: 数据库表结构
- `src/test/resources/data.sql`: 测试数据

## 测试覆盖率

### 生成覆盖率报告

```bash
# 运行测试并生成覆盖率报告
mvn clean test jacoco:report

# 查看报告
open target/site/jacoco/index.html
```

### 覆盖率目标

- **整体覆盖率**: ≥ 80%
- **核心业务逻辑**: ≥ 90%
- **Controller层**: ≥ 85%
- **Service层**: ≥ 90%
- **Mapper层**: ≥ 80%

## 最佳实践

### 1. 测试命名

使用清晰的测试方法名：

```java
@Test
@DisplayName("用户登录 - 成功")
void testLogin_Success() { }

@Test
@DisplayName("用户登录 - 密码错误")
void testLogin_WrongPassword() { }
```

### 2. AAA模式

```java
@Test
void testExample() {
    // Arrange (准备)
    User user = new User();
    when(mapper.findById(1L)).thenReturn(user);
    
    // Act (执行)
    User result = service.getById(1L);
    
    // Assert (断言)
    assertNotNull(result);
    assertEquals(user.getId(), result.getId());
}
```

### 3. 使用@BeforeEach

```java
private User testUser;

@BeforeEach
void setUp() {
    testUser = TestDataBuilder.createTestUser();
}
```

### 4. 测试隔离

每个测试应该独立，不依赖其他测试：

```java
@Transactional  // 自动回滚数据库操作
@Test
void testInsert() {
    // 测试结束后自动回滚
}
```

### 5. Mock外部依赖

```java
@Mock
private OllamaClient ollamaClient;

@Test
void testQuery() {
    when(ollamaClient.generateText(anyString()))
        .thenReturn("Mock response");
}
```

## 常见问题

### 1. 测试时数据库连接失败

**解决方案：** 确保使用H2内存数据库，检查`application-test.yml`配置。

### 2. MockMvc返回404

**解决方案：** 检查Controller路径，确保`@SpringBootTest`正确加载。

### 3. 测试数据污染

**解决方案：** 使用`@Transactional`注解，每个测试后自动回滚。

### 4. Mock不生效

**解决方案：** 
- 确保使用`@ExtendWith(MockitoExtension.class)`
- 检查Mock对象是否正确注入
- 验证`when().thenReturn()`的参数匹配

## CI/CD集成

### GitHub Actions示例

```yaml
name: Backend Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run tests
        run: mvn test
      - name: Generate coverage report
        run: mvn jacoco:report
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v2
```

## 调试测试

### 1. 打印日志

```java
@Test
void testExample() {
    log.info("Testing with user: {}", testUser);
    // ...
}
```

### 2. 使用断点

在IDE中设置断点，以Debug模式运行测试。

### 3. 查看SQL

在`application-test.yml`中启用SQL日志：

```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

## 参考资源

- [JUnit 5 文档](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito 文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [AssertJ 文档](https://assertj.github.io/doc/)

