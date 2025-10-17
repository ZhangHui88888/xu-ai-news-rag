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

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="features-card">
          <h2>核心功能</h2>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="feature-item" @click="$router.push('/search')">
                <el-icon :size="40" color="#409eff"><Search /></el-icon>
                <h3>智能搜索</h3>
                <p>基于向量的语义搜索</p>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="feature-item" @click="$router.push('/chat')">
                <el-icon :size="40" color="#67c23a"><ChatDotRound /></el-icon>
                <h3>AI 问答</h3>
                <p>智能对话助手</p>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 20px">
            <el-col :span="24">
              <div class="feature-item" @click="$router.push('/knowledge')">
                <el-icon :size="40" color="#e6a23c"><FolderOpened /></el-icon>
                <h3>知识管理</h3>
                <p>知识库管理与维护</p>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="keywords-card" v-loading="keywordsLoading">
          <h2>🔥 热门关键词 Top10</h2>
          <div v-if="keywords.length > 0" class="keywords-list">
            <div
              v-for="(item, index) in keywords"
              :key="index"
              class="keyword-item"
            >
              <div class="keyword-rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
              <div class="keyword-name">{{ item.keyword }}</div>
              <div class="keyword-bar">
                <div 
                  class="keyword-bar-fill" 
                  :style="{ width: getBarWidth(item.count) + '%' }"
                  :class="'bar-' + (index + 1)"
                ></div>
              </div>
              <div class="keyword-count">{{ item.count }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无关键词数据" :image-size="100" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document, ChatDotRound, User, Search, FolderOpened } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getStatistics, getKeywordsTop10 } from '@/api/statistics'

const stats = ref({
  knowledgeCount: 0,
  queryCount: 0,
  userCount: 0
})

const loading = ref(false)
const keywordsLoading = ref(false)
const keywords = ref([])
const maxCount = ref(0)

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

const loadKeywords = async () => {
  keywordsLoading.value = true
  try {
    const res = await getKeywordsTop10()
    if (res && res.code === 200 && res.data) {
      keywords.value = res.data
      if (keywords.value.length > 0) {
        maxCount.value = keywords.value[0].count
      }
    }
  } catch (error) {
    console.error('加载关键词数据失败:', error)
  } finally {
    keywordsLoading.value = false
  }
}

const getBarWidth = (count) => {
  if (maxCount.value === 0) return 0
  return Math.max((count / maxCount.value) * 100, 5)
}

onMounted(() => {
  loadStatistics()
  loadKeywords()
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

.keywords-card {
  height: 100%;
}

.keywords-card h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
  font-size: 18px;
}

.keywords-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.keyword-item {
  display: grid;
  grid-template-columns: 40px 1fr 2fr 50px;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s;
}

.keyword-item:hover {
  background: #f5f7fa;
  transform: translateX(5px);
}

.keyword-rank {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  color: white;
  background: #909399;
}

.keyword-rank.rank-1 {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  box-shadow: 0 2px 8px rgba(245, 87, 108, 0.3);
}

.keyword-rank.rank-2 {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  box-shadow: 0 2px 8px rgba(79, 172, 254, 0.3);
}

.keyword-rank.rank-3 {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  box-shadow: 0 2px 8px rgba(67, 233, 123, 0.3);
}

.keyword-name {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.keyword-bar {
  height: 8px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}

.keyword-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.8s ease;
}

.keyword-bar-fill.bar-1 {
  background: linear-gradient(90deg, #f093fb 0%, #f5576c 100%);
}

.keyword-bar-fill.bar-2 {
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
}

.keyword-bar-fill.bar-3 {
  background: linear-gradient(90deg, #43e97b 0%, #38f9d7 100%);
}

.keyword-bar-fill.bar-4,
.keyword-bar-fill.bar-5,
.keyword-bar-fill.bar-6,
.keyword-bar-fill.bar-7,
.keyword-bar-fill.bar-8,
.keyword-bar-fill.bar-9,
.keyword-bar-fill.bar-10 {
  background: linear-gradient(90deg, #a8edea 0%, #fed6e3 100%);
}

.keyword-count {
  font-weight: bold;
  color: #409eff;
  text-align: right;
  font-size: 14px;
}
</style>

