package com.example.fitness.admin.food.controller;

import com.example.fitness.admin.food.dto.AdminFoodPageQuery;
import com.example.fitness.admin.food.dto.CreateFoodRequest;
import com.example.fitness.admin.food.dto.UpdateFoodRequest;
import com.example.fitness.admin.food.service.AdminFoodService;
import com.example.fitness.common.api.Result;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.food.vo.FoodVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/foods")
@RequiredArgsConstructor
public class AdminFoodController {

    private final AdminFoodService adminFoodService;

    @GetMapping
    public Result<PageResponse<FoodVO>> pageFoods(@Valid AdminFoodPageQuery query) {
        return Result.success(adminFoodService.pageFoods(query));
    }

    @PostMapping
    public Result<FoodVO> createFood(@Valid @RequestBody CreateFoodRequest request) {
        return Result.success(adminFoodService.createFood(request));
    }

    @PutMapping("/{id}")
    public Result<FoodVO> updateFood(@PathVariable Long id, @RequestBody UpdateFoodRequest request) {
        return Result.success(adminFoodService.updateFood(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteFood(@PathVariable Long id) {
        adminFoodService.deleteFood(id);
        return Result.success(null);
    }
}
