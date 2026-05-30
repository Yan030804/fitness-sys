package com.example.fitness.recommendation.vo;

import lombok.Data;

import java.util.List;

@Data
public class RecommendationGenerateVO {

    private Integer generatedCount;
    private List<RecommendationVO> list;
}
