package com.example.fitness.recommendation.controller;

import com.example.fitness.common.api.Result;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.recommendation.dto.RecommendationGenerateRequest;
import com.example.fitness.recommendation.dto.RecommendationHistoryQuery;
import com.example.fitness.recommendation.service.RecommendationService;
import com.example.fitness.recommendation.vo.LatestRecommendationVO;
import com.example.fitness.recommendation.vo.RecommendationGenerateVO;
import com.example.fitness.recommendation.vo.RecommendationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/generate")
    public Result<RecommendationGenerateVO> generate(@Valid @RequestBody RecommendationGenerateRequest request) {
        return Result.success(recommendationService.generate(request));
    }

    @GetMapping("/latest")
    public Result<LatestRecommendationVO> latest(@RequestParam(required = false) String recType) {
        return Result.success(recommendationService.latest(recType));
    }

    @GetMapping("/history")
    public Result<PageResponse<RecommendationVO>> history(@Valid RecommendationHistoryQuery query) {
        return Result.success(recommendationService.history(query));
    }

    @GetMapping("/{id}")
    public Result<RecommendationVO> detail(@PathVariable Long id) {
        return Result.success(recommendationService.detail(id));
    }
}
