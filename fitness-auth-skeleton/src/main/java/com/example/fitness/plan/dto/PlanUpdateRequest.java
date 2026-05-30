package com.example.fitness.plan.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PlanUpdateRequest {
    private String planName;
    private String cycleType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String targetGoal;
    private String status;
    private String notes;
    private String sourceType;
}
