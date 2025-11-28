package com.mars.deploy.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.mars.deploy.common.Result;
import com.mars.deploy.entity.User;
import com.mars.deploy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User loginUser) {
        User user = userService.login(loginUser.getUsername(), loginUser.getPassword());
        
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        
        // 登录
        StpUtil.login(user.getId());
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", StpUtil.getTokenValue());
        data.put("user", user);
        
        return Result.success(data);
    }
    
    @PostMapping("/logout")
    public Result<String> logout() {
        StpUtil.logout();
        return Result.success("退出成功");
    }
    
    @GetMapping("/info")
    public Result<User> info() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        return Result.success(user);
    }
}
