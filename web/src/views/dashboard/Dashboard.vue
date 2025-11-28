<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <n-grid :x-gap="16" :y-gap="16" :cols="4" responsive="screen">
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="项目总数" :value="stats.projectCount">
            <template #prefix>
              <n-icon size="32" color="#18a058">
                <FolderOpenSharp />
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
      
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="服务器总数" :value="stats.serverCount">
            <template #prefix>
              <n-icon size="32" color="#2080f0">
                <ServerSharp />
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
      
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="构建总数" :value="stats.buildCount">
            <template #prefix>
              <n-icon size="32" color="#f0a020">
                <RocketSharp />
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
      
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="成功率">
            <template #prefix>
              <n-icon size="32" color="#18a058">
                <CheckmarkCircleSharp />
              </n-icon>
            </template>
            <template #default>
              {{ stats.successRate }}%
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
    </n-grid>
    
    <!-- 图表区域 -->
    <n-grid :x-gap="16" :y-gap="16" :cols="2" style="margin-top: 16px;" responsive="screen">
      <n-gi>
        <n-card title="构建趋势" :bordered="false">
          <div ref="buildTrendChart" style="width: 100%; height: 300px;"></div>
        </n-card>
      </n-gi>
      
      <n-gi>
        <n-card title="构建状态分布" :bordered="false">
          <div ref="buildStatusChart" style="width: 100%; height: 300px;"></div>
        </n-card>
      </n-gi>
    </n-grid>
    
    <!-- 最近构建 表格 -->
    <n-card title="最近构建" :bordered="false" style="margin-top: 16px;">
      <template #header-extra>
        <n-text depth="3" style="font-size: 14px;">展示最近10条构建记录</n-text>
      </template>
      <n-data-table
        :columns="columns"
        :data="recentBuilds"
        :loading="loading"
        :pagination="false"
      />
    </n-card>
    
    <!-- 项目信息 -->
    <n-card :bordered="false" style="margin-top: 16px;">
      <n-grid :x-gap="24" :y-gap="16" :cols="3" responsive="screen">
        <n-gi>
          <div class="info-section">
            <n-icon size="40" color="#18a058" style="margin-bottom: 12px;">
              <PersonSharp />
            </n-icon>
            <h3>开源作者</h3>
            <n-divider style="margin: 12px 0" />
            <n-space vertical size="small">
              <n-text strong style="font-size: 16px; color: #18a058;">程序员Mars</n-text>
              <n-text depth="3" style="font-size: 13px;">• 抖音技术博主</n-text>
              <n-text depth="3" style="font-size: 13px;">• 精通 Java/Python 开发</n-text>
              <n-text depth="3" style="font-size: 13px;">• 分布式系统架构师</n-text>
              <n-text depth="3" style="font-size: 13px;">• 爬虫技术专家</n-text>
            </n-space>
          </div>
        </n-gi>
        
        <n-gi>
          <div class="info-section">
            <n-icon size="40" color="#2080f0" style="margin-bottom: 12px;">
              <CubeSharp />
            </n-icon>
            <h3>项目功能</h3>
            <n-divider style="margin: 12px 0" />
            <n-space vertical size="small">
              <n-text depth="3" style="font-size: 13px;">• 项目管理（Git配置、构建命令）</n-text>
              <n-text depth="3" style="font-size: 13px;">• 一键触发构建与部署</n-text>
              <n-text depth="3" style="font-size: 13px;">• 实时构建日志推送</n-text>
              <n-text depth="3" style="font-size: 13px;">• 服务器SSH管理与连接测试</n-text>
              <n-text depth="3" style="font-size: 13px;">• 自动化部署流程</n-text>
            </n-space>
          </div>
        </n-gi>
        
        <n-gi>
          <div class="info-section">
            <n-icon size="40" color="#f0a020" style="margin-bottom: 12px;">
              <CodeSlashSharp />
            </n-icon>
            <h3>技术栈</h3>
            <n-divider style="margin: 12px 0" />
            <n-space vertical size="small">
              <n-text depth="3" style="font-size: 13px;"><strong>后端:</strong> Spring Boot 3 + MyBatis-Plus</n-text>
              <n-text depth="3" style="font-size: 13px;"><strong>前端:</strong> Vue3 + Vite + Naive UI</n-text>
              <n-text depth="3" style="font-size: 13px;"><strong>数据库:</strong> MySQL 8.0</n-text>
              <n-text depth="3" style="font-size: 13px;"><strong>工具:</strong> JGit、JSch、ECharts</n-text>
            </n-space>
          </div>
        </n-gi>
      </n-grid>
    </n-card>
    
    <!-- 更新日志 -->
    <n-card title="更新日志" :bordered="false" style="margin-top: 16px;">
      <n-timeline>
        <n-timeline-item
          type="success"
          title="v1.0.0"
          content="2024-01-15"
        >
          <template #default>
            <n-space vertical size="small" style="margin-top: 8px;">
              <n-text depth="2">• 项目管理功能：支持 Java/Vue 项目配置、Git 仓库集成</n-text>
              <n-text depth="2">• 一键构建部署：支持 Maven/Gradle/npm 等构建工具</n-text>
              <n-text depth="2">• 实时构建日志：WebSocket 实时推送构建日志</n-text>
              <n-text depth="2">• 服务器管理：SSH 连接测试、SFTP 文件上传</n-text>
              <n-text depth="2">• 权限管理：基于角色的权限控制系统</n-text>
              <n-text depth="2">• 数据可视化：构建趋势图、状态分布图</n-text>
            </n-space>
          </template>
        </n-timeline-item>
      </n-timeline>
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { NTag, NButton } from 'naive-ui'
import {
  FolderOpenSharp,
  ServerSharp,
  RocketSharp,
  CheckmarkCircleSharp,
  PersonSharp,
  CubeSharp,
  CodeSlashSharp
} from '@vicons/ionicons5'
import * as echarts from 'echarts'
import { getBuildList, getStats, getBuildTrend, getStatusDistribution } from '@/api/build'

