package com.mars.deploy.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.deploy.annotation.OperationLog;
import com.mars.deploy.common.Result;
import com.mars.deploy.entity.Build;
import com.mars.deploy.entity.Project;
import com.mars.deploy.entity.ProjectMember;
import com.mars.deploy.service.BuildService;
import com.mars.deploy.service.ProjectService;
import com.mars.deploy.service.ProjectMemberService;
import com.mars.deploy.service.ProjectServerService;
import com.mars.deploy.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/project")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private ProjectMemberService projectMemberService;
    
    @Autowired
    private ProjectServerService projectServerService;
    
    @Autowired
    private UserRoleService userRoleService;
    
    @Autowired
    private BuildService buildService;
    
    @GetMapping("/list")
    public Result<Page<Project>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {
        
        Long userId = Long.parseLong(StpUtil.getLoginIdAsString());
        Page<Project> page = new Page<>(current, size);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        
        // 根据用户角色过滤项目
        boolean isAdmin = userRoleService.hasRole(userId, "ADMIN");
        boolean isProjectAdmin = userRoleService.hasRole(userId, "PROJECT_ADMIN");
        
        if (!isAdmin && !isProjectAdmin) {
            // 普通用户和开发者只能看到自己有权限的项目
            List<Long> projectIds = projectMemberService.getUserProjectIds(userId);
            if (projectIds.isEmpty()) {
                // 没有任何项目权限，返回空列表
                return Result.success(new Page<>(current, size));
            }
            wrapper.in(Project::getId, projectIds);
        }
        
        if (name != null && !name.isEmpty()) {
            wrapper.like(Project::getName, name);
        }
        
        wrapper.orderByDesc(Project::getCreateTime);
        
        Page<Project> projectPage = projectService.page(page, wrapper);
        
        // 填充最近部署时间
        projectPage.getRecords().forEach(project -> {
            LambdaQueryWrapper<Build> buildWrapper = new LambdaQueryWrapper<>();
            buildWrapper.eq(Build::getProjectId, project.getId())
                       .eq(Build::getStatus, "SUCCESS")
                       .orderByDesc(Build::getEndTime)
                       .last("LIMIT 1");
            Build lastBuild = buildService.getOne(buildWrapper);
            if (lastBuild != null && lastBuild.getEndTime() != null) {
                project.setLastDeployTime(lastBuild.getEndTime());
            }
        });
        
        return Result.success(projectPage);
    }
    
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        Project project = projectService.getById(id);
        List<Long> serverIds = projectServerService.getProjectServerIds(id);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("project", project);
        result.put("serverIds", serverIds);
        
        return Result.success(result);
    }
    
    @OperationLog(module = "项目管理", type = "新增", description = "创建项目")
    @PostMapping
    public Result<String> save(@RequestBody Map<String, Object> params) {
        Long userId = Long.parseLong(StpUtil.getLoginIdAsString());
        
        // 创建项目
        Project project = new Project();
        project.setName((String) params.get("name"));
        project.setDescription((String) params.get("description"));
        project.setGitUrl((String) params.get("gitUrl"));
        project.setBranch((String) params.get("branch"));
        project.setGitUsername((String) params.get("gitUsername"));
        project.setGitPassword((String) params.get("gitPassword"));
        project.setProjectType((String) params.get("projectType"));
        project.setBuildCommand((String) params.get("buildCommand"));
        project.setBuildDir((String) params.get("buildDir"));
        project.setAutoDeploy(params.get("autoDeploy") != null ? (Integer) params.get("autoDeploy") : 0);
        project.setDeployScript((String) params.get("deployScript"));
        project.setDeployPath((String) params.get("deployPath"));
        project.setAppPort(params.get("appPort") != null ? (Integer) params.get("appPort") : 8080);
        
        projectService.save(project);
        
        // 分配服务器
        @SuppressWarnings("unchecked")
        List<Object> serverIdsObj = (List<Object>) params.get("serverIds");
        if (serverIdsObj != null && !serverIdsObj.isEmpty()) {
            List<Long> serverIds = serverIdsObj.stream()
                    .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
                    .collect(Collectors.toList());
            projectServerService.assignServers(project.getId(), serverIds);
        }
        
        // 为创建者添加OWNER权限
        projectMemberService.addMember(project.getId(), userId, "OWNER");
        
        return Result.success("添加成功");
    }
    
    @OperationLog(module = "项目管理", type = "编辑", description = "更新项目")
    @PutMapping
    public Result<String> update(@RequestBody Map<String, Object> params) {
        Long userId = Long.parseLong(StpUtil.getLoginIdAsString());
        Long projectId = Long.valueOf(params.get("id").toString());
        
        // 检查权限：管理员、项目管理员或项目拥有者可以编辑
        if (!checkProjectPermission(userId, projectId, "EDIT")) {
            return Result.error("无权限编辑此项目");
        }
        
        // 更新项目
        Project project = projectService.getById(projectId);
        project.setName((String) params.get("name"));
        project.setDescription((String) params.get("description"));
        project.setGitUrl((String) params.get("gitUrl"));
        project.setBranch((String) params.get("branch"));
        project.setGitUsername((String) params.get("gitUsername"));
        project.setGitPassword((String) params.get("gitPassword"));
        project.setProjectType((String) params.get("projectType"));
        project.setBuildCommand((String) params.get("buildCommand"));
        project.setBuildDir((String) params.get("buildDir"));
        project.setAutoDeploy(params.get("autoDeploy") != null ? (Integer) params.get("autoDeploy") : 0);
        project.setDeployScript((String) params.get("deployScript"));
        project.setDeployPath((String) params.get("deployPath"));
        project.setAppPort(params.get("appPort") != null ? (Integer) params.get("appPort") : 8080);
        
        projectService.updateById(project);
        
        // 更新服务器分配
        @SuppressWarnings("unchecked")
        List<Object> serverIdsObj = (List<Object>) params.get("serverIds");
        if (serverIdsObj != null) {
            List<Long> serverIds = serverIdsObj.stream()
                    .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
                    .collect(Collectors.toList());
            projectServerService.assignServers(projectId, serverIds);
        }
        
        return Result.success("更新成功");
    }
    
    @OperationLog(module = "项目管理", type = "删除", description = "删除项目")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Long userId = Long.parseLong(StpUtil.getLoginIdAsString());
        
        // 检查权限：管理员、项目管理员或项目拥有者可以删除
        if (!checkProjectPermission(userId, id, "DELETE")) {
            return Result.error("无权限删除此项目");
        }
        
        projectService.removeById(id);
        return Result.success("删除成功");
    }
    
    /**
     * 检查项目权限
     */
    private boolean checkProjectPermission(Long userId, Long projectId, String action) {
        // 管理员和项目管理员有所有权限
        if (userRoleService.hasRole(userId, "ADMIN") || 
            userRoleService.hasRole(userId, "PROJECT_ADMIN")) {
            return true;
        }
        
        // 获取用户在项目中的角色
        String roleType = projectMemberService.getProjectRole(userId, projectId);
        
        if (roleType == null) {
            return false;
        }
        
        // 根据操作类型检查权限
        return switch (action) {
            case "EDIT", "DELETE" -> "OWNER".equals(roleType);
            case "BUILD" -> "OWNER".equals(roleType) || "DEVELOPER".equals(roleType);
            case "VIEW" -> true; // 所有成员都可以查看
            default -> false;
        };
    }
    
    /**
     * 获取项目成员列表
     */
    @GetMapping("/{projectId}/members")
    public Result<List<ProjectMember>> getMembers(@PathVariable Long projectId) {
        List<ProjectMember> members = projectMemberService.getProjectMembers(projectId);
        return Result.success(members);
    }
    
    /**
     * 分配项目成员
     */
    @OperationLog(module = "项目管理", type = "分配成员", description = "分配项目成员")
    @PostMapping("/{projectId}/members")
    public Result<String> assignMembers(
            @PathVariable Long projectId,
            @RequestBody Map<String, Object> params) {
        
        Long userId = Long.parseLong(StpUtil.getLoginIdAsString());
        
        // 检查权限：只有管理员、项目管理员或项目拥有者可以分配成员
        if (!checkProjectPermission(userId, projectId, "EDIT")) {
            return Result.error("无权限管理项目成员");
        }
        
        // 解析成员列表
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> membersData = (List<Map<String, Object>>) params.get("members");
        
        if (membersData != null) {
            List<ProjectMember> members = membersData.stream()
                    .map(data -> {
                        ProjectMember member = new ProjectMember();
                        Object userIdObj = data.get("userId");
                        member.setUserId(userIdObj instanceof Integer ? 
                                ((Integer) userIdObj).longValue() : (Long) userIdObj);
                        member.setRoleType((String) data.get("roleType"));
                        return member;
                    })
                    .collect(Collectors.toList());
            
            projectMemberService.batchAssignMembers(projectId, members);
        }
        
        return Result.success("分配成功");
    }
    
    /**
     * 移除项目成员
     */
    @OperationLog(module = "项目管理", type = "移除成员", description = "移除项目成员")
    @DeleteMapping("/{projectId}/members/{memberId}")
    public Result<String> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId) {
        
        Long currentUserId = Long.parseLong(StpUtil.getLoginIdAsString());
        
        // 检查权限
        if (!checkProjectPermission(currentUserId, projectId, "EDIT")) {
            return Result.error("无权限管理项目成员");
        }
        
        // 不能删除拥有者
        String roleType = projectMemberService.getProjectRole(memberId, projectId);
        if ("OWNER".equals(roleType)) {
            return Result.error("不能移除项目拥有者");
        }
        
        projectMemberService.removeMember(projectId, memberId);
        return Result.success("移除成功");
    }
}
