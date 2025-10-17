/**
 * Auth API 单元测试
 * 
 * @author XU
 * @since 2025-10-17
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { login, register } from '@/api/auth'
import request from '@/api/request'

vi.mock('@/api/request')

describe('Auth API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('login', () => {
    it('应该调用登录API并返回token', async () => {
      const mockResponse = {
        code: 200,
        message: '登录成功',
        data: {
          token: 'test-token-123',
          tokenType: 'Bearer'
        }
      }

      request.post.mockResolvedValue(mockResponse)

      const credentials = {
        username: 'testuser',
        password: 'password123'
      }

      const result = await login(credentials)

      expect(request.post).toHaveBeenCalledWith('/auth/login', credentials)
      expect(result).toEqual(mockResponse)
    })

    it('应该处理登录失败', async () => {
      const mockError = {
        code: 401,
        message: '用户名或密码错误'
      }

      request.post.mockRejectedValue(mockError)

      const credentials = {
        username: 'wronguser',
        password: 'wrongpass'
      }

      await expect(login(credentials)).rejects.toEqual(mockError)
    })
  })

  describe('register', () => {
    it('应该调用注册API并返回用户信息', async () => {
      const mockResponse = {
        code: 200,
        message: '注册成功',
        data: {
          userId: 1,
          username: 'newuser',
          email: 'newuser@example.com'
        }
      }

      request.post.mockResolvedValue(mockResponse)

      const userData = {
        username: 'newuser',
        password: 'Password123',
        confirmPassword: 'Password123',
        email: 'newuser@example.com'
      }

      const result = await register(userData)

      expect(request.post).toHaveBeenCalledWith('/auth/register', userData)
      expect(result).toEqual(mockResponse)
    })

    it('应该处理注册失败 - 用户名已存在', async () => {
      const mockError = {
        code: 500,
        message: '用户名已存在'
      }

      request.post.mockRejectedValue(mockError)

      const userData = {
        username: 'existinguser',
        password: 'Password123',
        confirmPassword: 'Password123',
        email: 'existing@example.com'
      }

      await expect(register(userData)).rejects.toEqual(mockError)
    })
  })
})

