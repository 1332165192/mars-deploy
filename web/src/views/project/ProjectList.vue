<template>
  <div class="project-list">
    <div class="toolbar">
      <n-space>
        <n-input
          v-model:value="searchName"
          placeholder="搜索项目名称"
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
        <n-button  type="primary" @click="showModal()">
          <template #icon>
            <n-icon><AddSharp /></n-icon>
          </template>
          新增项目
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
      :title="editId ? '编辑项目' : '新增项目'"
      preset="dialog"
      style="width: 800px"
      :show-icon="false"
    >
      <n-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        label-placement="left"
        label-width="110"
      >
        <n-form-item label="项目名称" path="name">
          <n-input v-model:value="formState.name" placeholder="请输入项目名称" />
        </n-form-item>
        
        <n-form-item label="项目描述" path="description">
          <n-input v-model:value="formState.description" type="textarea" :rows="3" placeholder="请输入项目描述" />
        </n-form-item>
        
        <n-form-item label="Git地址" path="gitUrl">
          <n-input v-model:value="formState.gitUrl" placeholder="https://github.com/xxx/xxx.git" />
        </n-form-item>
        
        <n-form-item label="分支" path="branch">
          <n-input v-model:value="formState.branch" placeholder="master" />
        </n-form-item>
        
        <n-form-item label="Git用户名" path="gitUsername">
          <n-input v-model:value="formState.gitUsername" placeholder="请输入Git用户名（可选）" />
        </n-form-item>
        
        <n-form-item label="Git密码" path="gitPassword">
          <n-input v-model:value="formState.gitPassword" type="password" show-password-on="click" placeholder="请输入Git密码（可选）" />
        </n-form-item>
        
        <n-form-item label="项目类型" path="projectType">
          <n-select 
            v-model:value="formState.projectType" 
            :options="projectTypeOptions" 
            placeholder="请选择项目类型"
            @update:value="handleProjectTypeChange"
          />
        </n-form-item>
        
        <n-form-item label="构建命令" path="buildCommand">
          <n-select
            v-model:value="formState.buildCommand"
            :options="buildCommandOptions"
            tag
            filterable
            placeholder="请选择或自定义构建命令"
          />
        </n-form-item>
        
        <n-form-item label="产物路径" path="buildDir">
          <n-select
            v-model:value="formState.buildDir"
            :options="buildDirOptions"
            tag
            filterable
            placeholder="请选择或自定义产物路径"
          />
        </n-form-item>
        
        <n-form-item label="部署服务器" path="serverIds">
          <n-select 
            v-model:value="formState.serverIds" 
            :options="serverOptions" 
            multiple
            clearable 
            placeholder="请选择部署服务器（可多选）" 
          />
        </n-form-item>
        
        <n-form-item label="自动部署" path="autoDeploy">
          <n-radio-group v-model:value="formState.autoDeploy" @update:value="handleAutoDeployChange">
            <n-radio :value="1">是</n-radio>
            <n-radio :value="0">否</n-radio>
          </n-radio-group>
        </n-form-item>
        
        <n-form-item v-if="formState.autoDeploy === 1" label="应用端口" path="appPort">
          <n-input-number v-model:value="formState.appPort" :min="1" :max="65535" placeholder="8080" style="width: 100%" />
        </n-form-item>
        
        <n-form-item v-if="formState.autoDeploy === 1" label="部署目录" path="deployPath">
          <n-input v-model:value="formState.deployPath" placeholder="/home/deploy/" />
          <template #feedback>
            <n-text depth="3" style="font-size: 12px;">服务器上存放应用的目录，默认为 /home/deploy/</n-text>
          </template>
        </n-form-item>
        
        <n-form-item v-if="formState.autoDeploy === 1" label="部署脚本" path="deployScript">
          <n-input
            v-model:value="formState.deployScript"
            type="textarea"
            :rows="8"
            placeholder="部署脚本将自动生成，也可自定义"
          />
        </n-form-item>
      </n-form>
      
      <template #action>
        <n-space>
          <n-button @click="visible = false">取消</n-button>
          <n-button type="primary" @click="handleOk">确定</n-button>
        </n-space>
      </template>
    </n-modal>
    
    <!-- 成员管理弹窗 -->
    <n-modal v-model:show="memberVisible" title="项目成员管理" preset="dialog" style="width: 700px" :show-icon="false">
      <n-space vertical>
        <n-button type="primary" size="small" @click="addMemberRow">
          <template #icon><n-icon><AddSharp /></n-icon></template>
          添加成员
        </n-button>
        
        <n-data-table :columns="memberColumns" :data="projectMembers" :pagination="false" />
      </n-space>
      
      <template #action>
        <n-space>
          <n-button @click="memberVisible = false">取消</n-button>
          <n-button type="primary" @click="saveMember">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, NButton, NSpace, NTag, NPopconfirm, NSelect } from 'naive-ui'
