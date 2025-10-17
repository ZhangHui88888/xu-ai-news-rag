# 首页统计数据修复说明

## 🐛 问题描述

首页显示的三个统计数据（知识条目、查询次数、用户数量）是硬编码的假数据，不是从数据库实时获取的。

### 问题代码
```javascript
// Home.vue (修复前)
onMounted(() => {
  // TODO: 从API获取统计数据
  stats.value = {
    knowledgeCount: 128,  // 假数据
    queryCount: 456,       // 假数据
    userCount: 12          // 假数据
  }
})
```

## ✅ 解决方案

创建了真实的统计API接口，从数据库实时获取准确的统计数据。

## 📝 实现内容

### 1. 后端 - 统计控制器

创建了 `StatisticsController.java`，提供两个接口：

#### 基础统计接口
```
GET /api/statistics/overview
```

**返回数据：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "knowledgeCount": 37,  // 实时统计知识条目数
    "queryCount": 156,      // 实时统计查询次数
    "userCount": 5          // 实时统计用户数量
  }
}
```

#### 详细统计接口
```
GET /api/statistics/detailed
```

**返回数据：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "knowledgeCount": 37,
    "queryCount": 156,
    "userCount": 5,
    "knowledgeByType": {
      "news": 25,
      "article": 5,
      "document": 4,
      "test": 2,
      "other": 1
    }
  }
}
```

### 2. 前端 - API调用

创建了 `statistics.js` API文件：

```javascript
import request from './request'

export function getStatistics() {
  return request({
    url: '/statistics/overview',
    method: 'get'
  })
}

export function getDetailedStatistics() {
  return request({
    url: '/statistics/detailed',
    method: 'get'
  })
}
```

### 3. 前端 - 首页更新

更新了 `Home.vue`，使用真实API：

```javascript
const loadStatistics = async () => {
  loading.value = true
  try {
    const res = await getStatistics()
    if (res && res.code === 200 && res.data) {
      stats.value = {
        knowledgeCount: res.data.knowledgeCount || 0,
        queryCount: res.data.queryCount || 0,
        userCount: res.data.userCount || 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}
```

## 📂 修改的文件

```
backend/
└── src/main/java/com/xu/news/controller/
    └── StatisticsController.java  (新增)

frontend/
├── src/api/
│   └── statistics.js  (新增)
└── src/views/
    └── Home.vue  (修改)
```

## 🎯 统计数据说明

### 知识条目数
- **统计范围**：所有未删除的知识条目
- **SQL条件**：`deleted = 0`
- **表**：`knowledge_entry`

### 查询次数
- **统计范围**：所有查询历史记录
- **表**：`user_query_history`
- **包含**：所有用户的所有查询记录

### 用户数量
- **统计范围**：所有未删除的用户
- **SQL条件**：`deleted = 0`
- **表**：`user`

## 🚀 部署步骤

### 在虚拟机上执行：

```bash
# 1. 进入项目目录
cd /path/to/xu-ai-news-rag

# 2. 重启后端服务
docker-compose up -d --build backend

# 3. 查看日志
docker-compose logs -f backend
```

前端会自动热重载，无需重启。

## ✨ 新功能

### 1. 实时数据
- 每次刷新页面都会获取最新的统计数据
- 数据来源于真实的数据库查询

### 2. 加载状态
- 添加了加载动画（v-loading）
- 提升用户体验

### 3. 错误处理
- 如果API调用失败，会显示错误提示
- 统计数字会显示为0（而不是之前的假数据）

## 📊 效果对比

### 修复前
| 统计项 | 显示值 | 实际情况 |
|--------|--------|---------|
| 知识条目 | 128 | 固定假数据 |
| 查询次数 | 456 | 固定假数据 |
| 用户数量 | 12 | 固定假数据 |

### 修复后
| 统计项 | 显示值 | 实际情况 |
|--------|--------|---------|
| 知识条目 | 37 | 实时数据库查询 |
| 查询次数 | 156 | 实时数据库查询 |
| 用户数量 | 5 | 实时数据库查询 |

## 🔮 后续扩展建议

可以基于详细统计接口添加更多功能：

1. **知识类型分布图表**
   - 饼图显示各类型知识条目的占比
   - 使用 `knowledgeByType` 数据

2. **趋势统计**
   - 添加按日期统计的功能
   - 显示知识增长趋势图

3. **热门统计**
   - 最受欢迎的知识条目（按浏览量）
   - 最活跃的用户

4. **数据刷新**
   - 添加手动刷新按钮
   - 或设置定时自动刷新

## ⚠️ 注意事项

1. **性能考虑**：当数据量很大时，统计查询可能会较慢，可以考虑使用缓存
2. **权限控制**：当前统计接口无需认证，如需要可以添加权限验证
3. **数据一致性**：统计数据基于逻辑删除（`deleted = 0`），确保数据准确性

## 🎉 验证方法

1. 重启Docker服务后，访问首页
2. 查看控制台网络请求，应该能看到 `/api/statistics/overview` 请求
3. 统计数字应该与数据库实际数据一致
4. 可以添加/删除知识条目，刷新页面验证数字变化

