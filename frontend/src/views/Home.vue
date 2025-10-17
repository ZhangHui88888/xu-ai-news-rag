<template>
  <div class="home-container">
    <el-card class="welcome-card">
      <h1>欢迎使用 XU-News AI-RAG 系统</h1>
      <p>个性化新闻智能知识库系统</p>
    </el-card>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card" v-loading="loading">
          <div class="stat-icon" style="background: #409eff;">
            <el-icon :size="30"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <h3>知识条目</h3>
            <p class="stat-number">{{ stats.knowledgeCount }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" v-loading="loading">
          <div class="stat-icon" style="background: #67c23a;">
            <el-icon :size="30"><ChatDotRound /></el-icon>
          </div>
          <div class="stat-content">
            <h3>查询次数</h3>
            <p class="stat-number">{{ stats.queryCount }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" v-loading="loading">
          <div class="stat-icon" style="background: #e6a23c;">
            <el-icon :size="30"><User /></el-icon>
          </div>
          <div class="stat-content">
            <h3>用户数量</h3>
            <p class="stat-number">{{ stats.userCount }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="features-card">
      <h2>核心功能</h2>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="feature-item" @click="$router.push('/search')">
            <el-icon :size="40" color="#409eff"><Search /></el-icon>
            <h3>智能搜索</h3>
            <p>基于向量的语义搜索</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="feature-item" @click="$router.push('/chat')">
            <el-icon :size="40" color="#67c23a"><ChatDotRound /></el-icon>
            <h3>AI 问答</h3>
            <p>智能对话助手</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="feature-item" @click="$router.push('/knowledge')">
            <el-icon :size="40" color="#e6a23c"><FolderOpened /></el-icon>
            <h3>知识管理</h3>
            <p>知识库管理与维护</p>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document, ChatDotRound, User, Search, FolderOpened } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getStatistics } from '@/api/statistics'

const stats = ref({
  knowledgeCount: 0,
  queryCount: 0,
  userCount: 0
})

const loading = ref(false)

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

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.welcome-card {
  text-align: center;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.welcome-card h1 {
  margin: 0;
  font-size: 32px;
}

.welcome-card p {
  margin: 10px 0 0;
  font-size: 16px;
  opacity: 0.9;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 20px;
}

.stat-content h3 {
  margin: 0 0 10px;
  color: #666;
  font-size: 14px;
}

.stat-number {
  margin: 0;
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.features-card h2 {
  margin-top: 0;
  color: #333;
}

.feature-item {
  text-align: center;
  padding: 30px;
  border: 2px dashed #ddd;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.feature-item:hover {
  border-color: #409eff;
  background: #f0f9ff;
  transform: translateY(-5px);
}

.feature-item h3 {
  margin: 15px 0 10px;
  color: #333;
}

.feature-item p {
  margin: 0;
  color: #666;
  font-size: 14px;
}
</style>

