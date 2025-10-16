import request from './request'
import axios from 'axios'

// 使用简化的n8n RAG工作流（支持联网搜索）
const N8N_WEBHOOK_URL = 'http://192.168.171.128:5678/webhook/simple-rag'

export function askQuestion(data) {
  return axios.post(N8N_WEBHOOK_URL, {
    query: data.query,
    topK: data.topK || 5,
    needAnswer: true,
    similarityThreshold: 0.5
  }, {
    timeout: 60000
  })
}

// 备用：直接调用后端API
export function askQuestionDirect(data) {
  return request({
    url: '/query/ask',
    method: 'post',
    data: {
      query: data.query,
      topK: data.topK || 5,
      needAnswer: true,
      similarityThreshold: 0.5
    }
  })
}

export function semanticSearch(data) {
  return request({
    url: '/query/search',
    method: 'post',
    data
  })
}
