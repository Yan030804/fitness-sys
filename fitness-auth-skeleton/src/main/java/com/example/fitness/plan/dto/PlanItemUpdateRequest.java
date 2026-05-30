package com.example.fitness.plan.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class PlanItemUpdateRequest {
    private String itemType;
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
    private String completionStatus;
}
