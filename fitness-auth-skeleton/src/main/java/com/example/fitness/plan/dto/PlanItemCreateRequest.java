package com.example.fitness.plan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class PlanItemCreateRequest {

    @NotBlank(message = "itemType cannot be blank")
    private String itemType;

    @NotNull(message = "targetId cannot be null")
    private Long targetId;

    private LocalDate itemDate;
    private LocalTime itemTime;
    private Integer setsCount;
    private Integer repsCount;
    private Integer durationMin;
    private BigDecimal intakeGrams;
    private Integer calories;
    private Integer sortNo;
    private String remark;
}
