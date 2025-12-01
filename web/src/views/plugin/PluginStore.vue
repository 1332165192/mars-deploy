<template>
  <div class="plugin-store">
    <!-- 服务器选择 -->
    <n-card title="插件商店" size="small" style="margin-bottom: 16px;">
      <template #header-extra>
        <n-select
          v-model:value="selectedServerId"
          :options="serverOptions"
          placeholder="选择服务器"
          style="width: 300px"
          @update:value="loadInstalledPlugins"
        />
      </template>
      <n-text depth="3">为服务器快速安装常用开发环境和中间件</n-text>
    </n-card>

    <n-spin :show="loading">
      <!-- 基础环境插件 -->
      <n-card title="🛠️ 基础环境" size="small" style="margin-bottom: 16px;">
        <n-grid :cols="4" :x-gap="12" :y-gap="12">
          <n-grid-item v-for="plugin in baseEnvPlugins" :key="plugin.id">
            <n-card size="small" hoverable class="plugin-card">
              <div class="plugin-header">
                <n-icon :size="32" :color="getPluginColor(plugin.pluginKey)">
                  <component :is="getPluginIcon(plugin.icon)" />
                </n-icon>
                <div class="plugin-info">
                  <div class="plugin-name">{{ plugin.name }}</div>
                  <n-tag
                    v-if="isInstalled(plugin.id)"
                    :type="getInstallStatus(plugin.id) === 'SUCCESS' ? 'success' : 'warning'"
                    size="small"
                  >
                    {{ getInstallStatus(plugin.id) === 'SUCCESS' ? '已安装' : '安装中' }}
                  </n-tag>
                </div>
              </div>
              
              <div class="plugin-description">
                {{ plugin.description }}
              </div>
              
              <div class="plugin-actions">
                <n-space>
                  <n-select
                    v-model:value="plugin.selectedVersion"
                    :options="getVersionOptions(plugin.versions)"
                    size="small"
                    style="width: 120px"
                    :disabled="!selectedServerId || isInstalled(plugin.id)"
                  />
                  <n-button
                    v-if="!isInstalled(plugin.id) || getInstallStatus(plugin.id) === 'FAILED'"
                    type="primary"
                    size="small"
                    @click="handleInstall(plugin)"
                    :disabled="!selectedServerId"
                    :loading="installing[plugin.id]"
                  >
                    {{ getInstallStatus(plugin.id) === 'FAILED' ? '重新安装' : '安装' }}
                  </n-button>
                  <n-button
                    v-else-if="getInstallStatus(plugin.id) === 'SUCCESS'"
                    type="error"
                    size="small"
                    @click="handleUninstall(plugin)"
                    :loading="uninstalling[plugin.id]"
                  >
                    卸载
                  </n-button>
                  <n-tag
                    v-else-if="getInstallStatus(plugin.id) === 'INSTALLING'"
                    type="warning"
                    size="small"
                  >
                    安装中...
                  </n-tag>
                  <n-button
                    v-if="isInstalled(plugin.id)"
                    size="small"
                    @click="showInstallLog(plugin)"
                  >
                    日志
                  </n-button>
                </n-space>
              </div>
            </n-card>
          </n-grid-item>
        </n-grid>
      </n-card>

      <!-- 中间件插件 -->
      <n-card title="📦 中间件服务" size="small">
        <n-grid :cols="4" :x-gap="12" :y-gap="12">
          <n-grid-item v-for="plugin in middlewarePlugins" :key="plugin.id">
            <n-card size="small" hoverable class="plugin-card">
              <div class="plugin-header">
                <n-icon :size="32" :color="getPluginColor(plugin.pluginKey)">
                  <component :is="getPluginIcon(plugin.icon)" />
                </n-icon>
                <div class="plugin-info">
                  <div class="plugin-name">{{ plugin.name }}</div>
                  <n-tag
                    v-if="isInstalled(plugin.id)"
                    :type="getInstallStatus(plugin.id) === 'SUCCESS' ? 'success' : 'warning'"
                    size="small"
                  >
                    {{ getInstallStatus(plugin.id) === 'SUCCESS' ? '已安装' : '安装中' }}
                  </n-tag>
                </div>
              </div>
              
              <div class="plugin-description">
                {{ plugin.description }}
              </div>
              
              <div class="plugin-actions">
                <n-space>
                  <n-select
                    v-model:value="plugin.selectedVersion"
                    :options="getVersionOptions(plugin.versions)"
                    size="small"
                    style="width: 120px"
                    :disabled="!selectedServerId || isInstalled(plugin.id)"
                  />
                  <n-button
                    v-if="!isInstalled(plugin.id) || getInstallStatus(plugin.id) === 'FAILED'"
                    type="primary"
                    size="small"
                    @click="handleInstall(plugin)"
                    :disabled="!selectedServerId"
                    :loading="installing[plugin.id]"
                  >
                    {{ getInstallStatus(plugin.id) === 'FAILED' ? '重新安装' : '安装' }}
                  </n-button>
                  <n-button
                    v-else-if="getInstallStatus(plugin.id) === 'SUCCESS'"
                    type="error"
                    size="small"
                    @click="handleUninstall(plugin)"
                    :loading="uninstalling[plugin.id]"
                  >
                    卸载
                  </n-button>
                  <n-tag
                    v-else-if="getInstallStatus(plugin.id) === 'INSTALLING'"
                    type="warning"
                    size="small"
                  >
                    安装中...
                  </n-tag>
                  <n-button
                    v-if="isInstalled(plugin.id)"
                    size="small"
                    @click="showInstallLog(plugin)"
                  >
                    日志
                  </n-button>
                </n-space>
              </div>
            </n-card>
          </n-grid-item>
        </n-grid>
      </n-card>
    </n-spin>

    <!-- 日志查看弹窗 -->
    <n-modal
      v-model:show="logModalVisible"
      preset="card"
      :title="currentInstallId ? (installedPlugins.find(i => i.id === currentInstallId)?.status === 'UNINSTALLING' ? '卸载日志' : '安装日志') : '日志详情'"
      style="width: 800px"
      :segmented="{ content: 'soft', footer: 'soft' }"
      :mask-closable="false"
      @update:show="handleLogModalClose"
    >
      <n-scrollbar style="max-height: 500px">
        <n-code :code="currentLog" language="bash" :word-wrap="true" />
      </n-scrollbar>
      <template #footer>
        <n-space justify="end">
          <n-tag v-if="currentInstallId && installedPlugins.find(i => i.id === currentInstallId)?.status === 'INSTALLING'" type="warning" :bordered="false">
            <template #icon>
              <n-icon :component="SyncSharp" />
            </template>
            安装中...
          </n-tag>
          <n-tag v-if="currentInstallId && installedPlugins.find(i => i.id === currentInstallId)?.status === 'UNINSTALLING'" type="error" :bordered="false">
            <template #icon>
              <n-icon :component="SyncSharp" />
            </template>
            卸载中...
          </n-tag>
          <n-button @click="closeLogModal">关闭</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive, h, watch, nextTick, onUnmounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import {
  GitBranchSharp,
  CodeSharp,
  LogoNodejs,
  LogoDocker,
  LogoPython,
  ConstructSharp,
  ServerSharp,
  SyncSharp
} from '@vicons/ionicons5'
import { getPluginList, installPlugin, uninstallPlugin, getInstallLog, getInstalledPlugins } from '@/api/plugin'
import { getServerList } from '@/api/server'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const selectedServerId = ref(null)
const serverOptions = ref([])
const baseEnvPlugins = ref([])
const middlewarePlugins = ref([])
const installedPlugins = ref([])
const installing = reactive({})
const uninstalling = reactive({})
const logModalVisible = ref(false)
const currentLog = ref('')
const currentInstallId = ref(null)
const logSocket = ref(null)

