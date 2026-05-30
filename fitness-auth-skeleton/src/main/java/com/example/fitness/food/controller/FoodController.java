package com.example.fitness.food.controller;

import com.example.fitness.common.api.Result;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.food.dto.FoodPageQuery;
import com.example.fitness.food.service.FoodService;
import com.example.fitness.food.vo.FoodVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @GetMapping
    public Result<PageResponse<FoodVO>> pageFoods(@Valid FoodPageQuery query) {
        return Result.success(foodService.pageFoods(query));
    }

    @GetMapping("/{id}")
    public Result<FoodVO> getFoodDetail(@PathVariable Long id) {
        return Result.success(foodService.getFoodDetail(id));
    }
}
