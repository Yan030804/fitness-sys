package com.example.fitness.admin.recommendation.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AdminRecommendationPageQuery {

    @Min(value = 1, message = "pageNum must be greater than 0")
    private Integer pageNum = 1;

    @Min(value = 1, message = "pageSize must be greater than 0")
    private Integer pageSize = 10;

    private Long userId;
    private String recType;

    public int getOffset() {
        return (Math.max(pageNum, 1) - 1) * Math.max(pageSize, 1);
    }
}
