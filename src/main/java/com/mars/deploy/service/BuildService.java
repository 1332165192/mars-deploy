package com.mars.deploy.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mars.deploy.entity.Build;
import com.mars.deploy.entity.Project;
import com.mars.deploy.mapper.BuildMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BuildService extends ServiceImpl<BuildMapper, Build> {
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    @Lazy
    private BuildExecutorService buildExecutorService;
    
    /**
     * 触发构建
     */
    public Long triggerBuild(Long projectId) {
        Project project = projectService.getById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 创建构建记录
        Build build = new Build();
        build.setProjectId(projectId);
        build.setStatus("PENDING");
        build.setTriggerBy(StpUtil.getLoginIdAsString());
        this.save(build);
        
        // 异步执行构建（通过外部Service调用，确保异步生效）
        buildExecutorService.executeBuild(build.getId());
        
        return build.getId();
    }
}
