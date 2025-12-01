<template>
  <div class="server-list">
    <div class="toolbar">
      <n-space>
        <n-input
          v-model:value="searchName"
          placeholder="搜索服务器名称"
          clearable
          style="width: 250px"
          @keyup.enter="loadData"
        >
          <template #suffix>
            <n-button text @click="loadData">
              <n-icon><SearchSharp /></n-icon>
            </n-button>
          </template>
        </n-input>
        <n-button type="primary" @click="showModal()">
          <template #icon>
            <n-icon><AddSharp /></n-icon>
          </template>
          新增服务器
        </n-button>
      </n-space>
    </div>
    
    <n-data-table
      :columns="columns"
      :data="dataSource"
      :pagination="pagination"
      :loading="loading"
      :remote="true"
      @update:page="handlePageChange"
      @update:page-size="handlePageSizeChange"
    />
    
    <n-modal
      v-model:show="visible"
      :title="editId ? '编辑服务器' : '新增服务器'"
      preset="dialog"
      style="width: 700px"
      :show-icon="false"
    >
      <n-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        label-placement="left"
        label-width="100"
      >
        <n-form-item label="服务器名称" path="name">
          <n-input v-model:value="formState.name" placeholder="请输入服务器名称" />
        </n-form-item>
        
        <n-form-item label="主机地址" path="host">
          <n-input v-model:value="formState.host" placeholder="192.168.1.100" />
        </n-form-item>
        
        <n-form-item label="端口" path="port">
          <n-input-number v-model:value="formState.port" :min="1" :max="65535" style="width: 100%" />
        </n-form-item>
        
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="formState.username" placeholder="root" />
        </n-form-item>
        
        <n-form-item label="认证方式" path="authType">
          <n-radio-group v-model:value="formState.authType">
            <n-radio value="PASSWORD">密码</n-radio>
            <n-radio value="SSH_KEY">SSH Key</n-radio>
          </n-radio-group>
        </n-form-item>
        
        <n-form-item v-if="formState.authType === 'PASSWORD'" label="密码" path="password">
          <n-input v-model:value="formState.password" type="password" show-password-on="click" placeholder="请输入密码" />
        </n-form-item>
        
        <n-form-item v-if="formState.authType === 'SSH_KEY'" label="私钥" path="privateKey">
          <n-input v-model:value="formState.privateKey" type="textarea" :rows="5" placeholder="-----BEGIN RSA PRIVATE KEY-----" />
        </n-form-item>
        
