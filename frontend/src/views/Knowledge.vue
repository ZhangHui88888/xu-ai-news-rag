<template>
  <div class="knowledge-container">
    <!-- 搜索和操作工具栏 -->
    <el-card class="toolbar-card">
      <el-row :gutter="10" align="middle" style="margin-bottom: 15px">
        <el-col :span="18">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索知识条目（按标题或内容筛选）..."
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button @click="handleSearch" :loading="loading">
                搜索
              </el-button>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="showUploadDialog = true" style="width: 100%">
            <el-icon><Upload /></el-icon>
            上传文档
          </el-button>
        </el-col>
      </el-row>
      
      <!-- 筛选器 -->
      <el-row :gutter="10" align="middle">
        <el-col :span="6">
          <el-select v-model="filterType" placeholder="选择类型" clearable @change="handleSearch" style="width: 100%">
            <el-option label="全部类型" value="" />
            <el-option label="新闻" value="news" />
            <el-option label="文章" value="article" />
            <el-option label="文档" value="document" />
            <el-option label="测试" value="test" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-col>
        <el-col :span="10">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="handleSearch"
            style="width: 100%"
          />
        </el-col>
        <el-col :span="8">
          <el-space>
            <el-button 
              type="danger" 
              :disabled="selectedIds.length === 0"
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除 {{ selectedIds.length > 0 ? `(${selectedIds.length})` : '' }}
            </el-button>
            <el-button @click="handleClearFilters">
              <el-icon><RefreshLeft /></el-icon>
              重置
            </el-button>
          </el-space>
        </el-col>
      </el-row>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        :data="tableData"
        :loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="createdAt" label="创建时间" width="180" sortable>
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="contentType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagColor(row.contentType)">
              {{ getTypeLabel(row.contentType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceName" label="来源" width="150" show-overflow-tooltip />
        <el-table-column prop="viewCount" label="浏览" width="80" sortable />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="viewDetail(row)">
              查看
            </el-button>
            <el-button type="info" size="small" link @click="handleEdit(row)">
              编辑
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
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
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
          <el-descriptions-item label="类型">{{ getTypeLabel(currentItem.contentType) }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ currentItem.sourceName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ currentItem.author || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentItem.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="浏览次数">{{ currentItem.viewCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="标签">
            <el-tag v-for="tag in parseTag(currentItem.tags)" :key="tag" size="small" style="margin-right: 5px">
              {{ tag }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <div class="content">
          {{ currentItem.content || currentItem.summary }}
        </div>
      </div>
    </el-dialog>

    <!-- 编辑元数据对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑元数据" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input 
            v-model="editForm.summary" 
            type="textarea" 
            :rows="3"
            placeholder="请输入摘要"
          />
        </el-form-item>
        <el-form-item label="内容类型">
          <el-select v-model="editForm.contentType" placeholder="选择类型" style="width: 100%">
            <el-option label="新闻" value="news" />
            <el-option label="文章" value="article" />
            <el-option label="文档" value="document" />
            <el-option label="测试" value="test" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源">
          <el-input v-model="editForm.sourceName" placeholder="请输入来源名称" />
        </el-form-item>
        <el-form-item label="来源URL">
          <el-input v-model="editForm.sourceUrl" placeholder="请输入来源URL" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="editForm.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input 
            v-model="editForm.tagsInput" 
            placeholder="多个标签用逗号分隔，如：AI,机器学习,深度学习"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Search, Upload, UploadFilled, Delete, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const searchKeyword = ref('')
const filterType = ref('')
const dateRange = ref(null)
const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedIds = ref([])
const showUploadDialog = ref(false)
const showDetailDialog = ref(false)
const showEditDialog = ref(false)
const currentItem = ref(null)
const editForm = ref({
  id: null,
  title: '',
  summary: '',
  contentType: '',
  sourceName: '',
  sourceUrl: '',
  author: '',
  tagsInput: ''
})

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
    const { getKnowledgeList } = await import('@/api/knowledge')
    const params = { 
      page: currentPage.value, 
      size: pageSize.value, 
      keyword: searchKeyword.value 
    }
    
    // 添加类型筛选
    if (filterType.value) {
      params.contentType = filterType.value
    }
    
    // 添加时间范围筛选
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = formatDateForAPI(dateRange.value[0])
      params.endDate = formatDateForAPI(dateRange.value[1])
    }
    
    const res = await getKnowledgeList(params)
    
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

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleClearFilters = () => {
  searchKeyword.value = ''
  filterType.value = ''
  dateRange.value = null
  handleSearch()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
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

const handleEdit = (row) => {
  editForm.value = {
    id: row.id,
    title: row.title || '',
    summary: row.summary || '',
    contentType: row.contentType || 'other',
    sourceName: row.sourceName || '',
    sourceUrl: row.sourceUrl || '',
    author: row.author || '',
    tagsInput: parseTags(row.tags)
  }
  showEditDialog.value = true
}

const handleSaveEdit = async () => {
  if (!editForm.value.title) {
    ElMessage.warning('标题不能为空')
    return
  }
  
  saving.value = true
  try {
    const { updateKnowledge } = await import('@/api/knowledge')
    
    const updateData = {
      title: editForm.value.title,
      summary: editForm.value.summary,
      contentType: editForm.value.contentType,
      sourceName: editForm.value.sourceName,
      sourceUrl: editForm.value.sourceUrl,
      author: editForm.value.author,
      tags: editForm.value.tagsInput ? 
        JSON.stringify(editForm.value.tagsInput.split(',').map(t => t.trim()).filter(t => t)) : 
        null
    }
    
    await updateKnowledge(editForm.value.id, updateData)
    ElMessage.success('更新成功')
    showEditDialog.value = false
    loadData()
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('更新失败: ' + (error.message || '请稍后重试'))
  } finally {
    saving.value = false
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

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的条目')
    return
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条知识吗？`, '批量删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const { batchDeleteKnowledge } = await import('@/api/knowledge')
      const res = await batchDeleteKnowledge(selectedIds.value)
      
      if (res && res.code === 200) {
        const result = res.data
        if (result.failCount === 0) {
          ElMessage.success(`成功删除 ${result.successCount} 条记录`)
        } else {
          ElMessage.warning(`成功删除 ${result.successCount} 条，失败 ${result.failCount} 条`)
        }
        selectedIds.value = []
        loadData()
      }
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败: ' + (error.message || '请稍后重试'))
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

const getTypeTagColor = (type) => {
  const colorMap = {
    'news': 'success',
    'article': 'primary',
    'document': 'info',
    'test': 'warning',
    'other': ''
  }
  return colorMap[type] || ''
}

const parseTags = (tags) => {
  if (!tags) return ''
  try {
    const parsed = JSON.parse(tags)
    return Array.isArray(parsed) ? parsed.join(', ') : tags
  } catch {
    return tags
  }
}

const parseTag = (tags) => {
  if (!tags) return []
  try {
    const parsed = JSON.parse(tags)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
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

const formatDateForAPI = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toISOString().split('T')[0]
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
  max-height: 400px;
  overflow-y: auto;
}
</style>
