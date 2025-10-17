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

