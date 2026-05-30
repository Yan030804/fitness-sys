package com.example.fitness.diet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DietRecordUpdateRequest {

    private Long foodId;
    private LocalDate dietDate;
    private String mealType;

    @DecimalMin(value = "0.00", message = "intakeGrams cannot be negative")
    private BigDecimal intakeGrams;

    @Min(value = 0, message = "caloriesIntake cannot be negative")
    private Integer caloriesIntake;

    private Integer isRecommended;
    private String remark;
}
