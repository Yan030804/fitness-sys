package com.example.fitness.recommendation.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RecommendationVO {

    private Long id;
    private Long userId;
    private String recType;
    private Long targetId;
    private String targetName;
    private String algorithmType;
    private BigDecimal score;
    private String reason;
    private LocalDateTime createdAt;
}
