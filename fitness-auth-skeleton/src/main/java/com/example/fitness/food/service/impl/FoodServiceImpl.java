package com.example.fitness.food.service.impl;

import com.example.fitness.common.constants.RoleConstants;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.common.utils.SecurityUtils;
import com.example.fitness.food.dto.FoodPageQuery;
import com.example.fitness.food.entity.FitFood;
import com.example.fitness.food.mapper.FitFoodMapper;
import com.example.fitness.food.service.FoodService;
import com.example.fitness.food.vo.FoodVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {

    private final FitFoodMapper fitFoodMapper;

    @Override
    public PageResponse<FoodVO> pageFoods(FoodPageQuery query) {
        long total = fitFoodMapper.countPage(query);
        List<FoodVO> list = total == 0 ? List.of() : fitFoodMapper.selectPage(query);
        return PageResponse.of(list, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    public FoodVO getFoodDetail(Long id) {
        FitFood fitFood = fitFoodMapper.selectById(id);
        if (fitFood == null) {
            throw new BusinessException(404, "food not found");
        }
        boolean isAdmin = RoleConstants.ROLE_ADMIN.equals(SecurityUtils.getCurrentRoleCode());
        if (!isAdmin && (fitFood.getStatus() == null || fitFood.getStatus() != 1)) {
            throw new BusinessException(404, "food not found");
        }
        FoodVO foodVO = fitFoodMapper.selectDetailById(id);
        if (foodVO == null) {
            throw new BusinessException(404, "food not found");
        }
        return foodVO;
    }
}
