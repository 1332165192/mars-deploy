package com.mars.deploy.service;

import com.mars.deploy.entity.Build;
import com.mars.deploy.entity.Project;
import com.mars.deploy.entity.Server;
import com.mars.deploy.utils.BuildUtils;
import com.mars.deploy.utils.GitUtils;
import com.mars.deploy.utils.SshUtils;
import com.mars.deploy.websocket.BuildLogSocket;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 构建执行器服务
 * 专门用于异步执行构建任务
 */
@Slf4j
@Service
public class BuildExecutorService {

    @Autowired
    private BuildService buildService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private ProjectServerService projectServerService;

    @Value("${mars.workspace:}")
    private String workspace;

    @Value("${mars.keep-workspace:false}")
    private Boolean keepWorkspace;

    /**
     * 异步执行构建任务
     */
    @Async("buildExecutor")
    public void executeBuild(Long buildId) {
        Build build = buildService.getById(buildId);
        Project project = projectService.getById(build.getProjectId());

        // 更新状态为运行中
        build.setStatus("RUNNING");
        build.setStartTime(LocalDateTime.now());
        buildService.updateById(build);

        StringBuilder logBuilder = new StringBuilder();

        try {
            // 日志输出回调
            java.util.function.Consumer<String> logConsumer = msg -> {
                logBuilder.append(msg).append("\n");
                BuildLogSocket.sendMessage(buildId.toString(), msg);
                log.info("[Build-{}] {}", buildId, msg);
            };

            logConsumer.accept("========================================");
            logConsumer.accept("开始构建项目: " + project.getName());
            logConsumer.accept("========================================");

            // 1. 准备工作目录（使用系统临时目录，避免文件锁定问题）
            File projectDir = createWorkspaceDir(project.getId(), logConsumer);

            try {

                // 2. 拉取代码
                logConsumer.accept("\n[步骤1] 拉取Git代码");
                GitUtils.cloneOrPull(project.getGitUrl(), project.getBranch(), project.getGitUsername(), project.getGitPassword(), projectDir, logConsumer);

                // 3. 清理旧的构建产物（解决文件被占用问题）
                logConsumer.accept("\n[步骤2] 清理旧的构建产物");
                BuildUtils.cleanBuildDirectory(projectDir, project.getBuildDir(), logConsumer);

                // 4. 执行构建
                logConsumer.accept("\n[步骤3] 执行构建命令");
                boolean buildSuccess = BuildUtils.executeCommand(project.getBuildCommand(), projectDir, logConsumer);

                if (!buildSuccess) {
                    throw new Exception("构建失败");
                }

                // 5. 查找构建产物
                logConsumer.accept("\n[步骤4] 查找构建产物");
                File artifact = BuildUtils.findBuildArtifact(projectDir, project.getBuildDir());
                logConsumer.accept("[Build] 找到构建产物: " + artifact.getAbsolutePath());

                // 6. 部署到服务器（支持多服务器）
                if (project.getAutoDeploy() != null && project.getAutoDeploy() == 1) {
                    logConsumer.accept("\n[步骤5] 部署到服务器");

                    // 获取项目关联的所有服务器
                    List<Long> serverIds = projectServerService.getProjectServerIds(project.getId());

                    if (serverIds == null || serverIds.isEmpty()) {
                        logConsumer.accept("[警告] 未配置服务器，跳过部署步骤");
                    } else {
                        logConsumer.accept("[Deploy] 共需部署到 " + serverIds.size() + " 台服务器");

                        int successCount = 0;
                        int failCount = 0;

                        List<Server> servers = serverService.listByIds(serverIds);

                        // 分组
                        Map<Long, Server> serverGroup = servers.stream().collect(Collectors.toMap(Server::getId, Function.identity()));

                        for (int i = 0; i < serverIds.size(); i++) {
                            Long serverId = serverIds.get(i);
                            // 删除服务器的时候 判断下是否和项目关联
                            Server server = serverGroup.get(serverId);
                            if (server == null) {
                                logConsumer.accept("\n[服务器" + (i + 1) + "/" + serverIds.size() + "] 服务器不存在(ID:" + serverId + ")，跳过");
                                failCount++;
                                continue;
                            }

                            try {
                                logConsumer.accept("\n[服务器" + (i + 1) + "/" + serverIds.size() + "] 开始部署到: " + server.getName() + " (" + server.getHost() + ")");
                                deployToServer(server, project, artifact, logConsumer);
                                logConsumer.accept("[服务器" + (i + 1) + "/" + serverIds.size() + "] 部署成功");
                                successCount++;
                            } catch (Exception e) {
                                logConsumer.accept("[服务器" + (i + 1) + "/" + serverIds.size() + "] 部署失败: " + e.getMessage());
                                failCount++;
                                log.error("部署到服务器失败: " + server.getHost(), e);
                            }
                        }

                        logConsumer.accept("\n[Deploy] 部署完成: 成功 " + successCount + " 台，失败 " + failCount + " 台");

                        // 如果所有服务器都部署失败，则抛出异常
                        if (failCount > 0 && successCount == 0) {
                            throw new Exception("所有服务器部署均失败");
                        }
                    }
                } else {
                    logConsumer.accept("\n[跳过] 未开启自动部署，跳过部署步骤");
                }

                // 构建成功
                logConsumer.accept("\n========================================");
                logConsumer.accept("构建成功！");
                logConsumer.accept("========================================");

                build.setStatus("SUCCESS");

            } finally {
                // 清理工作目录（除非配置了保留）
                if (!keepWorkspace) {
                    cleanupWorkspaceDir(projectDir, logConsumer);
                }
            }

        } catch (Exception e) {
            log.error("构建失败", e);
            logBuilder.append("\n[错误] ").append(e.getMessage()).append("\n");
            BuildLogSocket.sendMessage(buildId.toString(), "[错误] " + e.getMessage());

            build.setStatus("FAILED");
        } finally {
            // 更新构建记录
            build.setEndTime(LocalDateTime.now());
            build.setDuration(ChronoUnit.SECONDS.between(build.getStartTime(), build.getEndTime()));
            build.setLog(logBuilder.toString());
            buildService.updateById(build);
        }
    }

