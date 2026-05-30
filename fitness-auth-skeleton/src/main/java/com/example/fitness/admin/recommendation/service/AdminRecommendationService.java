package com.example.fitness.admin.recommendation.service;

import com.example.fitness.admin.recommendation.dto.AdminRecommendationPageQuery;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.recommendation.vo.RecommendationVO;

public interface AdminRecommendationService {

    PageResponse<RecommendationVO> page(AdminRecommendationPageQuery query, String phone);
}