<!--        <n-form-item label="上传目录" path="uploadPath">-->
<!--          <n-input v-model:value="formState.uploadPath" placeholder="/opt/deploy" />-->
<!--        </n-form-item>-->
      </n-form>
      
      <template #action>
        <n-space>
          <n-button @click="visible = false">取消</n-button>
          <n-button type="primary" @click="handleOk">确定</n-button>
        </n-space>
      </template>
    </n-modal>
    
    <!-- SSH 控制台弹窗 -->
    <n-modal
      v-model:show="consoleVisible"
      preset="card"
      :style="consoleFullscreen ? 'width: 100vw; height: 100vh; max-width: 100vw; top: 0; padding: 0;' : 'width: 90%; max-width: 1200px;'"
      :bordered="false"
      :closable="false"
      :mask-closable="false"
      @after-leave="closeConsole"
    >
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between;">
          <span>SSH 控制台 - {{ currentServer?.name || '' }}</span>
          <n-space>
            <n-button text @click="toggleConsoleFullscreen">
              <template #icon>
                <n-icon><component :is="consoleFullscreen ? ContractSharp : ExpandSharp" /></n-icon>
              </template>
              {{ consoleFullscreen ? '退出全屏' : '全屏' }}
            </n-button>
            <n-button text @click="closeConsole">
              <template #icon>
                <n-icon><CloseSharp /></n-icon>
              </template>
              关闭
            </n-button>
          </n-space>
        </div>
      </template>
      
      <div class="terminal-container" :style="consoleFullscreen ? 'height: calc(100vh - 100px);' : 'height: 600px;'">
        <div ref="terminalRef" class="terminal"></div>
      </div>
    </n-modal>
    
    <!-- 监控面板弹窗 -->
    <n-modal
      v-model:show="monitorVisible"
      preset="card"
      :style="monitorFullscreen ? 'width: 100vw; height: 100vh; max-width: 100vw; top: 0; padding: 0;' : 'width: 95%; max-width: 1400px;'"
      :bordered="false"
      :closable="false"
      :mask-closable="false"
      @after-leave="closeMonitor"
    >
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between;">
          <span>服务器监控 - {{ currentServer?.name || '' }}</span>
          <n-space>
            <n-button text @click="loadMonitorData" :loading="monitorLoading">
              <template #icon>
                <n-icon><RefreshSharp /></n-icon>
              </template>
              立即刷新
            </n-button>
            <n-button text @click="toggleFullscreen">
              <template #icon>
                <n-icon><component :is="monitorFullscreen ? ContractSharp : ExpandSharp" /></n-icon>
              </template>
              {{ monitorFullscreen ? '退出全屏' : '全屏' }}
            </n-button>
            <n-button text @click="closeMonitor">
              <template #icon>
                <n-icon><CloseSharp /></n-icon>
              </template>
              关闭
            </n-button>
          </n-space>
        </div>
      </template>
      
      <n-spin :show="monitorLoading">
        <div class="monitor-container" :style="monitorFullscreen ? 'height: calc(100vh - 100px); overflow-y: auto;' : ''">
          <!-- 系统信息卡片 -->
          <n-card title="系统信息" :bordered="false" class="monitor-info-card">
            <n-descriptions :column="3" label-placement="left">
              <n-descriptions-item label="主机名">
                <n-text type="success">
                  <n-icon style="vertical-align: middle; margin-right: 4px;">
                    <ServerSharp />
                  </n-icon>
                  {{ monitorData.hostname || 'N/A' }}
                </n-text>
              </n-descriptions-item>
              <n-descriptions-item label="操作系统">
                {{ monitorData.os || 'N/A' }}
              </n-descriptions-item>
              <n-descriptions-item label="运行时间">
                {{ monitorData.uptime || 'N/A' }}
              </n-descriptions-item>
              <n-descriptions-item label="CPU核心数">
                <n-tag type="info" size="small">{{ monitorData.cpuCores || 0 }} 核</n-tag>
              </n-descriptions-item>
              <n-descriptions-item label="进程数">
                <n-tag type="warning" size="small">{{ monitorData.processCount || 0 }}</n-tag>
              </n-descriptions-item>
              <n-descriptions-item label="负载均值">
                <n-tag type="error" size="small">{{ monitorData.loadAvg || 'N/A' }}</n-tag>
              </n-descriptions-item>
            </n-descriptions>
          </n-card>
          
          <!-- 仪表盘图表 -->
          <n-grid :x-gap="16" :y-gap="16" :cols="3" style="margin-top: 16px;">
            <n-gi>
              <n-card title="CPU使用率" :bordered="false" class="chart-card">
                <div id="cpuChart" :style="monitorFullscreen ? 'width: 100%; height: 350px;' : 'width: 100%; height: 280px;'"></div>
                <div class="chart-info">
                  <n-text depth="3">当前使用: </n-text>
                  <n-text strong type="info">{{ (monitorData.cpuUsage || 0).toFixed(2) }}%</n-text>
                </div>
              </n-card>
            </n-gi>
            
            <n-gi>
              <n-card title="内存使用率" :bordered="false" class="chart-card">
                <div id="memChart" :style="monitorFullscreen ? 'width: 100%; height: 350px;' : 'width: 100%; height: 280px;'"></div>
                <div class="chart-info">
                  <n-text depth="3">已用: </n-text>
                  <n-text strong type="warning">{{ monitorData.memUsed || 0 }}MB</n-text>
                  <n-text depth="3"> / 总计: </n-text>
                  <n-text strong>{{ monitorData.memTotal || 0 }}MB</n-text>
                </div>
              </n-card>
            </n-gi>
            
            <n-gi>
              <n-card title="磁盘使用率" :bordered="false" class="chart-card">
                <div id="diskChart" :style="monitorFullscreen ? 'width: 100%; height: 350px;' : 'width: 100%; height: 280px;'"></div>
                <div class="chart-info">
                  <n-text depth="3">已用: </n-text>
                  <n-text strong type="error">{{ monitorData.diskUsed || 'N/A' }}</n-text>
                  <n-text depth="3"> / 总计: </n-text>
                  <n-text strong>{{ monitorData.diskTotal || 'N/A' }}</n-text>
                </div>
              </n-card>
            </n-gi>
          </n-grid>
          

        </div>
      </n-spin>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h, nextTick } from 'vue'
