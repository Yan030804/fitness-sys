package com.example.fitness.record.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserDietRecord {
    private Long id;
    private Long userId;
    private Long foodId;
    private LocalDate dietDate;
    private String mealType;
    private BigDecimal intakeGrams;
    private Integer caloriesIntake;
    private Integer isRecommended;
    private String remark;
    private LocalDateTime createdAt;
}