// Icon 映射
const getPluginIcon = (iconName) => {
  const iconMap = {
    GitBranchSharp,
    CodeSharp,
    LogoNodejs,
    LogoDocker,
    LogoPython,
    ConstructSharp,
    ServerSharp
  }
  return iconMap[iconName] || ServerSharp
}

// 插件颜色映射
const getPluginColor = (pluginKey) => {
  const colorMap = {
    git: '#F05032',
    jdk8: '#007396',
    jdk17: '#007396',
    nodejs: '#339933',
    docker: '#2496ED',
    python3: '#3776AB',
    maven: '#C71A36',
    nginx: '#009639',
    mysql: '#4479A1',
    redis: '#DC382D',
    mongodb: '#47A248'
  }
  return colorMap[pluginKey] || '#18a058'
}

// 加载服务器列表
const loadServers = async () => {
  try {
    const res = await getServerList({ current: 1, size: 100 })
    serverOptions.value = res.records.map(server => ({
      label: server.name,
      value: server.id
    }))
    
    // 默认选择第一个服务器
    if (serverOptions.value.length > 0 && !selectedServerId.value) {
      selectedServerId.value = serverOptions.value[0].value
      // 加载已安装插件
      loadInstalledPlugins()
    }
  } catch (error) {
    message.error('加载服务器列表失败')
  }
}

