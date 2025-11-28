package com.mars.deploy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mars.deploy.entity.Project;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}
