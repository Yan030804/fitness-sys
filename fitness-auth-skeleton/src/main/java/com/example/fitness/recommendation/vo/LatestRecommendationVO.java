package com.example.fitness.recommendation.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LatestRecommendationVO {

    private LocalDateTime latestTime;
    private List<RecommendationVO> list;
}