const router = useRouter()
const loading = ref(false)
const buildTrendChart = ref(null)
const buildStatusChart = ref(null)

const stats = ref({
  projectCount: 0,
  serverCount: 0,
  buildCount: 0,
  successRate: 0
})

const recentBuilds = ref([])

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '项目名称', key: 'projectName' },
  { 
    title: '状态', 
    key: 'status',
    render: (row) => {
      return h(
        NTag,
        {
          type: getStatusType(row.status),
          size: 'small'
        },
        { default: () => getStatusText(row.status) }
      )
    }
  },
  { title: '触发人', key: 'triggerByName' },
  { title: '开始时间', key: 'startTime' },
  {
    title: '操作',
    key: 'action',
    render: (row) => {
      return h(
        NButton,
        {
          text: true,
          type: 'primary',
          onClick: () => viewDetail(row.id)
        },
        { default: () => '查看详情' }
      )
    }
  }
]

const getStatusType = (status) => {
  const map = {
    'PENDING': 'default',
    'RUNNING': 'info',
    'SUCCESS': 'success',
    'FAILED': 'error'
  }
  return map[status] || 'default'
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '等待中',
    'RUNNING': '运行中',
    'SUCCESS': '成功',
    'FAILED': '失败'
  }
  return map[status] || status
}

const viewDetail = (id) => {
  if (!id) {
    console.error('构建ID不存在')
    return
  }
  router.push(`/build-detail/${id}`)
}

// 初始化构建趋势图表
const initBuildTrendChart = async () => {
  if (!buildTrendChart.value) return
  
  try {
    // 从后端获取数据
    const trendData = await getBuildTrend()
    
    const chart = echarts.init(buildTrendChart.value)
    const option = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['成功', '失败']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: trendData.dates || []
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '成功',
          type: 'line',
          smooth: true,
          data: trendData.successData || [],
          itemStyle: { color: '#18a058' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(24, 160, 88, 0.3)' },
              { offset: 1, color: 'rgba(24, 160, 88, 0.05)' }
            ])
          }
        },
        {
          name: '失败',
          type: 'line',
          smooth: true,
          data: trendData.failedData || [],
          itemStyle: { color: '#d03050' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(208, 48, 80, 0.3)' },
              { offset: 1, color: 'rgba(208, 48, 80, 0.05)' }
            ])
          }
        }
      ]
    }
    chart.setOption(option)
    
    // 响应式
    window.addEventListener('resize', () => chart.resize())
  } catch (error) {
    console.error('加载构建趋势数据失败:', error)
  }
}

// 初始化构建状态分布图表
const initBuildStatusChart = async () => {
  if (!buildStatusChart.value) return
  
  try {
    // 从后端获取数据
    const statusData = await getStatusDistribution()
    
    // 颜色映射
    const colorMap = {
      '成功': '#18a058',
      '失败': '#d03050',
      '运行中': '#2080f0',
      '等待中': '#f0a020'
    }
    
    // 为每个数据项添加颜色
    const chartData = statusData.map(item => ({
      ...item,
      itemStyle: { color: colorMap[item.name] || '#999' }
    }))
    
    const chart = echarts.init(buildStatusChart.value)
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: '10%',
        top: 'center'
      },
      series: [
        {
          name: '构建状态',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 20,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: chartData
        }
      ]
    }
    chart.setOption(option)
    
    // 响应式
    window.addEventListener('resize', () => chart.resize())
  } catch (error) {
    console.error('加载构建状态分布数据失败:', error)
  }
}

const loadData = async () => {
  try {
    loading.value = true
    
    // 加载统计数据和最近构建列表
    const [statsData, buildData] = await Promise.all([
      getStats(),
      getBuildList({ current: 1, size: 10 })
    ])
    
    // 设置统计数据
    stats.value.projectCount = statsData.projectCount || 0
    stats.value.serverCount = statsData.serverCount || 0
    stats.value.buildCount = statsData.buildCount || 0
    stats.value.successRate = statsData.successRate || 0
    
    // 设置最近构建列表（默认展示最近10条）
    recentBuilds.value = buildData.records || []
    
    // 初始化图表
    setTimeout(() => {
      initBuildTrendChart()
      initBuildStatusChart()
    }, 100)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  width: 100%;
}

.stat-card {
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

:deep(.n-statistic) {
  display: flex;
  align-items: center;
  gap: 16px;
}

:deep(.n-statistic-value__prefix) {
  margin-right: 0;
}

.info-section {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-radius: 8px;
  transition: all 0.3s;
}

.info-section:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.info-section h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.info-section :deep(.n-space) {
  text-align: left;
}
</style>
