package com.example.fitness.admin.recommendation.controller;

import com.example.fitness.admin.recommendation.dto.AdminRecommendationPageQuery;
import com.example.fitness.admin.recommendation.service.AdminRecommendationService;
import com.example.fitness.common.api.Result;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.recommendation.vo.RecommendationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin/recommendations")
@RequiredArgsConstructor
public class AdminRecommendationController {

    private final AdminRecommendationService adminRecommendationService;

    @GetMapping
    public Result<PageResponse<RecommendationVO>> page(@Valid AdminRecommendationPageQuery query,
                                                        @RequestParam(required = false) String phone) {
        return Result.success(adminRecommendationService.page(query, phone));
    }
}
