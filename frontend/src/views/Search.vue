<template>
  <div class="search-container">
    <el-card class="search-card">
      <el-input
        v-model="keyword"
        placeholder="搜索知识库..."
        size="large"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            搜索
          </el-button>
        </template>
      </el-input>
    </el-card>

    <el-card v-if="results.length > 0" class="results-card">
      <h3>搜索结果 ({{ total }})</h3>
      <el-space direction="vertical" fill style="width: 100%">
        <el-card
          v-for="item in results"
          :key="item.id"
          class="result-item"
          shadow="hover"
        >
          <h4>{{ item.title }}</h4>
          <p class="content">{{ item.summary || item.content }}</p>
          <div class="meta">
            <el-tag size="small">{{ item.contentType }}</el-tag>
            <span class="date">{{ formatDate(item.publishedAt) }}</span>
            <span class="source">{{ item.sourceName }}</span>
          </div>
        </el-card>
      </el-space>

      <el-pagination
        v-if="total > pageSize"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
        style="margin-top: 20px; text-align: center"
      />
    </el-card>

    <el-empty
      v-else-if="searched"
      description="没有找到相关结果"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const keyword = ref('')
const loading = ref(false)
const searched = ref(false)
const results = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const handleSearch = async () => {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  loading.value = true
  searched.value = true

  try {
    // TODO: 调用搜索API
    // const res = await searchKnowledge({ keyword: keyword.value, page: currentPage.value, size: pageSize.value })
    
    // 模拟数据
    setTimeout(() => {
      results.value = [
        {
          id: 1,
          title: '人工智能最新进展',
          summary: 'AI技术在各个领域都取得了突破性进展...',
          contentType: '新闻',
          publishedAt: '2025-10-15',
          sourceName: '科技日报'
        },
        {
          id: 2,
          title: '机器学习算法优化',
          summary: '新的优化算法提升了模型训练效率...',
          contentType: '文章',
          publishedAt: '2025-10-14',
          sourceName: '技术博客'
        }
      ]
      total.value = 2
      loading.value = false
    }, 1000)
  } catch (error) {
    ElMessage.error('搜索失败')
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  handleSearch()
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.search-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.results-card h3 {
  margin-top: 0;
  color: #333;
}

.result-item {
  cursor: pointer;
  transition: transform 0.2s;
}

.result-item:hover {
  transform: translateX(5px);
}

.result-item h4 {
  margin: 0 0 10px;
  color: #409eff;
  font-size: 18px;
}

.result-item .content {
  margin: 0 0 15px;
  color: #666;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-item .meta {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 12px;
  color: #999;
}

.result-item .date,
.result-item .source {
  color: #999;
}
</style>

