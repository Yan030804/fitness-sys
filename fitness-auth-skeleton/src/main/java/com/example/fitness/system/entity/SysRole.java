package com.example.fitness.system.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SysRole {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private Integer status;
    private LocalDateTime createdAt;
}
