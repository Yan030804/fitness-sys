package com.example.fitness.food.service;

import com.example.fitness.common.model.PageResponse;
import com.example.fitness.food.dto.FoodPageQuery;
import com.example.fitness.food.vo.FoodVO;

public interface FoodService {

    PageResponse<FoodVO> pageFoods(FoodPageQuery query);

    FoodVO getFoodDetail(Long id);
}
