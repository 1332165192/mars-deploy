package com.mars.deploy.service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.mars.deploy.entity.Plugin;
import com.mars.deploy.entity.PluginInstall;
import com.mars.deploy.entity.Server;
import com.mars.deploy.utils.SshUtils;
import com.mars.deploy.websocket.PluginLogSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * 插件安装执行器服务
 * 专门用于异步执行插件安装任务
 */
@Slf4j
@Service
public class PluginExecutorService {

    @Autowired
    private PluginInstallService pluginInstallService;

    @Autowired
    private PluginService pluginService;

    @Autowired
    private ServerService serverService;

    /**
     * 异步执行插件安装
     */
    @Async("buildExecutor")
    public void executeInstall(Long installId) {
        PluginInstall install = pluginInstallService.getById(installId);
        Plugin plugin = pluginService.getById(install.getPluginId());
        Server server = serverService.getById(install.getServerId());

        // 更新状态为安装中
        install.setStatus("INSTALLING");
        install.setInstallTime(LocalDateTime.now());
        pluginInstallService.updateById(install);

        StringBuilder logBuilder = new StringBuilder();

        try {
            // 日志输出回调
            Consumer<String> logConsumer = msg -> {
                logBuilder.append(msg).append("\n");
                PluginLogSocket.sendMessage(installId.toString(), msg);
                log.info("[Plugin-{}] {}", installId, msg);
            };

            logConsumer.accept("========================================");
            logConsumer.accept("开始安装插件: " + plugin.getName() + " (" + install.getVersion() + ")");
            logConsumer.accept("目标服务器: " + server.getHost());
            logConsumer.accept("========================================");

            // 创建SSH连接
            logConsumer.accept("\n[SSH] 连接服务器: " + server.getHost());
            Session session = null;

            try {
                session = SshUtils.createSession(
                        server.getHost(),
                        server.getPort(),
                        server.getUsername(),
                        server.getPassword(),
                        server.getPrivateKey()
                );
                session.connect();
                logConsumer.accept("[SSH] 连接成功");

                // 执行安装命令
                String script = plugin.getInstallScript()
                        .replace("{{version}}", install.getVersion());

                logConsumer.accept("\n[安装] 执行安装命令: " + script);
                executeCommand(session, script, logConsumer);

                // 安装成功
                logConsumer.accept("\n========================================");
                logConsumer.accept("安装成功！");
                logConsumer.accept("========================================");

                install.setStatus("SUCCESS");

            } finally {
                if (session != null && session.isConnected()) {
                    session.disconnect();
                    logConsumer.accept("[SSH] 连接已关闭");
                }
            }

        } catch (Exception e) {
            log.error("安装失败", e);
            String errorMsg = "[错误] " + e.getMessage();
            logBuilder.append("\n").append(errorMsg).append("\n");
            PluginLogSocket.sendMessage(installId.toString(), errorMsg);

            install.setStatus("FAILED");
        } finally {
            // 更新安装记录
            install.setLog(logBuilder.toString());
            pluginInstallService.updateById(install);
        }
    }

    /**
     * 异步执行插件卸载
     */
    @Async("buildExecutor")
    public void executeUninstall(Long installId) {
        PluginInstall install = pluginInstallService.getById(installId);
        Plugin plugin = pluginService.getById(install.getPluginId());
        Server server = serverService.getById(install.getServerId());

        // 更新状态为卸载中
        install.setStatus("UNINSTALLING");
        pluginInstallService.updateById(install);

        StringBuilder logBuilder = new StringBuilder();

        try {
            // 日志输出回调
            Consumer<String> logConsumer = msg -> {
                logBuilder.append(msg).append("\n");
                PluginLogSocket.sendMessage(installId.toString(), msg);
                log.info("[Plugin-{}] {}", installId, msg);
            };

            logConsumer.accept("========================================");
            logConsumer.accept("开始卸载插件: " + plugin.getName());
            logConsumer.accept("目标服务器: " + server.getHost());
            logConsumer.accept("========================================");

            // 创建SSH连接
            logConsumer.accept("\n[SSH] 连接服务器: " + server.getHost());
            Session session = null;

            try {
                session = SshUtils.createSession(
                        server.getHost(),
                        server.getPort(),
                        server.getUsername(),
                        server.getPassword(),
                        server.getPrivateKey()
                );
                session.connect();
                logConsumer.accept("[SSH] 连接成功");

                // 执行卸载命令
                String script = plugin.getUninstallScript();
                logConsumer.accept("\n[卸载] 执行卸载命令: " + script);
                executeCommand(session, script, logConsumer);

                // 卸载成功，删除安装记录
                logConsumer.accept("\n========================================");
                logConsumer.accept("卸载成功！");
                logConsumer.accept("========================================");

                pluginInstallService.removeById(installId);
                return; // 已删除记录，不需要再更新

            } finally {
                if (session != null && session.isConnected()) {
                    session.disconnect();
                    logConsumer.accept("[SSH] 连接已关闭");
                }
            }

        } catch (Exception e) {
            log.error("卸载失败", e);
            String errorMsg = "[错误] " + e.getMessage();
            logBuilder.append("\n").append(errorMsg).append("\n");
            PluginLogSocket.sendMessage(installId.toString(), errorMsg);

            install.setStatus("FAILED");
            install.setLog(logBuilder.toString());
            pluginInstallService.updateById(install);
        }
    }

    /**
     * 执行命令并实时输出日志
     */
    private void executeCommand(Session session, String command, Consumer<String> logConsumer) throws Exception {
        Channel channel = null;
        try {
            channel = session.openChannel("exec");
            ChannelExec execChannel = (ChannelExec) channel;
            
            // 使用 bash -c 执行命令
            execChannel.setCommand("/bin/bash -c \"" + command.replace("\"", "\\\"") + "\"");
            
            // 获取输出流
            InputStream in = execChannel.getInputStream();
            InputStream err = execChannel.getErrStream();
            
            execChannel.connect();
            
            // 读取标准输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            BufferedReader errReader = new BufferedReader(new InputStreamReader(err));
            
            String line;
            while ((line = reader.readLine()) != null) {
                logConsumer.accept(line);
            }
            
            // 读取错误输出
            while ((line = errReader.readLine()) != null) {
                logConsumer.accept("[STDERR] " + line);
            }
            
            // 等待命令执行完成
            while (!execChannel.isClosed()) {
                Thread.sleep(100);
            }
            
            // 检查退出码
            int exitStatus = execChannel.getExitStatus();
            if (exitStatus != 0) {
                throw new RuntimeException("命令执行失败，退出码: " + exitStatus);
            }
            
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }
}
