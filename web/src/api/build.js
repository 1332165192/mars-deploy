import request from './request'

// 构建列表
export const getBuildList = (params) => {
  return request.get('/build/list', { params })
}

// 构建详情
export const getBuild = (id) => {
  return request.get(`/build/${id}`)
}

// 触发构建
export const triggerBuild = (projectId) => {
  return request.post('/build/trigger', { projectId })
}

// 删除构建记录
export const deleteBuild = (id) => {
  return request.delete(`/build/${id}`)
}

// 获取统计数据
export const getStats = () => {
  return request.get('/build/stats')
}

// 获取构建趋势数据
export const getBuildTrend = () => {
  return request.get('/build/trend')
}

// 获取构建状态分布数据
export const getStatusDistribution = () => {
  return request.get('/build/status-distribution')
}
