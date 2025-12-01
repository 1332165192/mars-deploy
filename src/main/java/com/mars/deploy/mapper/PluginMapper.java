package com.mars.deploy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mars.deploy.entity.Plugin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PluginMapper extends BaseMapper<Plugin> {
}
