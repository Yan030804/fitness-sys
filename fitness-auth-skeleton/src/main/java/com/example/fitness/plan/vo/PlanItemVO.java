package com.example.fitness.plan.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class PlanItemVO {
    private Long id;
    private Long planId;
    private String itemType;
    private Long targetId;
    private String targetName;
    private LocalDate itemDate;
    private LocalTime itemTime;
    private Integer setsCount;
    private Integer repsCount;
    private Integer durationMin;
    private BigDecimal intakeGrams;
    private Integer calories;
    private String completionStatus;
    private Integer sortNo;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
