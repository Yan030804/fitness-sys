package com.example.fitness.auth.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String realName;
    private String nickname;
    private String gender;
    private Integer age;
    private String phone;
    private String email;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private BigDecimal bmi;
    private String fitnessGoal;
    private String activityLevel;
    private String dietPreference;
    private Long roleId;
    private String roleCode;
    private String roleName;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
