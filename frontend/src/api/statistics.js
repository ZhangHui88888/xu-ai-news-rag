import request from './request'

export function getStatistics() {
  return request({
    url: '/statistics/overview',
    method: 'get'
  })
}

export function getDetailedStatistics() {
  return request({
    url: '/statistics/detailed',
    method: 'get'
  })
}

export function getKeywordsTop10() {
  return request({
    url: '/statistics/keywords-top10',
    method: 'get'
  })
}