import { SearchSharp, AddSharp, RocketSharp, CreateSharp, TrashSharp, PeopleSharp, PlayCircleSharp } from '@vicons/ionicons5'
import { getProjectList, getProject, addProject, updateProject, deleteProject, getProjectMembers, assignProjectMembers } from '@/api/project'
import { getServerList } from '@/api/server'
import { triggerBuild } from '@/api/build'
import { getCurrentUser, getUserList } from '@/api/user'

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const visible = ref(false)
const memberVisible = ref(false)
const currentProjectId = ref(null)
const editId = ref(null)
const searchName = ref('')
const formRef = ref()

const dataSource = ref([])
const servers = ref([])
const userPermissions = ref([])
const allUsers = ref([])
const projectMembers = ref([])

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const projectTypeOptions = [
  { label: 'Java', value: 'JAVA' },
  { label: 'Vue', value: 'VUE' }
]

// 构建命令选项（根据项目类型动态变化）
const buildCommandOptions = ref([
  { label: 'mvn clean package -DskipTests', value: 'mvn clean package -DskipTests' },
  { label: 'mvn clean package', value: 'mvn clean package' },
  { label: 'mvn clean install -DskipTests', value: 'mvn clean install -DskipTests' },
  { label: 'gradle build', value: 'gradle build' },
  { label: 'gradle build -x test', value: 'gradle build -x test' }
])

// 产物路径选项
const buildDirOptions = [
  { label: 'target/*.jar', value: 'target/*.jar' },
  { label: 'target/*.war', value: 'target/*.war' },
  { label: 'build/libs/*.jar', value: 'build/libs/*.jar' },
  { label: 'dist', value: 'dist' },
  { label: 'build', value: 'build' },
  { label: 'out', value: 'out' }
]

const serverOptions = ref([])

// 权限计算
const isAdmin = computed(() => userPermissions.value.includes('ADMIN'))
const isProjectAdmin = computed(() => 
  userPermissions.value.includes('ADMIN') || userPermissions.value.includes('PROJECT_ADMIN')
)
const canManageProject = computed(() => isProjectAdmin.value)

const columns = [
  { title: 'ID', key: 'id',width: 60 },
  { title: '项目名称', key: 'name' },
  { 
    title: '项目类型', 
    key: 'projectType', 
    render: (row) => {
      return h(
        NTag,
        { type: row.projectType === 'JAVA' ? 'info' : 'success', size: 'small' },
        { default: () => row.projectType }
      )
    }
  },
  { title: 'Git地址', key: 'gitUrl', ellipsis: { tooltip: true } },
  { title: '分支', key: 'branch' },
  { title: '创建时间', key: 'createTime' },
  { 
    title: '操作', 
    key: 'action', 
    width: 350,
    render: (row) => {
      const buttons = [

      ]
      
      // 触发构建按钮 - 管理员、项目管理员、开发者可见
      if (isProjectAdmin.value || userPermissions.value.includes('DEVELOPER')) {
        buttons.push(
          h(
            NPopconfirm,
            {
              onPositiveClick: () => triggerBuildAction(row.id)
            },
            {
              default: () => '确定要部署该项目吗？',
              trigger: () => h(
                NButton,
                {
                  text: true,
                  type: 'info',
                  size: 'small'
                },
                { 
                  default: () => '部署项目',
                  icon: () => h('i', { class: 'n-icon' }, [h(PlayCircleSharp)])
                }
              )
            }
          )
        )
      }

      // 编辑按钮 - 管理员、项目管理员可见
      if (canManageProject.value) {
        buttons.push(
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
            NButton,
            {
              text: true,
              type: 'warning',
              size: 'small',
              onClick: () => showMemberModal(row.id)
            },
            { 
              default: () => '分配成员',
              icon: () => h('i', { class: 'n-icon' }, [h(PeopleSharp)])
            }
          )
        )
      }
      
      // 删除按钮 - 管理员、项目管理员可见
      if (canManageProject.value) {
        buttons.push(
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
        )
      }
      
      return h(NSpace, {}, { default: () => buttons })
    }
  }
]

