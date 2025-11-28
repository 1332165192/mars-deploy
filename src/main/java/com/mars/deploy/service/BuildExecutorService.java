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
                GitUtils.cloneOrPull(
                        project.getGitUrl(),
                        project.getBranch(),
                        project.getGitUsername(),
                        project.getGitPassword(),
                        projectDir,
                        logConsumer
                );

                // 3. 清理旧的构建产物（解决文件被占用问题）
                logConsumer.accept("\n[步骤2] 清理旧的构建产物");
                BuildUtils.cleanBuildDirectory(projectDir, project.getBuildDir(), logConsumer);

                // 4. 执行构建
                logConsumer.accept("\n[步骤3] 执行构建命令");
                boolean buildSuccess = BuildUtils.executeCommand(
                        project.getBuildCommand(),
                        projectDir,
                        logConsumer
                );

                if (!buildSuccess) {
                    throw new Exception("构建失败");
                }

                // 5. 查找构建产物
                logConsumer.accept("\n[步骤4] 查找构建产物");
                File artifact = BuildUtils.findBuildArtifact(projectDir, project.getBuildDir());
                logConsumer.accept("[Build] 找到构建产物: " + artifact.getAbsolutePath());

                // 6. 部署到服务器
                if (project.getServerId() != null && project.getAutoDeploy() != null && project.getAutoDeploy() == 1) {
                    logConsumer.accept("\n[步骤5] 部署到服务器");
                    Server server = serverService.getById(project.getServerId());

                    if (server == null) {
                        logConsumer.accept("[警告] 未配置服务器，跳过部署步骤");
                    } else {
                        deployToServer(server, project, artifact, logConsumer);
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
                    Files.walk(projectDir.toPath())
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
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
    private void deployToServer(Server server, Project project, File artifact,
                                 java.util.function.Consumer<String> logConsumer) throws Exception {

        logConsumer.accept("[Deploy] 连接服务器: " + server.getHost());

        Session session = null;
        try {
            // 创建SSH连接（需要传入 privateKey 参数）
            session = SshUtils.createSession(server.getHost(), server.getPort(),
                    server.getUsername(), server.getPassword(), server.getPrivateKey());
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
                String script = project.getDeployScript()
                        .replace("{{uploadPath}}", uploadPath)
                        .replace("{{appPort}}", String.valueOf(project.getAppPort()));

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
