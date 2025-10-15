import request from './request'

export function askQuestion(data) {
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

