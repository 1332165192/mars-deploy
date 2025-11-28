-- 数据库迁移脚本 - 添加自动部署功能

USE mars_deploy;

-- 1. 删除服务器表的启动和停止命令字段
ALTER TABLE t_server DROP COLUMN IF EXISTS start_cmd;
ALTER TABLE t_server DROP COLUMN IF EXISTS stop_cmd;

-- 2. 为项目表添加自动部署相关字段
ALTER TABLE t_project ADD COLUMN IF NOT EXISTS auto_deploy TINYINT DEFAULT 0 COMMENT '是否自动部署：0-否，1-是';
ALTER TABLE t_project ADD COLUMN IF NOT EXISTS deploy_script TEXT COMMENT '部署脚本';
ALTER TABLE t_project ADD COLUMN IF NOT EXISTS app_port INT DEFAULT 8080 COMMENT '应用端口';

-- 3. 创建项目服务器关联表（支持多服务器部署）
CREATE TABLE IF NOT EXISTS t_project_server (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    server_id BIGINT NOT NULL COMMENT '服务器ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_project_server (project_id, server_id),
    FOREIGN KEY (project_id) REFERENCES t_project(id) ON DELETE CASCADE,
    FOREIGN KEY (server_id) REFERENCES t_server(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目服务器关联表';

-- 4. 迁移已有的服务器关联数据
INSERT INTO t_project_server (project_id, server_id)
SELECT id, server_id FROM t_project WHERE server_id IS NOT NULL;

