package com.mars.deploy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_build")
public class Build {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private String status;
    
    private String log;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Long duration;
    
    private String triggerBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
    
    // 项目名称（非数据库字段）
    @TableField(exist = false)
    private String projectName;
    
    // 触发人名称（非数据库字段）
    @TableField(exist = false)
    private String triggerByName;
}
