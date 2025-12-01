package com.mars.deploy.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.deploy.annotation.OperationLog;
import com.mars.deploy.common.Result;
import com.mars.deploy.entity.Role;
import com.mars.deploy.entity.User;
import com.mars.deploy.service.RoleService;
import com.mars.deploy.service.UserRoleService;
import com.mars.deploy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RoleService roleService;

    @GetMapping("/list")
    public Result<Page<User>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username) {

        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (username != null && !username.trim().isEmpty()) {
            wrapper.like(User::getUsername, username);
        }

        wrapper.orderByDesc(User::getCreateTime);

        Page<User> userPage = userService.page(page, wrapper);

        // 填充角色信息
        userPage.getRecords().forEach(user -> {
            user.setPassword(null); // 不返回密码
            List<Role> roles = userRoleService.getUserRoles(user.getId());
            user.setRoles(roles);
        });

        return Result.success(userPage);
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
            List<Role> roles = userRoleService.getUserRoles(user.getId());
            user.setRoles(roles);
        }
        return Result.success(user);
    }

    @OperationLog(module = "用户管理", type = "新增", description = "添加用户")
    @PostMapping
    public Result<String> add(@RequestBody Map<String, Object> params) {
        User user = new User();
        user.setUsername((String) params.get("username"));

        // MD5加密密码
        String password = (String) params.get("password");
        if (password == null || password.trim().isEmpty()) {
            password = "123456"; // 默认密码
        }
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));

        user.setNickname((String) params.get("nickname"));
        user.setEmail((String) params.get("email"));
        user.setPhone((String) params.get("phone"));
        user.setStatus(params.get("status") != null ? (Integer) params.get("status") : 1);

        userService.save(user);

        // 分配角色 - 兼容Integer和Long类型
        @SuppressWarnings("unchecked")
        List<Object> roleIdsObj = (List<Object>) params.get("roleIds");
        if (roleIdsObj != null && !roleIdsObj.isEmpty()) {
            List<Long> roleIds = roleIdsObj.stream()
                    .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
                    .collect(java.util.stream.Collectors.toList());
            userRoleService.assignRoles(user.getId(), roleIds);
        }

        return Result.success("添加成功");
    }

    @OperationLog(module = "用户管理", type = "编辑", description = "更新用户")
    @PutMapping
    public Result<String> update(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        User user = userService.getById(id);

        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setNickname((String) params.get("nickname"));
        user.setEmail((String) params.get("email"));
        user.setPhone((String) params.get("phone"));

        if (params.get("status") != null) {
            user.setStatus((Integer) params.get("status"));
        }

        // 如果提供了新密码，则更新密码
        String password = (String) params.get("password");
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        }

        userService.updateById(user);

        // 更新角色 - 兼容Integer和Long类型
        @SuppressWarnings("unchecked")
        List<Object> roleIdsObj = (List<Object>) params.get("roleIds");
        if (roleIdsObj != null) {
            List<Long> roleIds = roleIdsObj.stream()
                    .map(rid -> rid instanceof Integer ? ((Integer) rid).longValue() : (Long) rid)
                    .collect(java.util.stream.Collectors.toList());
            userRoleService.assignRoles(id, roleIds);
        }

        return Result.success("更新成功");
    }

    @OperationLog(module = "用户管理", type = "删除", description = "删除用户")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        // 不能删除自己
        String currentUserId = StpUtil.getLoginIdAsString();
        if (currentUserId.equals(id.toString())) {
            return Result.error("不能删除当前登录用户");
        }

        userService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/current")
    public Result<Map<String, Object>> getCurrentUser() {
        String userId = StpUtil.getLoginIdAsString();
        User user = userService.getById(Long.parseLong(userId));

        if (user != null) {
            user.setPassword(null);
            List<Role> roles = userRoleService.getUserRoles(user.getId());
            user.setRoles(roles);

            Map<String, Object> result = new HashMap<>();
            result.put("user", user);
            result.put("permissions", roles.stream().map(Role::getCode).toList());

            return Result.success(result);
        }

        return Result.error("用户不存在");
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody Map<String, Object> params) {
        String userId = StpUtil.getLoginIdAsString();
        User user = userService.getById(Long.parseLong(userId));

        if (user == null) {
            return Result.error("用户不存在");
        }

        // 只允许更新昵称和邮箱
        if (params.get("nickname") != null) {
            user.setNickname((String) params.get("nickname"));
        }
        if (params.get("email") != null) {
            user.setEmail((String) params.get("email"));
        }

        userService.updateById(user);
        return Result.success("个人信息更新成功");
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<String> updatePassword(@RequestBody Map<String, Object> params) {
        String userId = StpUtil.getLoginIdAsString();
        User user = userService.getById(Long.parseLong(userId));

        if (user == null) {
            return Result.error("用户不存在");
        }

        String oldPassword = (String) params.get("oldPassword");
        String newPassword = (String) params.get("newPassword");

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return Result.error("请输入原密码");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.error("请输入新密码");
        }
        if (newPassword.length() < 6) {
            return Result.error("密码至少需要6位");
        }

        // 验证原密码
        String oldPasswordMd5 = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!oldPasswordMd5.equals(user.getPassword())) {
            return Result.error("原密码错误");
        }

        // 更新密码
        String newPasswordMd5 = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        user.setPassword(newPasswordMd5);
        userService.updateById(user);

        // 退出登录
        StpUtil.logout();

        return Result.success("密码修改成功，请重新登录");
    }
}
