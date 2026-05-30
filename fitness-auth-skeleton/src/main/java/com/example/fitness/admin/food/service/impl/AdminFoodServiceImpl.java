package com.example.fitness.admin.food.service.impl;

import com.example.fitness.admin.food.dto.AdminFoodPageQuery;
import com.example.fitness.admin.food.dto.CreateFoodRequest;
import com.example.fitness.admin.food.dto.UpdateFoodRequest;
import com.example.fitness.admin.food.service.AdminFoodService;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.food.entity.FitFood;
import com.example.fitness.food.mapper.FitFoodMapper;
import com.example.fitness.food.vo.FoodVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminFoodServiceImpl implements AdminFoodService {

    private final FitFoodMapper fitFoodMapper;

    @Override
    public PageResponse<FoodVO> pageFoods(AdminFoodPageQuery query) {
        long total = fitFoodMapper.countAdminPage(query);
        List<FoodVO> list = total == 0 ? List.of() : fitFoodMapper.selectAdminPage(query);
        return PageResponse.of(list, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FoodVO createFood(CreateFoodRequest request) {
        FitFood fitFood = new FitFood();
        fitFood.setFoodName(request.getFoodName().trim());
        fitFood.setCategory(normalizeString(request.getCategory()));
        fitFood.setCaloriesPer100g(request.getCaloriesPer100g());
        fitFood.setProteinPer100g(request.getProteinPer100g());
        fitFood.setFatPer100g(request.getFatPer100g());
        fitFood.setCarbPer100g(request.getCarbPer100g());
        fitFood.setSuitableGoal(normalizeString(request.getSuitableGoal()));
        fitFood.setSuitableTime(normalizeString(request.getSuitableTime()));
        fitFood.setDescription(normalizeString(request.getDescription()));
        fitFood.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        fitFood.setCreatedAt(LocalDateTime.now());
        fitFood.setUpdatedAt(LocalDateTime.now());

        int inserted = fitFoodMapper.insert(fitFood);
        if (inserted <= 0 || fitFood.getId() == null) {
            throw new BusinessException(500, "create food failed");
        }
        return getRequiredDetail(fitFood.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FoodVO updateFood(Long id, UpdateFoodRequest request) {
        FitFood existing = checkFoodExists(id);

        FitFood toUpdate = new FitFood();
        toUpdate.setId(id);
        toUpdate.setFoodName(resolveStringField(request.getFoodName(), existing.getFoodName()));
        toUpdate.setCategory(resolveStringField(request.getCategory(), existing.getCategory()));
        toUpdate.setCaloriesPer100g(request.getCaloriesPer100g() != null ? request.getCaloriesPer100g() : existing.getCaloriesPer100g());
        toUpdate.setProteinPer100g(request.getProteinPer100g() != null ? request.getProteinPer100g() : existing.getProteinPer100g());
        toUpdate.setFatPer100g(request.getFatPer100g() != null ? request.getFatPer100g() : existing.getFatPer100g());
        toUpdate.setCarbPer100g(request.getCarbPer100g() != null ? request.getCarbPer100g() : existing.getCarbPer100g());
        toUpdate.setSuitableGoal(resolveStringField(request.getSuitableGoal(), existing.getSuitableGoal()));
        toUpdate.setSuitableTime(resolveStringField(request.getSuitableTime(), existing.getSuitableTime()));
        toUpdate.setDescription(resolveStringField(request.getDescription(), existing.getDescription()));
        toUpdate.setStatus(request.getStatus() != null ? request.getStatus() : existing.getStatus());
        toUpdate.setUpdatedAt(LocalDateTime.now());

        if (!StringUtils.hasText(toUpdate.getFoodName())) {
            throw new BusinessException(400, "foodName cannot be blank");
        }

        int updated = fitFoodMapper.updateById(toUpdate);
        if (updated <= 0) {
            throw new BusinessException(500, "update food failed");
        }
        return getRequiredDetail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFood(Long id) {
        FitFood existing = checkFoodExists(id);
        if (existing.getStatus() != null && existing.getStatus() == 0) {
            return;
        }
        int updated = fitFoodMapper.updateStatusById(id, 0, LocalDateTime.now());
        if (updated <= 0) {
            throw new BusinessException(500, "delete food failed");
        }
    }

    private FitFood checkFoodExists(Long id) {
        FitFood fitFood = fitFoodMapper.selectById(id);
        if (fitFood == null) {
            throw new BusinessException(404, "food not found");
        }
        return fitFood;
    }

    private FoodVO getRequiredDetail(Long id) {
        FoodVO foodVO = fitFoodMapper.selectDetailById(id);
        if (foodVO == null) {
            throw new BusinessException(404, "food not found");
        }
        return foodVO;
    }

    private String normalizeString(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String resolveStringField(String requestValue, String existingValue) {
        if (requestValue == null) {
            return existingValue;
        }
        return normalizeString(requestValue);
    }
}
