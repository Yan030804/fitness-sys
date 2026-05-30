package com.example.fitness.recommendation.service;

import com.example.fitness.common.model.PageResponse;
import com.example.fitness.recommendation.dto.RecommendationGenerateRequest;
import com.example.fitness.recommendation.dto.RecommendationHistoryQuery;
import com.example.fitness.recommendation.vo.LatestRecommendationVO;
import com.example.fitness.recommendation.vo.RecommendationGenerateVO;
import com.example.fitness.recommendation.vo.RecommendationVO;

public interface RecommendationService {

    RecommendationGenerateVO generate(RecommendationGenerateRequest request);

    LatestRecommendationVO latest(String recType);

    PageResponse<RecommendationVO> history(RecommendationHistoryQuery query);

    RecommendationVO detail(Long id);
}
