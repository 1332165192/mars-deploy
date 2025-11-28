package com.mars.deploy.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mars.deploy.entity.Server;
import com.mars.deploy.mapper.ServerMapper;
import com.mars.deploy.utils.SshUtils;
import org.springframework.stereotype.Service;

@Service
public class ServerService extends ServiceImpl<ServerMapper, Server> {
    
    /**
     * 测试服务器连接
     */
    public boolean testConnection(Server server) {
        return SshUtils.testConnection(
            server.getHost(),
            server.getPort(),
            server.getUsername(),
            server.getPassword(),
            server.getPrivateKey()
        );
    }
    
    /**
     * 保存或更新服务器信息，同时测试连接
     */
    public boolean saveOrUpdateServer(Server server) {
        boolean connected = testConnection(server);
        server.setStatus(connected ? "ONLINE" : "OFFLINE");
        return this.saveOrUpdate(server);
    }
}
