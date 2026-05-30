package com.example.fitness.admin.food.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateFoodRequest {

    @NotBlank(message = "foodName cannot be blank")
    private String foodName;

    private String category;
    private BigDecimal caloriesPer100g;
    private BigDecimal proteinPer100g;
    private BigDecimal fatPer100g;
    private BigDecimal carbPer100g;
    private String suitableGoal;
    private String suitableTime;
    private String description;
    private Integer status;
}