// 加载插件列表
const loadPlugins = async () => {
  try {
    loading.value = true
    const data = await getPluginList()
    
    // 处理基础环境插件
    baseEnvPlugins.value = (data.BASE_ENV || []).map(plugin => ({
      ...plugin,
      selectedVersion: plugin.defaultVersion,
      versions: JSON.parse(plugin.versions || '[]')
    }))
    
    // 处理中间件插件
    middlewarePlugins.value = (data.MIDDLEWARE || []).map(plugin => ({
      ...plugin,
      selectedVersion: plugin.defaultVersion,
      versions: JSON.parse(plugin.versions || '[]')
    }))
  } catch (error) {
    message.error('加载插件列表失败')
  } finally {
    loading.value = false
  }
}

// 加载已安装插件
const loadInstalledPlugins = async () => {
  if (!selectedServerId.value) {
    installedPlugins.value = []
    return
  }
  
  try {
    installedPlugins.value = await getInstalledPlugins(selectedServerId.value)
  } catch (error) {
    message.error('加载已安装插件失败')
  }
}

// 获取版本选项
const getVersionOptions = (versions) => {
  return versions.map(v => ({ label: v, value: v }))
}

// 检查是否已安装
const isInstalled = (pluginId) => {
  return installedPlugins.value.some(p => p.pluginId === pluginId)
}

// 获取安装状态
const getInstallStatus = (pluginId) => {
  const installed = installedPlugins.value.find(p => p.pluginId === pluginId)
  return installed?.status || ''
}

// 获取安装记录ID
const getInstallId = (pluginId) => {
  const installed = installedPlugins.value.find(p => p.pluginId === pluginId)
  return installed?.id
}

