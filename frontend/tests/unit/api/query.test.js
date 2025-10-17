/**
 * Query API 单元测试
 * 
 * @author XU
 * @since 2025-10-17
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { askQuestion, semanticSearch } from '@/api/query'
import request from '@/api/request'

vi.mock('@/api/request')

describe('Query API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('askQuestion', () => {
    it('应该发送问答请求并返回答案', async () => {
      const mockResponse = {
        code: 200,
        data: {
          answer: '这是AI生成的答案',
          query: '什么是人工智能？',
          retrievedEntries: [
            { id: 1, title: 'AI简介', score: 0.95 }
          ],
          processingTimeMs: 150
        }
      }

      request.post.mockResolvedValue(mockResponse)

      const data = {
        query: '什么是人工智能？',
        topK: 5,
        needAnswer: true
      }

      const result = await askQuestion(data)

      expect(request.post).toHaveBeenCalledWith('/query/ask', data)
      expect(result).toEqual(mockResponse)
      expect(result.data.answer).toBeTruthy()
    })

    it('应该处理AI服务不可用的情况', async () => {
      const mockError = {
        code: 500,
        message: '查询失败: 向量服务不可用'
      }

      request.post.mockRejectedValue(mockError)

      const data = {
        query: '测试问题',
        topK: 5,
        needAnswer: true
      }

      await expect(askQuestion(data)).rejects.toEqual(mockError)
    })

    it('应该支持不生成答案模式', async () => {
      const mockResponse = {
        code: 200,
        data: {
          answer: null,
          query: '什么是机器学习？',
          retrievedEntries: [
            { id: 1, title: 'ML基础', score: 0.92 }
          ],
          processingTimeMs: 80
        }
      }

      request.post.mockResolvedValue(mockResponse)

      const data = {
        query: '什么是机器学习？',
        topK: 5,
        needAnswer: false
      }

      const result = await askQuestion(data)

      expect(result.data.answer).toBeNull()
      expect(result.data.retrievedEntries).toHaveLength(1)
    })
  })

  describe('semanticSearch', () => {
    it('应该执行语义搜索', async () => {
      const mockResponse = {
        code: 200,
        data: {
          query: '深度学习',
          retrievedEntries: [
            { id: 1, title: '深度学习入门', score: 0.98 },
            { id: 2, title: '神经网络基础', score: 0.85 }
          ],
          processingTimeMs: 60
        }
      }

      request.post.mockResolvedValue(mockResponse)

      const data = {
        query: '深度学习',
        topK: 10
      }

      const result = await semanticSearch(data)

      expect(request.post).toHaveBeenCalledWith('/query/search', data)
      expect(result).toEqual(mockResponse)
      expect(result.data.retrievedEntries).toHaveLength(2)
    })

    it('应该处理未找到相关文档的情况', async () => {
      const mockResponse = {
        code: 200,
        data: {
          query: '不存在的内容',
          retrievedEntries: [],
          processingTimeMs: 45
        }
      }

      request.post.mockResolvedValue(mockResponse)

      const data = {
        query: '不存在的内容',
        topK: 5
      }

      const result = await semanticSearch(data)

      expect(result.data.retrievedEntries).toHaveLength(0)
    })
  })
})

