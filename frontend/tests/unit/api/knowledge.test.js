/**
 * Knowledge API 单元测试
 * 
 * @author XU
 * @since 2025-10-17
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { getKnowledgeList, getKnowledgeById, createKnowledge, updateKnowledge, deleteKnowledge } from '@/api/knowledge'
import request from '@/api/request'

vi.mock('@/api/request')

describe('Knowledge API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getKnowledgeList', () => {
    it('应该获取知识库列表', async () => {
      const mockResponse = {
        code: 200,
        data: {
          records: [
            { id: 1, title: '知识条目1', content: '内容1' },
            { id: 2, title: '知识条目2', content: '内容2' }
          ],
          total: 2,
          current: 1,
          size: 10
        }
      }

      request.get.mockResolvedValue(mockResponse)

      const params = { page: 1, size: 10 }
      const result = await getKnowledgeList(params)

      expect(request.get).toHaveBeenCalledWith('/knowledge/list', { params })
      expect(result).toEqual(mockResponse)
    })

    it('应该支持关键词搜索', async () => {
      const mockResponse = {
        code: 200,
        data: {
          records: [{ id: 1, title: 'AI相关知识', content: '内容' }],
          total: 1,
          current: 1,
          size: 10
        }
      }

      request.get.mockResolvedValue(mockResponse)

      const params = { page: 1, size: 10, keyword: 'AI' }
      const result = await getKnowledgeList(params)

      expect(request.get).toHaveBeenCalledWith('/knowledge/list', { params })
      expect(result.data.records).toHaveLength(1)
    })
  })

  describe('getKnowledgeById', () => {
    it('应该根据ID获取知识详情', async () => {
      const mockResponse = {
        code: 200,
        data: {
          id: 1,
          title: '知识条目',
          content: '详细内容',
          viewCount: 10
        }
      }

      request.get.mockResolvedValue(mockResponse)

      const result = await getKnowledgeById(1)

      expect(request.get).toHaveBeenCalledWith('/knowledge/1')
      expect(result).toEqual(mockResponse)
    })

    it('应该处理知识条目不存在的情况', async () => {
      const mockError = {
        code: 404,
        message: '知识条目不存在'
      }

      request.get.mockRejectedValue(mockError)

      await expect(getKnowledgeById(999)).rejects.toEqual(mockError)
    })
  })

  describe('createKnowledge', () => {
    it('应该创建新的知识条目', async () => {
      const mockResponse = {
        code: 200,
        message: '创建成功',
        data: {
          id: 1,
          title: '新知识',
          content: '新内容'
        }
      }

      request.post.mockResolvedValue(mockResponse)

      const data = {
        title: '新知识',
        content: '新内容',
        contentType: 'article'
      }

      const result = await createKnowledge(data)

      expect(request.post).toHaveBeenCalledWith('/knowledge', data)
      expect(result).toEqual(mockResponse)
    })
  })

  describe('updateKnowledge', () => {
    it('应该更新知识条目', async () => {
      const mockResponse = {
        code: 200,
        message: '更新成功'
      }

      request.put.mockResolvedValue(mockResponse)

      const id = 1
      const data = {
        title: '更新的标题',
        content: '更新的内容'
      }

      const result = await updateKnowledge(id, data)

      expect(request.put).toHaveBeenCalledWith(`/knowledge/${id}`, data)
      expect(result).toEqual(mockResponse)
    })
  })

  describe('deleteKnowledge', () => {
    it('应该删除知识条目', async () => {
      const mockResponse = {
        code: 200,
        message: '删除成功'
      }

      request.delete.mockResolvedValue(mockResponse)

      const id = 1
      const result = await deleteKnowledge(id)

      expect(request.delete).toHaveBeenCalledWith(`/knowledge/${id}`)
      expect(result).toEqual(mockResponse)
    })

    it('应该处理删除失败的情况', async () => {
      const mockError = {
        code: 500,
        message: '删除失败'
      }

      request.delete.mockRejectedValue(mockError)

      await expect(deleteKnowledge(999)).rejects.toEqual(mockError)
    })
  })
})

