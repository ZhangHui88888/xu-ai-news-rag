# 前端测试文档

## 测试框架

本项目使用以下测试框架：

- **Vitest**: 单元测试和组件测试
- **@vue/test-utils**: Vue组件测试工具
- **jsdom**: 模拟浏览器环境

## 测试结构

```
tests/
├── setup.js                 # 测试环境设置
├── unit/                    # 单元测试
│   ├── api/                # API测试
│   │   ├── auth.test.js
│   │   ├── knowledge.test.js
│   │   └── query.test.js
│   ├── stores/             # Store测试
│   │   └── user.test.js
│   └── components/         # 组件测试
└── e2e/                    # E2E测试（示例）
    └── auth.spec.js
```

## 运行测试

### 安装依赖

首先需要安装测试相关依赖：

```bash
npm install -D vitest @vue/test-utils jsdom @vitest/ui
```

### 更新 package.json

在 `package.json` 中添加测试脚本：

```json
{
  "scripts": {
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:run": "vitest run",
    "test:coverage": "vitest run --coverage"
  }
}
```

### 执行测试

```bash
# 运行所有测试（监视模式）
npm test

# 运行一次性测试
npm run test:run

# 运行测试并生成覆盖率报告
npm run test:coverage

# 使用UI界面运行测试
npm run test:ui
```

## 测试类型

### 1. 单元测试

测试独立的函数、工具类和模块。

**示例：API测试**
```javascript
import { describe, it, expect, vi } from 'vitest'
import { login } from '@/api/auth'

describe('Auth API', () => {
  it('should login successfully', async () => {
    const result = await login({ username: 'test', password: 'test' })
    expect(result.code).toBe(200)
  })
})
```

### 2. Store 测试

测试 Pinia store 的状态管理逻辑。

**示例：User Store测试**
```javascript
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should set token', () => {
    const store = useUserStore()
    store.setToken('test-token')
    expect(store.token).toBe('test-token')
  })
})
```

### 3. 组件测试

测试 Vue 组件的渲染和交互。

**示例：组件测试**
```javascript
import { mount } from '@vue/test-utils'
import LoginForm from '@/components/LoginForm.vue'

describe('LoginForm', () => {
  it('should render login form', () => {
    const wrapper = mount(LoginForm)
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
  })
})
```

### 4. E2E 测试

端到端测试需要配置额外的测试框架（如 Playwright 或 Cypress）。

## 测试覆盖率

运行测试覆盖率命令：

```bash
npm run test:coverage
```

覆盖率报告将生成在 `coverage/` 目录下。

## 最佳实践

1. **测试命名**: 使用描述性的测试名称，清楚说明测试目的
2. **AAA模式**: Arrange（准备）、Act（执行）、Assert（断言）
3. **隔离性**: 每个测试应该独立运行，不依赖其他测试
4. **Mock外部依赖**: 使用 `vi.mock()` 模拟外部API和服务
5. **覆盖率目标**: 核心业务逻辑至少80%覆盖率

## 常见问题

### 1. localStorage 未定义

在 `tests/setup.js` 中已经Mock了localStorage。

### 2. 组件测试中路由问题

需要Mock Vue Router：

```javascript
import { createRouter, createMemoryHistory } from 'vue-router'

const router = createRouter({
  history: createMemoryHistory(),
  routes: []
})

const wrapper = mount(Component, {
  global: {
    plugins: [router]
  }
})
```

### 3. API测试中的网络请求

使用 `vi.mock()` 模拟request模块，避免实际的网络请求。

## 持续集成

可以将测试集成到CI/CD流程中：

```yaml
# .github/workflows/test.yml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-node@v2
      - run: npm ci
      - run: npm run test:run
      - run: npm run test:coverage
```

## 参考文档

- [Vitest 文档](https://vitest.dev/)
- [Vue Test Utils 文档](https://test-utils.vuejs.org/)
- [Pinia 测试文档](https://pinia.vuejs.org/cookbook/testing.html)

