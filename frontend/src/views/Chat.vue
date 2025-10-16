<template>
  <div class="chat-container">
    <el-card class="chat-card">
      <div class="chat-messages" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty-state">
          <el-icon :size="60" color="#ccc"><ChatDotRound /></el-icon>
          <p>开始与 AI 对话吧！</p>
        </div>

        <div
          v-for="(message, index) in messages"
          :key="index"
          :class="['message', message.role]"
        >
          <div class="message-avatar">
            <el-icon v-if="message.role === 'user'" :size="24"><User /></el-icon>
            <el-icon v-else :size="24"><Service /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-text" v-html="formatMessage(message.content)"></div>
            <div class="message-time">{{ formatTime(message.time) }}</div>
          </div>
        </div>

        <div v-if="thinking" class="message assistant">
          <div class="message-avatar">
            <el-icon :size="24"><Service /></el-icon>
          </div>
          <div class="message-content">
            <div class="thinking-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-input">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="输入你的问题..."
          @keyup.enter.ctrl="handleSend"
        />
        <el-button
          type="primary"
          :loading="thinking"
          @click="handleSend"
          :disabled="!inputMessage.trim()"
        >
          发送 (Ctrl+Enter)
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ChatDotRound, User, Service } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const messages = ref([])
const inputMessage = ref('')
const thinking = ref(false)
const messagesContainer = ref(null)

const handleSend = async () => {
  if (!inputMessage.value.trim()) return

  const userMessage = {
    role: 'user',
    content: inputMessage.value,
    time: new Date()
  }

  messages.value.push(userMessage)
  const question = inputMessage.value
  inputMessage.value = ''
  thinking.value = true

  await nextTick()
  scrollToBottom()

  try {
    // 调用n8n简化RAG工作流（支持联网搜索）
    const { askQuestion } = await import('@/api/query')
    const res = await askQuestion({ query: question })
    
    console.log('n8n工作流响应:', res)
    
    // 显示AI回复
    let aiContent = ''
    const responseData = res.data
    
    if (responseData && responseData.success) {
      aiContent = responseData.answer || '未获取到答案'
      
      // 显示数据来源
      if (responseData.source === 'local_knowledge') {
        aiContent += '\n\n💡 来源：本地知识库'
      } else if (responseData.source === 'web_search') {
        aiContent += '\n\n🌐 来源：网络搜索'
      } else if (responseData.source === 'llm_direct') {
        aiContent += '\n\n🤖 来源：AI通用知识'
      }
      
      // 如果有检索到的文档，添加参考来源
      if (responseData.retrievedDocs && responseData.retrievedDocs.length > 0) {
        aiContent += '\n\n📚 参考文章：\n'
        responseData.retrievedDocs.forEach((doc, idx) => {
          aiContent += `${idx + 1}. ${doc.title || '无标题'}`
          if (doc.similarityScore) {
            aiContent += ` (相似度: ${(doc.similarityScore * 100).toFixed(1)}%)`
          }
          aiContent += '\n'
          if (doc.sourceUrl) {
            aiContent += `   🔗 ${doc.sourceUrl}\n`
          }
        })
      }
      
      // 显示响应时间
      if (responseData.responseTimeMs) {
        aiContent += `\n⏱️ 响应时间: ${responseData.responseTimeMs}ms`
      }
    } else {
      aiContent = '抱歉，暂时无法获取答案。请检查n8n服务是否正常运行。'
    }
    
    messages.value.push({
      role: 'assistant',
      content: aiContent,
      time: new Date()
    })
    
    thinking.value = false
    scrollToBottom()
  } catch (error) {
    console.error('AI问答失败:', error)
    ElMessage.error('AI问答失败: ' + (error.message || '请检查n8n服务'))
    thinking.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatMessage = (content) => {
  return content.replace(/\n/g, '<br>')
}

const formatTime = (time) => {
  return new Date(time).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.chat-container {
  padding: 20px;
  height: calc(100vh - 100px);
}

.chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.empty-state {
  text-align: center;
  color: #999;
  margin-top: 100px;
}

.empty-state p {
  margin-top: 20px;
  font-size: 16px;
}

.message {
  display: flex;
  margin-bottom: 20px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #409eff;
  color: white;
  margin-left: 10px;
}

.message.assistant .message-avatar {
  background: #67c23a;
  color: white;
  margin-right: 10px;
}

.message-content {
  max-width: 70%;
}

.message.user .message-content {
  text-align: right;
}

.message-text {
  background: #f0f0f0;
  padding: 12px 16px;
  border-radius: 10px;
  line-height: 1.6;
  word-break: break-word;
}

.message.user .message-text {
  background: #409eff;
  color: white;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.thinking-dots {
  display: flex;
  gap: 5px;
  padding: 12px 16px;
  background: #f0f0f0;
  border-radius: 10px;
}

.thinking-dots span {
  width: 8px;
  height: 8px;
  background: #999;
  border-radius: 50%;
  animation: thinking 1.4s infinite;
}

.thinking-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.thinking-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes thinking {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

.chat-input {
  border-top: 1px solid #eee;
  padding: 20px;
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.chat-input .el-input {
  flex: 1;
}
</style>

