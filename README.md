# MarsDeploy - 轻量级 CICD 自动化部署平台

## 项目简介

MarsDeploy 是一套用于 Java / Vue 项目的自动构建、自动上传、自动部署的平台。提供可视化项目管理、服务器管理、构建任务、实时日志等能力。

## 系统架构

- **前端**：Vue3 + Vite + Ant Design + @vicons/ionicons5
- **后端**：Spring Boot 3 + MyBatis-Plus + Sa-Token
- **数据库**：MySQL 8.0
- **其他**：WebSocket、JGit、JSch

## 核心功能

### 1. 登录权限
- JWT / Sa-Token 鉴权
- 用户权限控制

### 2. 项目管理
- 项目新增/编辑
- Git 地址配置
- 分支管理
- 构建命令配置（mvn / npm）
- 产物路径配置
- 绑定服务器

### 3. 构建任务管理
- 一键触发构建
- 构建队列
- 实时日志 WebSocket
- 构建结果记录
- 构建历史列表

### 4. 服务器管理
- 主机管理
- SSH 连接测试
- 部署目录
- 停止、启动命令
- 密码/SSH Key 登录

### 5. 自动化部署流程
- Git 自动拉取
- 自动构建（Java / Vue）
- 产物收集
- SFTP 上传
- 执行部署脚本
- 状态回写

## 快速开始

### 环境要求

- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 后端启动

```bash
# 1. 导入数据库
mysql -u root -p < src/main/resources/sql/schema.sql

# 2. 修改配置文件
# 编辑 src/main/resources/application.yml
# 修改数据库连接信息

# 3. 启动后端
mvn clean package
java -jar target/mars-deploy-1.0.0.jar
```

### 前端启动

```bash
cd web

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

### Docker 部署

```bash
# 1. 构建后端
mvn clean package

# 2. 构建前端
cd web
npm install
npm run build
cd ..

# 3. 启动 Docker Compose
docker-compose up -d
```

访问地址：http://localhost

## 默认账号

- 用户名：admin
- 密码：admin123

## 目录结构

```
mars-deploy/
├── src/                        # 后端源码
│   ├── main/
│   │   ├── java/
│   │   │   └── com/mars/deploy/
│   │   │       ├── config/     # 配置类
│   │   │       ├── controller/ # 控制器
│   │   │       ├── entity/     # 实体类
│   │   │       ├── mapper/     # Mapper
│   │   │       ├── service/    # 服务层
│   │   │       ├── utils/      # 工具类
│   │   │       └── websocket/  # WebSocket
│   │   └── resources/
│   │       ├── sql/            # SQL脚本
│   │       └── application.yml # 配置文件
├── web/                        # 前端源码
│   ├── src/
│   │   ├── api/                # API接口
│   │   ├── router/             # 路由
│   │   ├── views/              # 页面
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── Dockerfile                  # Docker 镜像
├── docker-compose.yml          # Docker Compose
├── nginx.conf                  # Nginx 配置
└── pom.xml                     # Maven 配置
```

## 技术选型

### 后端
- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- Sa-Token 1.37.0
- JGit 6.8.0
- JSch 0.2.16
- Hutool 5.8.25

### 前端
- Vue 3.4.0
- Vite 5.0.0
- Ant Design Vue 4.1.0
- Axios 1.6.2
- @vicons/ionicons5 0.12.0

## 使用说明

### 1. 添加服务器

进入"服务器管理"，点击"新增服务器"，填写服务器信息：
- 服务器名称
- 主机地址和端口
- 用户名和密码（或 SSH Key）
- 上传目录
- 启动/停止命令

点击"测试连接"确保服务器可以正常连接。

### 2. 创建项目

进入"项目管理"，点击"新增项目"，填写项目信息：
- 项目名称
- Git 地址和分支
- Git 认证信息（如需要）
- 项目类型（Java/Vue）
- 构建命令（如：mvn clean package -DskipTests）
- 产物路径（如：target/*.jar）
- 选择部署服务器

### 3. 触发构建

在项目列表中点击"触发构建"，系统将自动：
1. 拉取 Git 代码
2. 执行构建命令
3. 收集构建产物
4. 上传到服务器
5. 执行部署脚本

### 4. 查看日志

点击"查看详情"可以实时查看构建日志，通过 WebSocket 实时推送。

## 常见问题

### 1. Git 克隆失败
- 检查 Git 地址是否正确
- 检查网络连接
- 如果是私有仓库，需要配置认证信息

### 2. SSH 连接失败
- 检查服务器地址和端口
- 检查用户名和密码
- 检查防火墙设置

### 3. 构建失败
- 检查构建命令是否正确
- 检查项目依赖是否完整
- 查看详细日志定位问题

## License

MIT License

## 联系方式

如有问题，欢迎提 Issue。
