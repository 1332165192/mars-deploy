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
      :title="`SSH 控制台 - ${currentServer?.name || ''}`"
      preset="dialog"
      style="width: 90%; max-width: 1200px;"
      :show-icon="false"
      :closable="true"
      @after-leave="closeConsole"
    >
      <div class="terminal-container">
        <div ref="terminalRef" class="terminal"></div>
      </div>
      
      <template #action>
        <n-space>
          <n-button @click="closeConsole">关闭</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h, nextTick } from 'vue'
import { useMessage, NButton, NSpace, NTag, NBadge, NPopconfirm } from 'naive-ui'
import { SearchSharp, AddSharp, TerminalSharp, CheckmarkCircleSharp, CreateSharp, TrashSharp } from '@vicons/ionicons5'
import { getServerList, addServer, updateServer, deleteServer, testConnection } from '@/api/server'
import { Terminal } from 'xterm'
import { FitAddon } from 'xterm-addon-fit'
import 'xterm/css/xterm.css'

const message = useMessage()
const loading = ref(false)
const visible = ref(false)
const consoleVisible = ref(false)
const editId = ref(null)
const searchName = ref('')
const formRef = ref()
const testingId = ref(null)
const terminalRef = ref(null)
const currentServer = ref(null)

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
    width: 320,
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
  port: { 
    required: true, 
    type: 'number',
    message: '请输入端口', 
    trigger: ['blur', 'change'] 
  },
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
  currentServer.value = null
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
  height: 600px;
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
</style>
