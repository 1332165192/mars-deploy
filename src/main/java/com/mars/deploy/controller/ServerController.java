package com.mars.deploy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.deploy.common.Result;
import com.mars.deploy.entity.Server;
import com.mars.deploy.service.ServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/server")
public class ServerController {
    
    @Autowired
    private ServerService serverService;
    
    @GetMapping("/list")
    public Result<Page<Server>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {
        
        Page<Server> page = new Page<>(current, size);
        LambdaQueryWrapper<Server> wrapper = new LambdaQueryWrapper<>();
        
        if (name != null && !name.isEmpty()) {
            wrapper.like(Server::getName, name);
        }
        
        wrapper.orderByDesc(Server::getCreateTime);
        
        return Result.success(serverService.page(page, wrapper));
    }
    
    @GetMapping("/{id}")
    public Result<Server> getById(@PathVariable Long id) {
        return Result.success(serverService.getById(id));
    }
    
    @PostMapping
    public Result<String> save(@RequestBody Server server) {
        serverService.saveOrUpdateServer(server);
        return Result.success("添加成功");
    }
    
    @PutMapping
    public Result<String> update(@RequestBody Server server) {
        serverService.saveOrUpdateServer(server);
        return Result.success("更新成功");
    }
    
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        serverService.removeById(id);
        return Result.success("删除成功");
    }
    
    @PostMapping("/test/{id}")
    public Result<String> testConnection(@PathVariable Long id) {
        Server server = serverService.getById(id);
        if (server == null) {
            return Result.error("服务器不存在");
        }
        
        boolean connected = serverService.testConnection(server);
        return connected ? Result.success("连接成功") : Result.error("连接失败");
    }
    
    @GetMapping("/detail/{id}")
    public Result<Server> getDetail(@PathVariable Long id) {
        Server server = serverService.getById(id);
        if (server == null) {
            return Result.error("服务器不存在");
        }
        return Result.success(server);
    }
    
    /**
     * 获取服务器监控信息
     */
    @GetMapping("/monitor/{id}")
    public Result<?> getMonitorInfo(@PathVariable Long id) {
        Server server = serverService.getById(id);
        if (server == null) {
            return Result.error("服务器不存在");
        }
        
        try {
            return Result.success(serverService.getMonitorInfo(server));
        } catch (Exception e) {
            return Result.error("获取监控信息失败: " + e.getMessage());
        }
    }
}
