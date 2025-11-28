package com.mars.deploy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mars.deploy.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
