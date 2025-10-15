import request from './request'

export function searchKnowledge(data) {
  return request({
    url: '/knowledge/search',
    method: 'post',
    data
  })
}

export function createKnowledge(data) {
  return request({
    url: '/knowledge',
    method: 'post',
    data
  })
}

export function getKnowledge(id) {
  return request({
    url: `/knowledge/${id}`,
    method: 'get'
  })
}

export function deleteKnowledge(id) {
  return request({
    url: `/knowledge/${id}`,
    method: 'delete'
  })
}