// 安装插件
const handleInstall = async (plugin) => {
  if (!selectedServerId.value) {
    message.warning('请先选择服务器')
    return
  }
  
  const isReinstall = getInstallStatus(plugin.id) === 'FAILED'
  
  dialog.warning({
    title: isReinstall ? '重新安装' : '确认安装',
    content: `确定要${isReinstall ? '重新' : ''}在服务器上安装 ${plugin.name} (${plugin.selectedVersion}) 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        installing[plugin.id] = true
        
        // 如果是重新安装，先删除旧记录
        if (isReinstall) {
          const oldInstallId = getInstallId(plugin.id)
          if (oldInstallId) {
            await uninstallPlugin(oldInstallId)
            await new Promise(resolve => setTimeout(resolve, 500))
          }
        }
        
        const installId = await installPlugin({
          serverId: selectedServerId.value,
          pluginId: plugin.id,
          version: plugin.selectedVersion
        })
        
        // 立即打开日志弹窗并连接WebSocket
        currentInstallId.value = installId
        currentLog.value = ''
        logModalVisible.value = true
        connectLogSocket(installId)
        
        message.success(isReinstall ? '重新安装任务已提交' : '安装任务已提交')
      } catch (error) {
        message.error('安装失败：' + error.message)
        installing[plugin.id] = false
      }
    }
  })
}

// 卸载插件
const handleUninstall = async (plugin) => {
  const installId = getInstallId(plugin.id)
  if (!installId) return
  
  dialog.error({
    title: '确认卸载',
    content: `确定要卸载 ${plugin.name} 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        uninstalling[plugin.id] = true
        await uninstallPlugin(installId)
        
        // 立即打开日志弹窗并连接WebSocket
        currentInstallId.value = installId
        currentLog.value = ''
        logModalVisible.value = true
        connectLogSocket(installId)
        
        message.success('卸载任务已提交')
      } catch (error) {
        message.error('卸载失败：' + error.message)
        uninstalling[plugin.id] = false
      }
    }
  })
}

// 查看安装日志
const showInstallLog = async (plugin) => {
  const installId = getInstallId(plugin.id)
  if (!installId) return
  
  try {
    const data = await getInstallLog(installId)
    currentLog.value = data.log || '暂无日志'
    currentInstallId.value = null // 查看历史日志，不连接WebSocket
    logModalVisible.value = true
  } catch (error) {
    message.error('获取日志失败')
  }
}

// 连接WebSocket日志
const connectLogSocket = (installId) => {
  // 关闭之前的连接
  if (logSocket.value) {
    logSocket.value.close()
  }
  
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/ws/plugin/${installId}`
  
  logSocket.value = new WebSocket(wsUrl)
  
  logSocket.value.onopen = () => {
    console.log('WebSocket连接已建立')
  }
  
  logSocket.value.onmessage = (event) => {
    currentLog.value += event.data + '\n'
    // 自动滚动到底部
    nextTick(() => {
      const scrollbar = document.querySelector('.n-scrollbar-container')
      if (scrollbar) {
        scrollbar.scrollTop = scrollbar.scrollHeight
      }
    })
  }
  
  logSocket.value.onclose = () => {
    console.log('WebSocket连接已关闭')
    // 刷新插件列表
    setTimeout(() => {
      loadInstalledPlugins()
      // 清除loading状态
      Object.keys(installing).forEach(key => {
        installing[key] = false
      })
      Object.keys(uninstalling).forEach(key => {
        uninstalling[key] = false
      })
    }, 1000)
  }
  
  logSocket.value.onerror = (error) => {
    console.error('WebSocket错误', error)
    message.error('日志连接失败')
  }
}

// 关闭日志弹窗
const closeLogModal = () => {
  logModalVisible.value = false
  if (logSocket.value) {
    logSocket.value.close()
    logSocket.value = null
  }
  currentInstallId.value = null
}

// 监听日志变化，自动滚动到底部
// （已在 WebSocket onmessage 中处理）

// 处理弹窗关闭
const handleLogModalClose = (show) => {
  if (!show) {
    closeLogModal()
  }
}

onMounted(() => {
  loadServers()
  loadPlugins()
})

// 组件卸载时关闭WebSocket
onUnmounted(() => {
  if (logSocket.value) {
    logSocket.value.close()
  }
})
</script>

<style scoped>
.plugin-store {
  padding: 0;
}

.plugin-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.plugin-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.plugin-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.plugin-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.plugin-description {
  font-size: 13px;
  color: #666;
  margin-bottom: 16px;
  min-height: 40px;
}

.plugin-actions {
  margin-top: auto;
}
</style>
