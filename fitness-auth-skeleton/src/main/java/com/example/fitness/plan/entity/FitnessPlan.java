package com.example.fitness.plan.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FitnessPlan {
    private Long id;
    private Long userId;
    private String planType;
    private String planName;
    private String cycleType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String targetGoal;
    private String sourceType;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
