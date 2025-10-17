<template>
  <div class="search-container">
    <el-card class="search-card">
      <el-input
        v-model="keyword"
        placeholder="智能语义搜索（支持向量检索）..."
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
          @click="viewDetail(item)"
        >
          <h4>{{ item.title }}</h4>
          <p class="content">{{ item.summary || item.content }}</p>
          <div class="meta">
            <el-tag size="small">{{ getTypeLabel(item.contentType) }}</el-tag>
            <span class="date">{{ formatDateTime(item.createdAt) }}</span>
            <span class="source">{{ item.sourceName || '-' }}</span>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="文档详情" width="800px">
      <div v-if="currentItem" v-loading="detailLoading">
        <h2>{{ currentItem.title }}</h2>
        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="类型">{{ getTypeLabel(currentItem.contentType) }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ currentItem.sourceName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ currentItem.author || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentItem.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="浏览次数">{{ currentItem.viewCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="来源链接" :span="2">
            <a v-if="currentItem.sourceUrl" :href="currentItem.sourceUrl" target="_blank" style="color: #409eff">
              {{ currentItem.sourceUrl }}
            </a>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">摘要</el-divider>
        <p class="summary-text">{{ currentItem.summary || '暂无摘要' }}</p>
        <el-divider content-position="left">完整内容</el-divider>
        <div class="full-content">
          {{ currentItem.content }}
        </div>
      </div>
    </el-dialog>
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
const showDetailDialog = ref(false)
const currentItem = ref(null)
const detailLoading = ref(false)

const handleSearch = async () => {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  loading.value = true
  searched.value = true

  try {
    // 调用后端搜索API
    const { searchKnowledge } = await import('@/api/knowledge')
    const res = await searchKnowledge({
      keyword: keyword.value,
      current: currentPage.value,
      size: pageSize.value
    })
    
    console.log('搜索结果:', res)
    
    if (res && res.code === 200 && res.data) {
      const data = res.data
      results.value = data.records || []
      total.value = data.total || 0
      
      if (results.value.length === 0) {
        ElMessage.info('没有找到相关结果')
      }
    } else {
      results.value = []
      total.value = 0
      ElMessage.warning('未找到匹配的内容')
    }
    
    loading.value = false
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败: ' + (error.message || '请稍后重试'))
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  handleSearch()
}

const viewDetail = async (item) => {
  showDetailDialog.value = true
  detailLoading.value = true
  
  try {
    const { getKnowledge } = await import('@/api/knowledge')
    const res = await getKnowledge(item.id)
    if (res && res.code === 200 && res.data) {
      currentItem.value = res.data
    } else {
      currentItem.value = item
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    currentItem.value = item
  } finally {
    detailLoading.value = false
  }
}

const getTypeLabel = (type) => {
  const typeMap = {
    'news': '新闻',
    'article': '文章',
    'document': '文档',
    'test': '测试',
    'other': '其他'
  }
  return typeMap[type] || '其他'
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit', 
    hour: '2-digit', 
    minute: '2-digit' 
  })
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

.summary-text {
  color: #666;
  line-height: 1.8;
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin: 0;
}

.full-content {
  color: #333;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 500px;
  overflow-y: auto;
  padding: 15px;
  background: #fafafa;
  border-radius: 4px;
}
</style>

