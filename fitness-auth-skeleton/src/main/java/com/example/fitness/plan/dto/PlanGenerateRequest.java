package com.example.fitness.plan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PlanGenerateRequest {

    @NotBlank(message = "planType cannot be blank")
    private String planType;

    @NotBlank(message = "planName cannot be blank")
    private String planName;

    @NotBlank(message = "cycleType cannot be blank")
    private String cycleType;

    @NotNull(message = "startDate cannot be null")
    private LocalDate startDate;

    @NotNull(message = "endDate cannot be null")
    private LocalDate endDate;

    private String targetGoal;
    private String sourceType;
    private String notes;
}
