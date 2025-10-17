<template>
  <div class="knowledge-container">
    <el-card class="toolbar-card">
      <el-row :gutter="10" align="middle">
        <el-col :span="18">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索知识条目..."
            clearable
            @keyup.enter="loadData"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="showUploadDialog = true">
            <el-icon><Upload /></el-icon>
            上传文档
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="table-card">
      <el-table
        :data="tableData"
        :loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="contentType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.contentType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceName" label="来源" width="150" />
        <el-table-column prop="publishedAt" label="发布时间" width="120">
          <template #default="{ row }">
            {{ formatDate(row.publishedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="viewDetail(row)">
              查看
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
        style="margin-top: 20px; text-align: right"
      />
    </el-card>

    <!-- 上传对话框 -->
    <el-dialog v-model="showUploadDialog" title="上传文档" width="500px">
      <el-upload
        drag
        :action="uploadUrl"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        accept=".pdf,.doc,.docx,.txt,.md"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 PDF、Word、TXT、Markdown 格式，单个文件不超过 50MB
          </div>
        </template>
      </el-upload>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="知识详情" width="800px">
      <div v-if="currentItem">
        <h2>{{ currentItem.title }}</h2>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="类型">{{ currentItem.contentType }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ currentItem.sourceName }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ currentItem.author }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(currentItem.publishedAt) }}</el-descriptions-item>
          <el-descriptions-item label="浏览次数">{{ currentItem.viewCount }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <div class="content">
          {{ currentItem.content || currentItem.summary }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Search, Upload, UploadFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const searchKeyword = ref('')
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const showUploadDialog = ref(false)
const showDetailDialog = ref(false)
const currentItem = ref(null)

const uploadUrl = computed(() => {
  return import.meta.env.VITE_API_BASE_URL + '/knowledge/upload'
})

const uploadHeaders = computed(() => {
  return {
    Authorization: 'Bearer ' + userStore.token
  }
})

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    // 调用后端API获取知识库列表
    const { getKnowledgeList } = await import('@/api/knowledge')
    const res = await getKnowledgeList({ 
      page: currentPage.value, 
      size: pageSize.value, 
      keyword: searchKeyword.value 
    })
    
    console.log('知识库列表:', res)
    
    if (res && res.code === 200 && res.data) {
      const data = res.data
      tableData.value = data.records || []
      total.value = data.total || 0
    } else {
      tableData.value = []
      total.value = 0
    }
    
    loading.value = false
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载失败: ' + (error.message || '请稍后重试'))
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
}

const viewDetail = async (row) => {
  try {
    const { getKnowledge } = await import('@/api/knowledge')
    const res = await getKnowledge(row.id)
    if (res && res.code === 200 && res.data) {
      currentItem.value = res.data
      showDetailDialog.value = true
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败: ' + (error.message || '请稍后重试'))
  }
}

const handleDelete = async (row) => {
  ElMessageBox.confirm('确定要删除这条知识吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const { deleteKnowledge } = await import('@/api/knowledge')
      await deleteKnowledge(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '请稍后重试'))
    }
  }).catch(() => {})
}

const handleUploadSuccess = (response) => {
  ElMessage.success('上传成功')
  showUploadDialog.value = false
  loadData()
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.knowledge-container {
  padding: 20px;
}

.toolbar-card {
  margin-bottom: 20px;
}

.content {
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>

