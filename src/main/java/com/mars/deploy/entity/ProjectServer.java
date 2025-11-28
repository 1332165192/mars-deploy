package com.mars.deploy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_project_server")
public class ProjectServer {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private Long serverId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
