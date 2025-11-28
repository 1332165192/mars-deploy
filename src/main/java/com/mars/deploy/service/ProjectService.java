package com.mars.deploy.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mars.deploy.entity.Project;
import com.mars.deploy.mapper.ProjectMapper;
import org.springframework.stereotype.Service;

@Service
public class ProjectService extends ServiceImpl<ProjectMapper, Project> {
}