    /**
     * 创建工作目录
     */
    private File createWorkspaceDir(Long projectId, java.util.function.Consumer<String> logConsumer) {
        String baseDir;

        if (workspace != null && !workspace.isEmpty()) {
            // 使用配置的工作目录
            baseDir = workspace;
        } else {
            // 根据操作系统使用默认目录
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                // Windows: D:\mars-deploy
                baseDir = "D:\\mars-deploy";
            } else {
                // Linux/Mac: /home/mars-deploy
                baseDir = "/home/mars-deploy";
            }
        }

        String dirName = String.format("project-%d-%d", projectId, System.currentTimeMillis());
        File workDir = new File(baseDir, dirName);

        if (!workDir.exists()) {
            workDir.mkdirs();
        }

        logConsumer.accept("[Workspace] 工作目录: " + workDir.getAbsolutePath());
        return workDir;
    }

    /**
     * 清理工作目录
     */
    private void cleanupWorkspaceDir(File projectDir, java.util.function.Consumer<String> logConsumer) {
        if (projectDir == null || !projectDir.exists()) {
            return;
        }

        logConsumer.accept("\n[清理] 开始清理工作目录...");

        try {
            // Windows下可能文件被占用，重试3次
            int maxRetries = 3;
            boolean deleted = false;

            for (int i = 0; i < maxRetries && !deleted; i++) {
                try {
                    Files.walk(projectDir.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                    deleted = true;
                    logConsumer.accept("[清理] 工作目录清理成功");
                } catch (Exception e) {
                    if (i < maxRetries - 1) {
                        logConsumer.accept("[清理] 清理失败，等待重试... (" + (i + 1) + "/" + maxRetries + ")");
                        Thread.sleep(1000);
                    } else {
                        logConsumer.accept("[清理] 工作目录清理失败（可能被占用），将在下次构建时自动清理: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logConsumer.accept("[清理] 清理过程出错: " + e.getMessage());
        }
    }

    /**
     * 部署到服务器
     */
    private void deployToServer(Server server, Project project, File artifact, java.util.function.Consumer<String> logConsumer) throws Exception {

        logConsumer.accept("[Deploy] 连接服务器: " + server.getHost());

        Session session = null;
        try {
            // 创建SSH连接（需要传入 privateKey 参数）
            session = SshUtils.createSession(server.getHost(), server.getPort(), server.getUsername(), server.getPassword(), server.getPrivateKey());
            session.connect();
            logConsumer.accept("[Deploy] SSH连接成功");

            // 上传文件
            String uploadPath = "/home/deploy/" + project.getName();
            logConsumer.accept("[Deploy] 创建部署目录: " + uploadPath);
            SshUtils.executeCommand(session, "mkdir -p " + uploadPath, logConsumer);

            logConsumer.accept("[Deploy] 上传构建产物...");
            SshUtils.uploadFile(session, artifact, uploadPath, logConsumer);
            logConsumer.accept("[Deploy] 文件上传成功");

            // 执行部署脚本
            if (project.getDeployScript() != null && !project.getDeployScript().isEmpty()) {
                logConsumer.accept("[Deploy] 执行部署脚本...");

                // 替换脚本中的变量
                String script = project.getDeployScript().replace("{{uploadPath}}", uploadPath).replace("{{appPort}}", String.valueOf(project.getAppPort()));

                // 将脚本写入临时文件并执行
                String scriptPath = uploadPath + "/deploy.sh";
                SshUtils.executeCommand(session, "cat > " + scriptPath + " << 'EOF'\n" + script + "\nEOF", logConsumer);
                SshUtils.executeCommand(session, "chmod +x " + scriptPath, logConsumer);
                SshUtils.executeCommand(session, "bash " + scriptPath, logConsumer);

                logConsumer.accept("[Deploy] 部署脚本执行完成");
            }

        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
}