import { useMessage, NButton, NSpace, NTag, NBadge, NPopconfirm } from 'naive-ui'
import { SearchSharp, AddSharp, TerminalSharp, CheckmarkCircleSharp, CreateSharp, TrashSharp, BarChartSharp, ServerSharp, RefreshSharp, ExpandSharp, ContractSharp, CloseSharp } from '@vicons/ionicons5'
import { getServerList, addServer, updateServer, deleteServer, testConnection, getServerMonitor } from '@/api/server'
import { Terminal } from 'xterm'
import { FitAddon } from 'xterm-addon-fit'
import 'xterm/css/xterm.css'
import * as echarts from 'echarts'

const message = useMessage()
const loading = ref(false)
const visible = ref(false)
const consoleVisible = ref(false)
const consoleFullscreen = ref(false)
const monitorVisible = ref(false)
const monitorFullscreen = ref(false)
const editId = ref(null)
const searchName = ref('')
const formRef = ref()
const testingId = ref(null)
const terminalRef = ref(null)
const currentServer = ref(null)
const monitorLoading = ref(false)
const monitorData = ref({})

let terminal = null
let fitAddon = null
let ws = null

const dataSource = ref([])

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '服务器名称', key: 'name' },
  { title: '主机地址', key: 'host' },
  { title: '端口', key: 'port', width: 80 },
  { title: '用户名', key: 'username', width: 100 },
  { 
    title: '认证方式', 
    key: 'authType', 
    width: 100,
    render: (row) => {
      return h(
        NTag,
        { size: 'small' },
        { default: () => row.authType === 'PASSWORD' ? '密码' : 'SSH Key' }
      )
    }
  },
  { 
    title: '状态', 
    key: 'status', 
    width: 100,
    render: (row) => {
      return h(
        NBadge,
        {
          dot: true,
          type: row.status === 'ONLINE' ? 'success' : 'error'
        },
        { default: () => row.status === 'ONLINE' ? '在线' : '离线' }
      )
    }
  },
  { 
    title: '操作', 
    key: 'action', 
    width: 400,
    render: (row) => {
      return h(
        NSpace,
        {},
        {
          default: () => [
            h(
              NButton,
              {
                text: true,
                type: 'warning',
                size: 'small',
                onClick: () => openMonitor(row)
              },
              { 
                default: () => '监控面板',
                icon: () => h('i', { class: 'n-icon' }, [h(BarChartSharp)])
              }
            ),
            h(
              NButton,
              {
                text: true,
                type: 'info',
                size: 'small',
                onClick: () => openConsole(row)
              },
              { 
                default: () => '控制台',
                icon: () => h('i', { class: 'n-icon' }, [h(TerminalSharp)])
              }
            ),
            h(
              NButton,
              {
                text: true,
                type: 'primary',
                size: 'small',
                loading: testingId.value === row.id,
                onClick: () => testConn(row.id)
              },
              { 
                default: () => '测试连接',
                icon: () => h('i', { class: 'n-icon' }, [h(CheckmarkCircleSharp)])
              }
            ),
            h(
              NButton,
              {
                text: true,
                type: 'success',
                size: 'small',
                onClick: () => showModal(row)
              },
              { 
                default: () => '编辑',
                icon: () => h('i', { class: 'n-icon' }, [h(CreateSharp)])
              }
            ),
            h(
              NPopconfirm,
              {
                onPositiveClick: () => handleDelete(row.id)
              },
              {
                default: () => '确定要删除吗？',
                trigger: () => h(
                  NButton,
                  {
                    text: true,
                    type: 'error',
                    size: 'small'
                  },
                  { 
                    default: () => '删除',
                    icon: () => h('i', { class: 'n-icon' }, [h(TrashSharp)])
                  }
                )
              }
            )
          ]
        }
      )
    }
  }
]

