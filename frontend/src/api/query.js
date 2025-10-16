import request from './request'
import axios from 'axios'

// n8n Webhook URL
const N8N_WEBHOOK_URL = 'http://192.168.171.128:5678/webhook/rag-query'

// 使用n8n RAG工作流
export function askQuestion(data) {
  return axios.post(N8N_WEBHOOK_URL, {
    query: data.query,
    userId: data.userId || 1,
    topK: data.topK || 5,
    enableWebSearch: true
  })
}

// 备用：直接调用后端API（不经过n8n）
export function askQuestionDirect(data) {
  return request({
    url: '/query/ask',
    method: 'post',
    data
  })
}

export function semanticSearch(data) {
  return request({
    url: '/query/search',
    method: 'post',
    data
  })
}

