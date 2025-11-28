-- 创建数据库
CREATE DATABASE IF NOT EXISTS mars_deploy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mars_deploy;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS t_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS t_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES t_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 项目成员表
CREATE TABLE IF NOT EXISTS t_project_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_type VARCHAR(20) DEFAULT 'MEMBER' COMMENT '角色类型：OWNER-拥有者，DEVELOPER-开发者，MEMBER-成员',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_project_user (project_id, user_id),
    FOREIGN KEY (project_id) REFERENCES t_project(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员表';

-- 服务器表
CREATE TABLE IF NOT EXISTS t_server (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '服务器名称',
    host VARCHAR(100) NOT NULL COMMENT '服务器地址',
    port INT DEFAULT 22 COMMENT '端口',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    auth_type VARCHAR(20) DEFAULT 'PASSWORD' COMMENT '认证类型：PASSWORD/SSH_KEY',
    password VARCHAR(255) COMMENT '密码',
    private_key TEXT COMMENT 'SSH私钥',
    upload_path VARCHAR(255) DEFAULT '/opt/deploy' COMMENT '上传目录',
    status VARCHAR(20) DEFAULT 'OFFLINE' COMMENT '状态：ONLINE/OFFLINE',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务器表';

-- 项目表
CREATE TABLE IF NOT EXISTS t_project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '项目名称',
    description VARCHAR(500) COMMENT '项目描述',
    git_url VARCHAR(500) NOT NULL COMMENT 'Git地址',
    branch VARCHAR(100) DEFAULT 'master' COMMENT '分支',
    git_username VARCHAR(100) COMMENT 'Git用户名',
    git_password VARCHAR(255) COMMENT 'Git密码',
    project_type VARCHAR(20) NOT NULL COMMENT '项目类型：JAVA/VUE',
    build_command TEXT NOT NULL COMMENT '构建命令',
    build_dir VARCHAR(255) NOT NULL COMMENT '产物路径',
    server_id BIGINT COMMENT '关联服务器ID',
    auto_deploy TINYINT DEFAULT 0 COMMENT '是否自动部署：0-否，1-是',
    deploy_script TEXT COMMENT '部署脚本',
    app_port INT DEFAULT 8080 COMMENT '应用端口',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    FOREIGN KEY (server_id) REFERENCES t_server(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 构建记录表
CREATE TABLE IF NOT EXISTS t_build (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/RUNNING/SUCCESS/FAILED',
    log LONGTEXT COMMENT '构建日志',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration BIGINT COMMENT '耗时（秒）',
    trigger_by VARCHAR(50) COMMENT '触发人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    FOREIGN KEY (project_id) REFERENCES t_project(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='构建记录表';

-- 系统日志表
CREATE TABLE IF NOT EXISTS t_system_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(100) COMMENT '操作',
    method VARCHAR(200) COMMENT '方法',
    params TEXT COMMENT '参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- 插入默认管理员账号（密码：admin123）
INSERT INTO t_user (username, password, nickname, status) 
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 1);

-- 插入默认角色
INSERT INTO t_role (name, code, description) VALUES
('系统管理员', 'ADMIN', '拥有系统所有权限'),
('项目管理员', 'PROJECT_ADMIN', '管理所有项目'),
('开发者', 'DEVELOPER', '可以查看和触发构建'),
('普通用户', 'USER', '只能查看分配给自己的项目');

-- 菜单表
CREATE TABLE IF NOT EXISTS t_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID，0表示顶级菜单',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path VARCHAR(200) COMMENT '菜单路径',
    icon VARCHAR(50) COMMENT '菜单图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    menu_type VARCHAR(20) DEFAULT 'MENU' COMMENT '菜单类型：MENU-菜单，BUTTON-按钮',
    permission VARCHAR(100) COMMENT '权限标识',
    visible TINYINT DEFAULT 1 COMMENT '是否可见：1-是，0-否',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS t_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_menu (role_id, menu_id)
);

-- 插入默认菜单
INSERT INTO t_menu (id, parent_id, name, path, icon, sort_order, menu_type) VALUES
(1, 0, '仪表盘', '/dashboard', 'SpeedometerSharp', 1, 'MENU'),
(2, 0, '项目管理', '/project', 'FolderOpenSharp', 2, 'MENU'),
(3, 0, '服务器管理', '/server', 'ServerSharp', 3, 'MENU'),
(4, 0, '构建任务', '/build', 'RocketSharp', 4, 'MENU'),
(5, 0, '系统管理', 'system', 'ShieldCheckmarkSharp', 5, 'MENU'),
(6, 5, '用户管理', '/user', 'PeopleSharp', 1, 'MENU'),
(7, 5, '角色管理', '/role', 'ShieldCheckmarkSharp', 2, 'MENU'),
(8, 5, '菜单管理', '/menu', 'MenuSharp', 3, 'MENU');

-- 为管理员分配所有菜单
INSERT INTO t_role_menu (role_id, menu_id)
SELECT 1, id FROM t_menu WHERE deleted = 0;

-- 为项目管理员分配菜单（除了系统管理）
INSERT INTO t_role_menu (role_id, menu_id)
SELECT 2, id FROM t_menu WHERE id IN (1, 2, 3, 4) AND deleted = 0;

-- 为开发者分配菜单
INSERT INTO t_role_menu (role_id, menu_id)
SELECT 3, id FROM t_menu WHERE id IN (1, 2, 4) AND deleted = 0;

-- 为普通用户分配菜单
INSERT INTO t_role_menu (role_id, menu_id)
SELECT 4, id FROM t_menu WHERE id IN (1, 2) AND deleted = 0;

-- 为管理员分配角色
INSERT INTO t_user_role (user_id, role_id) VALUES (1, 1);
