/**
 * User Store 单元测试
 * 
 * @author XU
 * @since 2025-10-17
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('应该初始化为未登录状态', () => {
    const store = useUserStore()
    expect(store.isLoggedIn).toBe(false)
    expect(store.token).toBe('')
    expect(store.userInfo).toBeNull()
  })

  it('应该能够设置token', () => {
    const store = useUserStore()
    const testToken = 'test-token-123'
    
    store.setToken(testToken)
    
    expect(store.token).toBe(testToken)
    expect(store.isLoggedIn).toBe(true)
    expect(localStorage.setItem).toHaveBeenCalledWith('token', testToken)
  })

  it('应该能够设置用户信息', () => {
    const store = useUserStore()
    const testUserInfo = {
      id: 1,
      username: 'testuser',
      email: 'test@example.com'
    }
    
    store.setUserInfo(testUserInfo)
    
    expect(store.userInfo).toEqual(testUserInfo)
    expect(localStorage.setItem).toHaveBeenCalledWith('userInfo', JSON.stringify(testUserInfo))
  })

  it('应该能够登出', () => {
    const store = useUserStore()
    
    // 先登录
    store.setToken('test-token')
    store.setUserInfo({ id: 1, username: 'test' })
    
    // 登出
    store.logout()
    
    expect(store.token).toBe('')
    expect(store.userInfo).toBeNull()
    expect(store.isLoggedIn).toBe(false)
    expect(localStorage.removeItem).toHaveBeenCalledWith('token')
    expect(localStorage.removeItem).toHaveBeenCalledWith('userInfo')
  })

  it('应该从localStorage恢复登录状态', () => {
    const testToken = 'stored-token'
    const testUserInfo = { id: 1, username: 'test' }
    
    localStorage.getItem.mockImplementation((key) => {
      if (key === 'token') return testToken
      if (key === 'userInfo') return JSON.stringify(testUserInfo)
      return null
    })
    
    const store = useUserStore()
    store.initFromStorage()
    
    expect(store.token).toBe(testToken)
    expect(store.userInfo).toEqual(testUserInfo)
    expect(store.isLoggedIn).toBe(true)
  })

  it('应该检查token是否存在', () => {
    const store = useUserStore()
    
    expect(store.hasToken()).toBe(false)
    
    store.setToken('test-token')
    
    expect(store.hasToken()).toBe(true)
  })

  it('应该获取用户角色', () => {
    const store = useUserStore()
    
    expect(store.getUserRole()).toBe('guest')
    
    store.setUserInfo({ id: 1, username: 'test', role: 'admin' })
    
    expect(store.getUserRole()).toBe('admin')
  })

  it('应该检查是否为管理员', () => {
    const store = useUserStore()
    
    expect(store.isAdmin()).toBe(false)
    
    store.setUserInfo({ id: 1, username: 'admin', role: 'admin' })
    
    expect(store.isAdmin()).toBe(true)
  })
})

