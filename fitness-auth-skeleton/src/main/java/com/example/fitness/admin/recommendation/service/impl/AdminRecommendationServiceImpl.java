package com.example.fitness.admin.recommendation.service.impl;

import com.example.fitness.admin.recommendation.dto.AdminRecommendationPageQuery;
import com.example.fitness.admin.recommendation.service.AdminRecommendationService;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.recommendation.mapper.RecommendationRecordMapper;
import com.example.fitness.recommendation.vo.RecommendationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRecommendationServiceImpl implements AdminRecommendationService {

    private final RecommendationRecordMapper recommendationRecordMapper;

    @Override
    public PageResponse<RecommendationVO> page(AdminRecommendationPageQuery query, String phone) {
        long total = recommendationRecordMapper.countAdminPage(query, phone);
        List<RecommendationVO> list = total == 0 ? List.of() : recommendationRecordMapper.selectAdminPage(query, phone);
        return PageResponse.of(list, total, query.getPageNum(), query.getPageSize());
    }
}
