import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, register } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const username = ref(localStorage.getItem('username') || '')

  // 设置 Token
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 登录
  async function userLogin(data) {
    const res = await login(data)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    return res
  }

  // 注册
  async function userRegister(data) {
    const res = await register(data)
    return res
  }

  // 登出
  function logout() {
    token.value = ''
    userInfo.value = {}
    username.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('username')
  }

  // 设置用户信息
  function setUserInfo(info) {
    userInfo.value = info
    if (info.username) {
      username.value = info.username
      localStorage.setItem('username', info.username)
    }
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  return {
    token,
    userInfo,
    username,
    setToken,
    userLogin,
    userRegister,
    logout,
    setUserInfo
  }
})

