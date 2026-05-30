package com.example.fitness.admin.food.service;

import com.example.fitness.admin.food.dto.AdminFoodPageQuery;
import com.example.fitness.admin.food.dto.CreateFoodRequest;
import com.example.fitness.admin.food.dto.UpdateFoodRequest;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.food.vo.FoodVO;

public interface AdminFoodService {

    PageResponse<FoodVO> pageFoods(AdminFoodPageQuery query);

    FoodVO createFood(CreateFoodRequest request);

    FoodVO updateFood(Long id, UpdateFoodRequest request);

    void deleteFood(Long id);
}
