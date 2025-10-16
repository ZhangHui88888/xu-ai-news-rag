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

export function uploadFile(formData) {
  return request({
    url: '/knowledge/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function getKnowledgeList(params) {
  return request({
    url: '/knowledge/list',
    method: 'get',
    params
  })
}

