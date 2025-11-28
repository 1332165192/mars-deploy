package com.mars.deploy.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mars.deploy.entity.Role;
import com.mars.deploy.mapper.RoleMapper;
import org.springframework.stereotype.Service;

@Service
public class RoleService extends ServiceImpl<RoleMapper, Role> {
}