const formState = reactive({
  name: '',
  description: '',
  gitUrl: '',
  branch: 'master',
  gitUsername: '',
  gitPassword: '',
  projectType: 'JAVA',
  buildCommand: '',
  buildDir: '',
  serverIds: [],
  autoDeploy: 0,
  deployScript: '',
  deployPath: '/home/deploy/',
  appPort: 8080
})

const rules = {
  name: { required: true, message: '请输入项目名称', trigger: 'blur' },
  gitUrl: { required: true, message: '请输入Git地址', trigger: 'blur' },
  projectType: { required: true, message: '请选择项目类型', trigger: 'change' },
  buildCommand: { required: true, message: '请输入构建命令', trigger: 'blur' },
  buildDir: { required: true, message: '请输入产物路径', trigger: 'blur' }
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
    
    const data = await getProjectList(params)
    
    dataSource.value = data.records
    pagination.itemCount = data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadServers = async () => {
  try {
    const data = await getServerList({ current: 1, size: 100 })
    servers.value = data.records
    serverOptions.value = data.records.map(server => ({
      label: server.name,
      value: server.id
    }))
  } catch (error) {
    console.error(error)
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

const showModal = async (record) => {
  if (record) {
    editId.value = record.id
    
    // 获取项目详情和服务器ID列表
    try {
      const response = await getProject(record.id)
      const project = response.project
      const serverIds = response.serverIds || []
      
      Object.assign(formState, {
        name: project.name || '',
        description: project.description || '',
        gitUrl: project.gitUrl || '',
        branch: project.branch || 'master',
        gitUsername: project.gitUsername || '',
        gitPassword: project.gitPassword || '',
        projectType: project.projectType || 'JAVA',
        buildCommand: project.buildCommand || '',
        buildDir: project.buildDir || '',
        serverIds: serverIds,
        autoDeploy: project.autoDeploy != null ? project.autoDeploy : 0,
        deployScript: project.deployScript || generateDefaultDeployScript(project.projectType),
        deployPath: project.deployPath || '/home/deploy/',
        appPort: project.appPort || 8080
      })
    } catch (error) {
      console.error('获取项目详情失败:', error)
      message.error('获取项目详情失败')
    }
  } else {
    editId.value = null
    Object.assign(formState, {
      name: '',
      description: '',
      gitUrl: '',
      branch: 'master',
      gitUsername: '',
      gitPassword: '',
      projectType: 'JAVA',
      buildCommand: '',
      buildDir: '',
      serverIds: [],
      autoDeploy: 0,
      deployScript: generateDefaultDeployScript('JAVA'),
      deployPath: '/home/deploy/',
      appPort: 8080
    })
  }
  visible.value = true
}

const handleOk = async () => {
  try {
    await formRef.value?.validate()
    
    const submitData = {
      ...formState,
      serverIds: formState.serverIds || [],
      autoDeploy: formState.autoDeploy || 0,
      deployPath: formState.deployPath || '/home/deploy/',
      appPort: formState.appPort || 8080
    }
    
    if (editId.value) {
      submitData.id = editId.value
      await updateProject(submitData)
      message.success('更新成功')
    } else {
      await addProject(submitData)
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
    await deleteProject(id)
    message.success('删除成功')
    loadData()
  } catch (error) {
    console.error(error)
  }
}

const viewBuilds = (projectId) => {
  router.push(`/build/${projectId}`)
}

// 处理项目类型变化，动态更新构建命令选项和部署脚本
const handleProjectTypeChange = (value) => {
  if (value === 'JAVA') {
    buildCommandOptions.value = [
      { label: 'mvn clean package -DskipTests', value: 'mvn clean package -DskipTests' },
      { label: 'mvn clean package', value: 'mvn clean package' },
      { label: 'mvn clean install -DskipTests', value: 'mvn clean install -DskipTests' },
      { label: 'mvn clean install', value: 'mvn clean install' },
      { label: 'gradle build -x test', value: 'gradle build -x test' },
      { label: 'gradle build', value: 'gradle build' },
      { label: 'gradle bootJar', value: 'gradle bootJar' }
    ]
    // 如果当前构建命令为空，设置默认值
    if (!formState.buildCommand) {
      formState.buildCommand = 'mvn clean package -DskipTests'
    }
  } else if (value === 'VUE') {
    buildCommandOptions.value = [
      { label: 'npm install && npm run build', value: 'npm install && npm run build' },
      { label: 'npm run build', value: 'npm run build' },
      { label: 'yarn install && yarn build', value: 'yarn install && yarn build' },
      { label: 'yarn build', value: 'yarn build' },
      { label: 'pnpm install && pnpm build', value: 'pnpm install && pnpm build' },
      { label: 'pnpm build', value: 'pnpm build' }
    ]
    // 如果当前构建命令为空，设置默认值
    if (!formState.buildCommand) {
      formState.buildCommand = 'npm install && npm run build'
    }
  }
  
  // 更新部署脚本
  if (formState.autoDeploy === 1) {
    formState.deployScript = generateDefaultDeployScript(value)
  }
}

// 处理自动部署选项变化
const handleAutoDeployChange = (value) => {
  if (value === 1) {
    // 开启自动部署时，根据项目类型生成部署脚本
    formState.deployScript = generateDefaultDeployScript(formState.projectType)
  }
}

// 生成默认部署脚本（根据项目类型）
const generateDefaultDeployScript = (projectType) => {
  const type = projectType || formState.projectType
  
  if (type === 'VUE') {
    // Vue 项目部署脚本（直接上传静态文件）
    return `#!/bin/bash
# Vue 项目部署脚本

DEPLOY_DIR="{{uploadPath}}"
WEB_ROOT="/www/wwwroot"
PROJECT_NAME="{{projectName}}"
TARGET_DIR="\$WEB_ROOT/\$PROJECT_NAME"
BACKUP_DIR="\$WEB_ROOT/backup"

echo "========================================"
echo "开始部署Vue项目"
echo "========================================"

# 1. 备份旧版本
echo "[步骤1] 备份旧版本..."
if [ -d "\$TARGET_DIR" ]; then
  mkdir -p \$BACKUP_DIR
  BACKUP_NAME="\${PROJECT_NAME}_\$(date +%Y%m%d_%H%M%S)"
  mv \$TARGET_DIR \$BACKUP_DIR/\$BACKUP_NAME
  echo "已备份为: \$BACKUP_NAME"
  
  # 保留最近3个备份
  ls -t \$BACKUP_DIR | grep "^\${PROJECT_NAME}_" | tail -n +4 | xargs -I {} rm -rf \$BACKUP_DIR/{}
fi

# 2. 部署新版本
echo "[步骤2] 部署新版本..."
mkdir -p \$TARGET_DIR

# 检查是否为压缩包
if [ -f "\$DEPLOY_DIR/dist.zip" ]; then
  echo "检测到 dist.zip 文件"
  # 先解压到临时目录
  cd \$DEPLOY_DIR
  unzip -o -q dist.zip
  
  # 检查解压后的结构
  if [ -d "\$DEPLOY_DIR/dist" ]; then
    # 解压后有 dist 目录，复制其内容
    cp -r \$DEPLOY_DIR/dist/* \$TARGET_DIR/
    echo "已解压 dist.zip 并复制到 \$TARGET_DIR"
  else
    # 解压后直接是文件，移动到目标目录
    mv \$DEPLOY_DIR/* \$TARGET_DIR/ 2>/dev/null || true
    echo "已解压 dist.zip 到 \$TARGET_DIR"
  fi
elif [ -f "\$DEPLOY_DIR/dist.tar.gz" ]; then
  tar -xzf \$DEPLOY_DIR/dist.tar.gz -C \$DEPLOY_DIR
  cp -r \$DEPLOY_DIR/dist/* \$TARGET_DIR/
  echo "已解压 dist.tar.gz 到 \$TARGET_DIR"
elif [ -d "\$DEPLOY_DIR/dist" ]; then
  cp -r \$DEPLOY_DIR/dist/* \$TARGET_DIR/
  echo "已复制 dist 目录到 \$TARGET_DIR"
else
  echo "错误: 未找到构建产物（dist目录或压缩包）"
  exit 1
fi

# 3. 设置文件权限
echo "[步骤3] 设置文件权限..."
chmod -R 755 \$TARGET_DIR

echo "========================================"
echo "部署完成！"
echo "部署目录: \$TARGET_DIR"
echo "访问路径: http://your-domain/\$PROJECT_NAME"
echo "========================================"`
  } else {
    // Java 项目部署脚本（启动 jar 包）
    return `#!/bin/bash
# Java 项目部署脚本

APP_NAME="app"
APP_PORT={{appPort}}
DEPLOY_DIR="{{uploadPath}}"
LOG_FILE="\$DEPLOY_DIR/app.log"

echo "========================================"
echo "开始部署Java应用"
echo "========================================"

# 1. 停止旧进程
echo "[步骤1] 正在停止旧进程..."
PID=\$(lsof -t -i:\$APP_PORT 2>/dev/null)
if [ ! -z "\$PID" ]; then
  kill -15 \$PID
  sleep 3
  if ps -p \$PID > /dev/null 2>&1; then
    kill -9 \$PID
    echo "已强制停止进程: \$PID"
  else
    echo "已优雅停止进程: \$PID"
  fi
else
  echo "未找到运行中的进程"
fi

sleep 2

# 2. 备份旧版本
echo "[步骤2] 备份旧版本..."
if [ -f "\$DEPLOY_DIR/app.jar" ]; then
  BACKUP_NAME="app_\$(date +%Y%m%d_%H%M%S).jar.bak"
  mv \$DEPLOY_DIR/app.jar \$DEPLOY_DIR/\$BACKUP_NAME
  echo "已备份为: \$BACKUP_NAME"
  ls -t \$DEPLOY_DIR/*.jar.bak 2>/dev/null | tail -n +4 | xargs rm -f 2>/dev/null
fi

# 3. 重命名新上传的jar
echo "[步骤3] 准备新版本..."
# 备份后重新查找 jar 文件（排除 .bak 文件）
JAR_FILE=\$(ls \$DEPLOY_DIR/*.jar 2>/dev/null | grep -v '\\.bak\$' | head -n 1)

if [ -f "\$JAR_FILE" ]; then
  cp \$JAR_FILE \$DEPLOY_DIR/app.jar
  echo "已准备新版本: app.jar"
  echo "原文件: \$(basename \$JAR_FILE)"
else
  echo "错误: 未找到jar文件"
  exit 1
fi

# 4. 启动新应用
echo "[步骤4] 正在启动应用..."
cd \$DEPLOY_DIR

# 清空旧日志
> \$LOG_FILE

# 后台启动应用
nohup java -jar -Xms512m -Xmx1024m -Dserver.port=\$APP_PORT app.jar > \$LOG_FILE 2>&1 &
NEW_PID=\$!

echo "应用已启动，PID: \$NEW_PID"
echo "日志文件: \$LOG_FILE"

# 5. 等待应用启动并实时显示日志
echo "[步骤5] 等待应用启动..."
echo "----------------------------------------"
echo "应用启动日志："
echo "----------------------------------------"

# 实时显示日志并检测启动成功
START_TIME=\$(date +%s)
TIMEOUT=60
STARTED=false

# 使用 tail -f 实时显示日志，同时检测启动状态
(
  tail -f \$LOG_FILE &
  TAIL_PID=\$!
  
  while true; do
    CURRENT_TIME=\$(date +%s)
    ELAPSED=\$((CURRENT_TIME - START_TIME))
    
    # 检查超时
    if [ \$ELAPSED -gt \$TIMEOUT ]; then
      kill \$TAIL_PID 2>/dev/null
      echo ""
      echo "----------------------------------------"
      echo "警告: 应用启动超时（60秒）"
      echo "请检查日志文件: \$LOG_FILE"
      exit 1
    fi
    
    # 检查端口是否已监听
    if lsof -t -i:\$APP_PORT > /dev/null 2>&1; then
      sleep 2
      kill \$TAIL_PID 2>/dev/null
      STARTED=true
      break
    fi
    
    # 检查进程是否还在运行
    if ! ps -p \$NEW_PID > /dev/null 2>&1; then
      kill \$TAIL_PID 2>/dev/null
      echo ""
      echo "----------------------------------------"
      echo "错误: 应用进程已退出"
      echo "请检查日志文件: \$LOG_FILE"
      exit 1
    fi
    
    sleep 1
  done
  
  if [ "\$STARTED" = true ]; then
    echo ""
    echo "----------------------------------------"
    echo "应用启动成功！"
    echo "========================================"
    echo "部署完成！"
    echo "应用端口: \$APP_PORT"
    echo "应用PID: \$NEW_PID"
    echo "日志文件: \$LOG_FILE"
    echo "========================================"
    exit 0
  fi
) || exit 1`
  }
}

const triggerBuildAction = async (projectId) => {
  try {
    const data = await triggerBuild(projectId)
    message.success('构建任务已创建，正在跳转...')
    // 跳转到构建详情页，实时查看构建日志
    router.push(`/build-detail/${data.buildId}`)
  } catch (error) {
    console.error(error)
    message.error('部署失败')
  }
}

const loadUserPermissions = async () => {
  try {
    const data = await getCurrentUser()
    userPermissions.value = data.permissions || []
  } catch (error) {
    console.error('加载用户权限失败', error)
  }
}

// 成员管理相关
const memberColumns = [
  { 
    title: '用户', 
    key: 'userId',
    render: (row, index) => {
      const userOptions = allUsers.value.map(u => ({
        label: `${u.nickname || u.username} (${u.username})`,
        value: u.id
      }))
      return h(
        NSelect,
        {
          value: row.userId,
          options: userOptions,
          placeholder: '请选择用户',
          style: { width: '250px' },
          onUpdateValue: (value) => {
            projectMembers.value[index].userId = value
          }
        }
      )
    }
  },
  { 
    title: '角色', 
    key: 'roleType',
    render: (row, index) => {
      const roleOptions = [
        { label: '开发者', value: 'DEVELOPER' },
        { label: '成员', value: 'MEMBER' }
      ]
      return h(
        NSelect,
        {
          value: row.roleType,
          options: roleOptions,
          style: { width: '120px' },
          onUpdateValue: (value) => {
            projectMembers.value[index].roleType = value
          }
        }
      )
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render: (row, index) => {
      // 不能删除拥有者
      if (row.roleType === 'OWNER') {
        return h(NTag, { type: 'success' }, { default: () => '拥有者' })
      }
      return h(
        NButton,
        {
          text: true,
          type: 'error',
          size: 'small',
          onClick: () => removeMemberRow(index)
        },
        { default: () => '移除' }
      )
    }
  }
]

const showMemberModal = async (projectId) => {
  currentProjectId.value = projectId
  
  try {
    // 加载所有用户
    const userData = await getUserList({ current: 1, size: 100 })
    allUsers.value = userData.records
    
    // 加载项目成员
    const members = await getProjectMembers(projectId)
    projectMembers.value = members.map(m => ({
      userId: m.userId,
      roleType: m.roleType,
      user: m.user
    }))
    
    memberVisible.value = true
  } catch (error) {
    message.error('加载成员列表失败')
  }
}

const addMemberRow = () => {
  projectMembers.value.push({
    userId: null,
    roleType: 'MEMBER'
  })
}

const removeMemberRow = (index) => {
  projectMembers.value.splice(index, 1)
}

const saveMember = async () => {
  try {
    // 过滤掉拥有者和空的成员
    const members = projectMembers.value
      .filter(m => m.userId && m.roleType !== 'OWNER')
      .map(m => ({
        userId: m.userId,
        roleType: m.roleType
      }))
    
    await assignProjectMembers(currentProjectId.value, members)
    message.success('保存成功')
    memberVisible.value = false
  } catch (error) {
    message.error('保存失败')
  }
}

onMounted(() => {
  loadUserPermissions()
  loadData()
  loadServers()
})
</script>

<style scoped>
.project-list {
  width: 100%;
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
}
</style>
