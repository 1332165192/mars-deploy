package com.mars.deploy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mars.deploy.entity.Server;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ServerMapper extends BaseMapper<Server> {
}
