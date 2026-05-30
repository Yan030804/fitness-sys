package com.example.fitness.diet.service.impl;

import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.common.utils.SecurityUtils;
import com.example.fitness.diet.dto.DietRecordCreateRequest;
import com.example.fitness.diet.dto.DietRecordPageQuery;
import com.example.fitness.diet.dto.DietRecordUpdateRequest;
import com.example.fitness.diet.mapper.UserDietRecordMapper;
import com.example.fitness.diet.service.DietRecordService;
import com.example.fitness.diet.vo.DietRecordVO;
import com.example.fitness.food.entity.FitFood;
import com.example.fitness.food.mapper.FitFoodMapper;
import com.example.fitness.record.entity.UserDietRecord;
import com.example.fitness.system.entity.SysUser;
import com.example.fitness.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DietRecordServiceImpl implements DietRecordService {

    private final UserDietRecordMapper userDietRecordMapper;
    private final FitFoodMapper fitFoodMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DietRecordVO create(DietRecordCreateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        checkCurrentUserUsable(currentUserId);
        checkFoodUsable(request.getFoodId());

        UserDietRecord record = new UserDietRecord();
        record.setUserId(currentUserId);
        record.setFoodId(request.getFoodId());
        record.setDietDate(request.getDietDate());
        record.setMealType(request.getMealType());
        record.setIntakeGrams(request.getIntakeGrams());
        record.setCaloriesIntake(request.getCaloriesIntake());
        record.setIsRecommended(request.getIsRecommended() == null ? 0 : request.getIsRecommended());
        record.setRemark(request.getRemark());

        int inserted = userDietRecordMapper.insert(record);
        if (inserted <= 0 || record.getId() == null) {
            throw new BusinessException(500, "create diet record failed");
        }
        return detail(record.getId());
    }

    @Override
    public PageResponse<DietRecordVO> page(DietRecordPageQuery query) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        long total = userDietRecordMapper.countPage(currentUserId, query);
        List<DietRecordVO> list = total == 0
                ? List.of()
                : userDietRecordMapper.selectPage(currentUserId, query);
        return PageResponse.of(list, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    public DietRecordVO detail(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        DietRecordVO detail = userDietRecordMapper.selectDetailByIdAndUserId(id, currentUserId);
        if (detail == null) {
            throw new BusinessException(404, "diet record not found");
        }
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DietRecordVO update(Long id, DietRecordUpdateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        UserDietRecord existing = userDietRecordMapper.selectByIdAndUserId(id, currentUserId);
        if (existing == null) {
            throw new BusinessException(404, "diet record not found");
        }
        if (request.getFoodId() != null) {
            checkFoodUsable(request.getFoodId());
            existing.setFoodId(request.getFoodId());
        }
        if (request.getDietDate() != null) {
            existing.setDietDate(request.getDietDate());
        }
        if (request.getMealType() != null) {
            existing.setMealType(request.getMealType());
        }
        if (request.getIntakeGrams() != null) {
            existing.setIntakeGrams(request.getIntakeGrams());
        }
        if (request.getCaloriesIntake() != null) {
            existing.setCaloriesIntake(request.getCaloriesIntake());
        }
        if (request.getIsRecommended() != null) {
            existing.setIsRecommended(request.getIsRecommended());
        }
        if (request.getRemark() != null) {
            existing.setRemark(request.getRemark());
        }

        int updated = userDietRecordMapper.updateByIdAndUserId(existing);
        if (updated <= 0) {
            throw new BusinessException(500, "update diet record failed");
        }
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        UserDietRecord existing = userDietRecordMapper.selectByIdAndUserId(id, currentUserId);
        if (existing == null) {
            throw new BusinessException(404, "diet record not found");
        }
        int deleted = userDietRecordMapper.deleteByIdAndUserId(id, currentUserId);
        if (deleted <= 0) {
            throw new BusinessException(500, "delete diet record failed");
        }
    }

    private void checkCurrentUserUsable(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "user not found");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "user is disabled");
        }
    }

    private FitFood checkFoodUsable(Long foodId) {
        FitFood food = fitFoodMapper.selectById(foodId);
        if (food == null || food.getStatus() == null || food.getStatus() != 1) {
            throw new BusinessException(400, "food not found or disabled");
        }
        return food;
    }
}
