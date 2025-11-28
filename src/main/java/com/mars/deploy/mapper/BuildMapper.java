package com.mars.deploy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mars.deploy.entity.Build;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BuildMapper extends BaseMapper<Build> {
}