const formState = reactive({
  name: '',
  host: '',
  port: 22,
  username: 'root',
  authType: 'PASSWORD',
  password: '',
  privateKey: '',
  uploadPath: '/opt/deploy'
})

const rules = {
  name: { required: true, message: '请输入服务器名称', trigger: 'blur' },
  host: { required: true, message: '请输入主机地址', trigger: 'blur' },
  port: { required: true, message: '请输入端口', trigger: 'blur' },
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  authType: { required: true, message: '请选择认证方式', trigger: 'change' }
}

const loadData = async () => {
  try {
    loading.value = true
    
    const params = {
      current: pagination.page,
      size: pagination.pageSize
    }
    
    if (searchName.value) {
      params.name = searchName.value
    }
    
    const data = await getServerList(params)
    
    dataSource.value = data.records
    pagination.itemCount = data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  loadData()
}

const showModal = (record) => {
  if (record) {
    editId.value = record.id
    Object.assign(formState, record)
  } else {
    editId.value = null
    Object.assign(formState, {
      name: '',
      host: '',
      port: 22,
      username: 'root',
      authType: 'PASSWORD',
      password: '',
      privateKey: '',
      uploadPath: '/opt/deploy'
    })
  }
  visible.value = true
}

const handleOk = async () => {
  try {
    await formRef.value?.validate()
    
    if (editId.value) {
      await updateServer({ ...formState, id: editId.value })
      message.success('更新成功')
    } else {
      await addServer(formState)
      message.success('添加成功')
    }
    
    visible.value = false
    loadData()
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (id) => {
  try {
    await deleteServer(id)
    message.success('删除成功')
    loadData()
  } catch (error) {
    console.error(error)
  }
}

const testConn = async (id) => {
  try {
    testingId.value = id
    await testConnection(id)
    message.success('连接成功')
    loadData()
  } catch (error) {
    console.error(error)
  } finally {
    testingId.value = null
  }
}

// 打开监控面板
const openMonitor = (server) => {
  currentServer.value = server
  monitorVisible.value = true
  loadMonitorData()
}

// 加载监控数据
const loadMonitorData = async () => {
  if (!currentServer.value) return
  
  try {
    monitorLoading.value = true
    const data = await getServerMonitor(currentServer.value.id)
    monitorData.value = data
    
    // 初始化或更新图表
    nextTick(() => {
      initMonitorCharts()
    })
  } catch (error) {
    console.error('加载监控数据失败:', error)
    message.error('获取监控数据失败')
  } finally {
    monitorLoading.value = false
  }
}

// 关闭监控面板
const closeMonitor = () => {
  monitorVisible.value = false
  monitorFullscreen.value = false
  currentServer.value = null
  monitorData.value = {}
}

// 切换全屏
const toggleFullscreen = () => {
  monitorFullscreen.value = !monitorFullscreen.value
  // 延迟调整图表大小
  setTimeout(() => {
    const cpuChart = echarts.getInstanceByDom(document.getElementById('cpuChart'))
    const memChart = echarts.getInstanceByDom(document.getElementById('memChart'))
    const diskChart = echarts.getInstanceByDom(document.getElementById('diskChart'))
    const netChart = echarts.getInstanceByDom(document.getElementById('netChart'))
    cpuChart?.resize()
    memChart?.resize()
    diskChart?.resize()
    netChart?.resize()
  }, 300)
}

// 初始化监控图表
const initMonitorCharts = () => {
  // CPU使用率仪表盘
  const cpuChart = echarts.init(document.getElementById('cpuChart'))
  cpuChart.setOption({
    series: [{
      type: 'gauge',
      startAngle: 180,
      endAngle: 0,
      min: 0,
      max: 100,
      splitNumber: 8,
      axisLine: {
        lineStyle: {
          width: 6,
          color: [
            [0.3, '#18a058'],
            [0.7, '#f0a020'],
            [1, '#d03050']
          ]
        }
      },
      pointer: {
        icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
        length: '12%',
        width: 20,
        offsetCenter: [0, '-60%'],
        itemStyle: {
          color: 'auto'
        }
      },
      axisTick: {
        length: 12,
        lineStyle: {
          color: 'auto',
          width: 2
        }
      },
      splitLine: {
        length: 20,
        lineStyle: {
          color: 'auto',
          width: 5
        }
      },
      axisLabel: {
        color: '#464646',
        fontSize: 14,
        distance: -60,
        formatter: function (value) {
          return value + '%'
        }
      },
      title: {
        offsetCenter: [0, '-20%'],
        fontSize: 16,
        color: '#464646'
      },
      detail: {
        fontSize: 30,
        offsetCenter: [0, '0%'],
        valueAnimation: true,
        formatter: function (value) {
          return Math.round(value) + '%'
        },
        color: 'auto'
      },
      data: [{
        value: monitorData.value.cpuUsage || 0,
        name: 'CPU使用率'
      }]
    }]
  })
  
  // 内存使用率仪表盘
  const memChart = echarts.init(document.getElementById('memChart'))
  memChart.setOption({
    series: [{
      type: 'gauge',
      startAngle: 180,
      endAngle: 0,
      min: 0,
      max: 100,
      splitNumber: 8,
      axisLine: {
        lineStyle: {
          width: 6,
          color: [
            [0.3, '#18a058'],
            [0.7, '#f0a020'],
            [1, '#d03050']
          ]
        }
      },
      pointer: {
        icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
        length: '12%',
        width: 20,
        offsetCenter: [0, '-60%'],
        itemStyle: {
          color: 'auto'
        }
      },
      axisTick: {
        length: 12,
        lineStyle: {
          color: 'auto',
          width: 2
        }
      },
      splitLine: {
        length: 20,
        lineStyle: {
          color: 'auto',
          width: 5
        }
      },
      axisLabel: {
        color: '#464646',
        fontSize: 14,
        distance: -60,
        formatter: function (value) {
          return value + '%'
        }
      },
      title: {
        offsetCenter: [0, '-20%'],
        fontSize: 16,
        color: '#464646'
      },
      detail: {
        fontSize: 30,
        offsetCenter: [0, '0%'],
        valueAnimation: true,
        formatter: function (value) {
          return Math.round(value) + '%'
        },
        color: 'auto'
      },
      data: [{
        value: monitorData.value.memUsage || 0,
        name: '内存使用率'
      }]
    }]
  })
  
  // 磁盘使用率仪表盘
  const diskChart = echarts.init(document.getElementById('diskChart'))
  diskChart.setOption({
    series: [{
      type: 'gauge',
      startAngle: 180,
      endAngle: 0,
      min: 0,
      max: 100,
      splitNumber: 8,
      axisLine: {
        lineStyle: {
          width: 6,
          color: [
            [0.3, '#18a058'],
            [0.7, '#f0a020'],
            [1, '#d03050']
          ]
        }
      },
      pointer: {
        icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
        length: '12%',
        width: 20,
        offsetCenter: [0, '-60%'],
        itemStyle: {
          color: 'auto'
        }
      },
      axisTick: {
        length: 12,
        lineStyle: {
          color: 'auto',
          width: 2
        }
      },
      splitLine: {
        length: 20,
        lineStyle: {
          color: 'auto',
          width: 5
        }
      },
      axisLabel: {
        color: '#464646',
        fontSize: 14,
        distance: -60,
        formatter: function (value) {
          return value + '%'
        }
      },
      title: {
        offsetCenter: [0, '-20%'],
        fontSize: 16,
        color: '#464646'
      },
      detail: {
        fontSize: 30,
        offsetCenter: [0, '0%'],
        valueAnimation: true,
        formatter: function (value) {
          return Math.round(value) + '%'
        },
        color: 'auto'
      },
      data: [{
        value: monitorData.value.diskUsage || 0,
        name: '磁盘使用率'
      }]
    }]
  })
  
  // 网络流量柱状图
  const netChart = echarts.init(document.getElementById('netChart'))
  netChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['接收', '发送']
    },
    yAxis: {
      type: 'value',
      name: 'MB'
    },
    series: [{
      name: '流量',
      type: 'bar',
      data: [
        {
          value: monitorData.value.netRx || 0,
          itemStyle: { color: '#2080f0' }
        },
        {
          value: monitorData.value.netTx || 0,
          itemStyle: { color: '#18a058' }
        }
      ],
      label: {
        show: true,
        position: 'top',
        formatter: '{c} MB'
      },
      barWidth: '50%'
    }]
  })
  
  // 响应式调整
  window.addEventListener('resize', () => {
    cpuChart.resize()
    memChart.resize()
    diskChart.resize()
    netChart.resize()
  })
}

