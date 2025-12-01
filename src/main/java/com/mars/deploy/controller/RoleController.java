package com.mars.deploy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.deploy.annotation.OperationLog;
import com.mars.deploy.common.Result;
import com.mars.deploy.entity.Role;
import com.mars.deploy.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {
    
    @Autowired
    private RoleService roleService;
    
    @GetMapping("/list")
    public Result<Page<Role>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {
        
        Page<Role> page = new Page<>(current, size);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(Role::getName, name);
        }
        
        wrapper.orderByAsc(Role::getId);
        
        return Result.success(roleService.page(page, wrapper));
    }
    
    @GetMapping("/all")
    public Result<List<Role>> all() {
        return Result.success(roleService.list());
    }
    
    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }
    
    @OperationLog(module = "角色管理", type = "新增", description = "添加角色")
    @PostMapping
    public Result<String> add(@RequestBody Role role) {
        roleService.save(role);
        return Result.success("添加成功");
    }
    
    @OperationLog(module = "角色管理", type = "编辑", description = "更新角色")
    @PutMapping
    public Result<String> update(@RequestBody Role role) {
        roleService.updateById(role);
        return Result.success("更新成功");
    }
    
    @OperationLog(module = "角色管理", type = "删除", description = "删除角色")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return Result.success("删除成功");
    }
}
