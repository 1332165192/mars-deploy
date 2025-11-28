package com.mars.deploy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.deploy.common.Result;
import com.mars.deploy.entity.Build;
import com.mars.deploy.entity.Project;
import com.mars.deploy.entity.Server;
import com.mars.deploy.entity.User;
import com.mars.deploy.service.BuildService;
import com.mars.deploy.service.ProjectService;
import com.mars.deploy.service.ServerService;
import com.mars.deploy.service.UserService;
import com.mars.deploy.service.ProjectMemberService;
import com.mars.deploy.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/build")
public class BuildController {
    
    @Autowired
    private BuildService buildService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private ServerService serverService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProjectMemberService projectMemberService;
    
    @Autowired
    private UserRoleService userRoleService;
    
    @GetMapping("/list")
    public Result<Page<Build>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long projectId) {
        
        Page<Build> page = new Page<>(current, size);
        LambdaQueryWrapper<Build> wrapper = new LambdaQueryWrapper<>();
        
        if (projectId != null) {
            wrapper.eq(Build::getProjectId, projectId);
        }
        
        wrapper.orderByDesc(Build::getCreateTime);
        
        Page<Build> buildPage = buildService.page(page, wrapper);
        
        // 填充项目名称和触发人名称
        buildPage.getRecords().forEach(build -> {
            // 填充项目名称
            if (build.getProjectId() != null) {
                Project project = projectService.getById(build.getProjectId());
                if (project != null) {
                    build.setProjectName(project.getName());
                }
            }
            
            // 填充触发人名称
            if (build.getTriggerBy() != null) {
                try {
                    Long userId = Long.parseLong(build.getTriggerBy());
                    User user = userService.getById(userId);
                    if (user != null) {
                        build.setTriggerByName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    }
                } catch (NumberFormatException e) {
                    // 如果不是数字，使用原值
                    build.setTriggerByName(build.getTriggerBy());
                }
            }
        });
        
        return Result.success(buildPage);
    }
    
    @GetMapping("/{id}")
    public Result<Build> getById(@PathVariable Long id) {
        Build build = buildService.getById(id);
        
        if (build != null) {
            // 填充项目名称
            if (build.getProjectId() != null) {
                Project project = projectService.getById(build.getProjectId());
                if (project != null) {
                    build.setProjectName(project.getName());
                }
            }
            
            // 填充触发人名称
            if (build.getTriggerBy() != null) {
                try {
                    Long userId = Long.parseLong(build.getTriggerBy());
                    User user = userService.getById(userId);
                    if (user != null) {
                        build.setTriggerByName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    }
                } catch (NumberFormatException e) {
                    build.setTriggerByName(build.getTriggerBy());
                }
            }
        }
        
        return Result.success(build);
    }
    
    @PostMapping("/trigger")
    public Result<Map<String, Object>> trigger(@RequestBody Map<String, Long> params) {
        Long projectId = params.get("projectId");
        Long userId = Long.parseLong(cn.dev33.satoken.stp.StpUtil.getLoginIdAsString());
        
        // 检查权限：管理员、项目管理员、OWNER或DEVELOPER可以触发构建
        if (!checkBuildPermission(userId, projectId)) {
            return Result.error("无权限触发此项目的构建");
        }
        
        Long buildId = buildService.triggerBuild(projectId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("buildId", buildId);
        
        return Result.success(data);
    }
    
    /**
     * 检查构建权限
     */
    private boolean checkBuildPermission(Long userId, Long projectId) {
        // 管理员和项目管理员有所有权限
        if (userRoleService.hasRole(userId, "ADMIN") || 
            userRoleService.hasRole(userId, "PROJECT_ADMIN")) {
            return true;
        }
        
        // 获取用户在项目中的角色
        String roleType = projectMemberService.getProjectRole(userId, projectId);
        
        // OWNER和DEVELOPER可以触发构建
        return "OWNER".equals(roleType) || "DEVELOPER".equals(roleType);
    }
    
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        buildService.removeById(id);
        return Result.success("删除成功");
    }
    
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 项目总数
        long projectCount = projectService.count();
        stats.put("projectCount", projectCount);
        
        // 服务器总数
        long serverCount = serverService.count();
        stats.put("serverCount", serverCount);
        
        // 构建总数
        long buildCount = buildService.count();
        stats.put("buildCount", buildCount);
        
        // 成功率计算
        if (buildCount > 0) {
            long successCount = buildService.count(
                new LambdaQueryWrapper<Build>().eq(Build::getStatus, "SUCCESS")
            );
            double successRate = (double) successCount / buildCount * 100;
            stats.put("successRate", Math.round(successRate));
        } else {
            stats.put("successRate", 0);
        }
        
        return Result.success(stats);
    }
    
    /**
     * 获取构建趋势数据（最近7天）
     */
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrend() {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> successData = new ArrayList<>();
        List<Integer> failedData = new ArrayList<>();
        
        // 最近7天
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            
            LocalDateTime startTime = date.atStartOfDay();
            LocalDateTime endTime = date.plusDays(1).atStartOfDay();
            
            // 统计成功的构建
            long successCount = buildService.count(
                new LambdaQueryWrapper<Build>()
                    .eq(Build::getStatus, "SUCCESS")
                    .ge(Build::getCreateTime, startTime)
                    .lt(Build::getCreateTime, endTime)
            );
            successData.add((int) successCount);
            
            // 统计失败的构建
            long failedCount = buildService.count(
                new LambdaQueryWrapper<Build>()
                    .eq(Build::getStatus, "FAILED")
                    .ge(Build::getCreateTime, startTime)
                    .lt(Build::getCreateTime, endTime)
            );
            failedData.add((int) failedCount);
        }
        
        result.put("dates", dates);
        result.put("successData", successData);
        result.put("failedData", failedData);
        
        return Result.success(result);
    }
    
    /**
     * 获取构建状态分布数据
     */
    @GetMapping("/status-distribution")
    public Result<List<Map<String, Object>>> getStatusDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 统计各状态的数量
        String[] statuses = {"SUCCESS", "FAILED", "RUNNING", "PENDING"};
        String[] statusNames = {"成功", "失败", "运行中", "等待中"};
        
        for (int i = 0; i < statuses.length; i++) {
            long count = buildService.count(
                new LambdaQueryWrapper<Build>().eq(Build::getStatus, statuses[i])
            );
            
            if (count > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", statusNames[i]);
                item.put("value", count);
                result.add(item);
            }
        }
        
        return Result.success(result);
    }
}
