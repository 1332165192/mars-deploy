package com.mars.deploy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_project")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String description;
    
    private String gitUrl;
    
    private String branch;
    
    private String gitUsername;
    
    private String gitPassword;
    
    private String projectType;
    
    private String buildCommand;
    
    private String buildDir;
    
    private Long serverId;
    
    private Integer autoDeploy;
    
    private String deployScript;
    
    private Integer appPort;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
