package com.example.fitness.recommendation.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RecommendationRecord {

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