// 打开控制台
const openConsole = (server) => {
  currentServer.value = server
  consoleVisible.value = true
  
  nextTick(() => {
    initTerminal()
  })
}

// 初始化终端
const initTerminal = () => {
  if (!terminalRef.value) return
  
  // 创建终端实例
  terminal = new Terminal({
    cursorBlink: true,
    fontSize: 14,
    fontFamily: 'Consolas, Monaco, Courier New, monospace',
    theme: {
      background: '#1e1e1e',
      foreground: '#d4d4d4',
      cursor: '#ffffff',
      selection: 'rgba(255, 255, 255, 0.3)'
    },
    scrollback: 1000
  })
  
  // 自适应插件
  fitAddon = new FitAddon()
  terminal.loadAddon(fitAddon)
  
  // 挂载到 DOM
  terminal.open(terminalRef.value)
  fitAddon.fit()
  
  // 连接 WebSocket
  connectWebSocket()
  
  // 监听用户输入
  terminal.onData((data) => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(data)
    }
  })
  
  // 窗口大小改变时自适应
  window.addEventListener('resize', handleResize)
}

// 连接 WebSocket
const connectWebSocket = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/ws/ssh/terminal`
  
  ws = new WebSocket(wsUrl)
  
  ws.onopen = () => {
    console.log('WebSocket SSH 连接已建立')
    terminal.writeln('\x1b[32m正在连接服务器...\x1b[0m')
    
    // 发送连接命令
    const server = currentServer.value
    const connectMsg = `CONNECT|${server.host}|${server.port}|${server.username}|${server.authType}|${server.password || server.privateKey || ''}`
    ws.send(connectMsg)
  }
  
  ws.onmessage = (event) => {
    terminal.write(event.data)
  }
  
  ws.onerror = (error) => {
    console.error('WebSocket 错误:', error)
    terminal.writeln('\r\n\x1b[31m连接错误\x1b[0m')
  }
  
  ws.onclose = () => {
    console.log('WebSocket SSH 连接已关闭')
    terminal.writeln('\r\n\x1b[33m连接已断开\x1b[0m')
  }
}

// 关闭控制台
const closeConsole = () => {
  if (ws) {
    ws.send('CLOSE')
    ws.close()
    ws = null
  }
  
  if (terminal) {
    terminal.dispose()
    terminal = null
  }
  
  if (fitAddon) {
    fitAddon = null
  }
  
  window.removeEventListener('resize', handleResize)
  
  consoleVisible.value = false
  consoleFullscreen.value = false
  currentServer.value = null
}

// 切换控制台全屏
const toggleConsoleFullscreen = () => {
  consoleFullscreen.value = !consoleFullscreen.value
  // 延迟调整终端大小
  setTimeout(() => {
    if (fitAddon && consoleVisible.value) {
      fitAddon.fit()
    }
  }, 300)
}

// 窗口大小改变处理
const handleResize = () => {
  if (fitAddon && consoleVisible.value) {
    fitAddon.fit()
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.server-list {
  width: 100%;
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
}

.terminal-container {
  background: #1e1e1e;
  border-radius: 4px;
  padding: 10px;
  overflow: hidden;
}

.terminal {
  height: 100%;
  width: 100%;
}

/* xterm.js 样式覆盖 */
:deep(.xterm) {
  height: 100%;
  padding: 0;
}

:deep(.xterm-viewport) {
  overflow-y: auto;
}

:deep(.xterm-screen) {
  padding: 8px;
}

/* 监控面板样式 */
.monitor-container {
  min-height: 600px;
}

.monitor-info-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.monitor-info-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.chart-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transform: translateY(-4px);
}

.chart-info {
  text-align: center;
  margin-top: 10px;
  padding: 10px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 4px;
}
</style>
