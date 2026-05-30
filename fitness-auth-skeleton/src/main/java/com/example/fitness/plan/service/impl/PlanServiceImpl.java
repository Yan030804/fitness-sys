package com.example.fitness.plan.service.impl;

import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.common.utils.SecurityUtils;
import com.example.fitness.exercise.entity.FitExercise;
import com.example.fitness.exercise.mapper.FitExerciseMapper;
import com.example.fitness.food.entity.FitFood;
import com.example.fitness.food.mapper.FitFoodMapper;
import com.example.fitness.plan.dto.*;
import com.example.fitness.plan.entity.FitnessPlan;
import com.example.fitness.plan.entity.FitnessPlanItem;
import com.example.fitness.plan.mapper.FitnessPlanItemMapper;
import com.example.fitness.plan.mapper.FitnessPlanMapper;
import com.example.fitness.plan.service.PlanService;
import com.example.fitness.plan.vo.PlanItemVO;
import com.example.fitness.plan.vo.PlanVO;
import com.example.fitness.system.entity.SysUser;
import com.example.fitness.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private static final String ITEM_TYPE_EXERCISE = "EXERCISE";
    private static final String ITEM_TYPE_FOOD = "FOOD";

    private final FitnessPlanMapper fitnessPlanMapper;
    private final FitnessPlanItemMapper fitnessPlanItemMapper;
    private final SysUserMapper sysUserMapper;
    private final FitExerciseMapper fitExerciseMapper;
    private final FitFoodMapper fitFoodMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanVO generate(PlanGenerateRequest request) {
        validatePlanDateRange(request.getStartDate(), request.getEndDate());
        Long currentUserId = SecurityUtils.getCurrentUserId();
        checkCurrentUserUsable(currentUserId);

        FitnessPlan plan = new FitnessPlan();
        plan.setUserId(currentUserId);
        plan.setPlanType(normalizeUpper(request.getPlanType()));
        plan.setPlanName(request.getPlanName());
        plan.setCycleType(normalizeUpper(request.getCycleType()));
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setTargetGoal(request.getTargetGoal());
        plan.setSourceType(request.getSourceType() == null ? "SYSTEM" : normalizeUpper(request.getSourceType()));
        plan.setStatus("PENDING");
        plan.setNotes(request.getNotes());

        int inserted = fitnessPlanMapper.insert(plan);
        if (inserted <= 0 || plan.getId() == null) {
            throw new BusinessException(500, "generate plan failed");
        }
        return detail(plan.getId());
    }

    @Override
    public PageResponse<PlanVO> page(PlanPageQuery query) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        long total = fitnessPlanMapper.countUserPage(currentUserId, query);
        List<PlanVO> list = total == 0 ? List.of() : fitnessPlanMapper.selectUserPage(currentUserId, query);
        return PageResponse.of(list, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    public PlanVO detail(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        PlanVO plan = fitnessPlanMapper.selectDetailByIdAndUserId(id, currentUserId);
        if (plan == null) {
            throw new BusinessException(404, "plan not found");
        }
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanVO update(Long id, PlanUpdateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        FitnessPlan existing = getOwnedPlan(id, currentUserId);

        if (request.getStartDate() != null || request.getEndDate() != null) {
            validatePlanDateRange(
                    request.getStartDate() != null ? request.getStartDate() : existing.getStartDate(),
                    request.getEndDate() != null ? request.getEndDate() : existing.getEndDate()
            );
        }

        if (request.getPlanName() != null) {
            existing.setPlanName(request.getPlanName());
        }
        if (request.getCycleType() != null) {
            existing.setCycleType(normalizeUpper(request.getCycleType()));
        }
        if (request.getStartDate() != null) {
            existing.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            existing.setEndDate(request.getEndDate());
        }
        if (request.getTargetGoal() != null) {
            existing.setTargetGoal(request.getTargetGoal());
        }
        if (request.getStatus() != null) {
            existing.setStatus(normalizeUpper(request.getStatus()));
        }
        if (request.getNotes() != null) {
            existing.setNotes(request.getNotes());
        }
        if (request.getSourceType() != null) {
            existing.setSourceType(normalizeUpper(request.getSourceType()));
        }

        int updated = fitnessPlanMapper.updateByIdAndUserId(existing);
        if (updated <= 0) {
            throw new BusinessException(500, "update plan failed");
        }
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        getOwnedPlan(id, currentUserId);
        fitnessPlanItemMapper.deleteByPlanId(id);
        int deleted = fitnessPlanMapper.deleteByIdAndUserId(id, currentUserId);
        if (deleted <= 0) {
            throw new BusinessException(500, "delete plan failed");
        }
    }

    @Override
    public Map<String, Object> listItems(Long planId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        getOwnedPlan(planId, currentUserId);
        List<PlanItemVO> list = fitnessPlanItemMapper.selectByPlanId(planId);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanItemVO createItem(Long planId, PlanItemCreateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        getOwnedPlan(planId, currentUserId);

        ResolvedTarget resolvedTarget = resolveTarget(request.getItemType(), request.getTargetId());
        FitnessPlanItem item = new FitnessPlanItem();
        item.setPlanId(planId);
        item.setItemType(resolvedTarget.itemType());
        item.setTargetId(request.getTargetId());
        item.setTargetName(resolvedTarget.targetName());
        item.setItemDate(request.getItemDate());
        item.setItemTime(request.getItemTime());
        item.setSetsCount(request.getSetsCount());
        item.setRepsCount(request.getRepsCount());
        item.setDurationMin(request.getDurationMin());
        item.setIntakeGrams(request.getIntakeGrams());
        item.setCalories(request.getCalories());
        item.setCompletionStatus("PENDING");
        item.setSortNo(request.getSortNo());
        item.setRemark(request.getRemark());

        int inserted = fitnessPlanItemMapper.insert(item);
        if (inserted <= 0 || item.getId() == null) {
            throw new BusinessException(500, "create plan item failed");
        }
        return getOwnedItemDetail(item.getId(), currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanItemVO updateItem(Long itemId, PlanItemUpdateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        FitnessPlanItem existing = getOwnedPlanItem(itemId, currentUserId);

        String itemType = request.getItemType() != null ? request.getItemType() : existing.getItemType();
        Long targetId = request.getTargetId() != null ? request.getTargetId() : existing.getTargetId();
        if (request.getItemType() != null || request.getTargetId() != null) {
            ResolvedTarget resolvedTarget = resolveTarget(itemType, targetId);
            existing.setItemType(resolvedTarget.itemType());
            existing.setTargetId(targetId);
            existing.setTargetName(resolvedTarget.targetName());
        }
        if (request.getItemDate() != null) {
            existing.setItemDate(request.getItemDate());
        }
        if (request.getItemTime() != null) {
            existing.setItemTime(request.getItemTime());
        }
        if (request.getSetsCount() != null) {
            existing.setSetsCount(request.getSetsCount());
        }
        if (request.getRepsCount() != null) {
            existing.setRepsCount(request.getRepsCount());
        }
        if (request.getDurationMin() != null) {
            existing.setDurationMin(request.getDurationMin());
        }
        if (request.getIntakeGrams() != null) {
            existing.setIntakeGrams(request.getIntakeGrams());
        }
        if (request.getCalories() != null) {
            existing.setCalories(request.getCalories());
        }
        if (request.getSortNo() != null) {
            existing.setSortNo(request.getSortNo());
        }
        if (request.getRemark() != null) {
            existing.setRemark(request.getRemark());
        }
        if (request.getCompletionStatus() != null) {
            existing.setCompletionStatus(normalizeUpper(request.getCompletionStatus()));
        }

        int updated = fitnessPlanItemMapper.updateById(existing);
        if (updated <= 0) {
            throw new BusinessException(500, "update plan item failed");
        }
        return getOwnedItemDetail(itemId, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanItemVO completeItem(Long itemId, PlanItemCompleteRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        FitnessPlanItem existing = getOwnedPlanItem(itemId, currentUserId);
        existing.setCompletionStatus(normalizeUpper(request.getCompletionStatus()));
        if (request.getRemark() != null) {
            existing.setRemark(request.getRemark());
        }
        int updated = fitnessPlanItemMapper.updateById(existing);
        if (updated <= 0) {
            throw new BusinessException(500, "complete plan item failed");
        }
        return getOwnedItemDetail(itemId, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long itemId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        getOwnedPlanItem(itemId, currentUserId);
        int deleted = fitnessPlanItemMapper.deleteById(itemId);
        if (deleted <= 0) {
            throw new BusinessException(500, "delete plan item failed");
        }
    }

    private SysUser checkCurrentUserUsable(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "user not found");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "user is disabled");
        }
        return user;
    }

    private FitnessPlan getOwnedPlan(Long planId, Long currentUserId) {
        FitnessPlan plan = fitnessPlanMapper.selectByIdAndUserId(planId, currentUserId);
        if (plan == null) {
            throw new BusinessException(404, "plan not found");
        }
        return plan;
    }

    private FitnessPlanItem getOwnedPlanItem(Long itemId, Long currentUserId) {
        FitnessPlanItem item = fitnessPlanItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(404, "plan item not found");
        }
        FitnessPlan plan = fitnessPlanMapper.selectByIdAndUserId(item.getPlanId(), currentUserId);
        if (plan == null) {
            throw new BusinessException(403, "no permission to access this plan item");
        }
        return item;
    }

    private PlanItemVO getOwnedItemDetail(Long itemId, Long currentUserId) {
        FitnessPlanItem item = getOwnedPlanItem(itemId, currentUserId);
        PlanItemVO detail = fitnessPlanItemMapper.selectDetailById(item.getId());
        if (detail == null) {
            throw new BusinessException(404, "plan item not found");
        }
        return detail;
    }

    private ResolvedTarget resolveTarget(String rawItemType, Long targetId) {
        if (targetId == null) {
            throw new BusinessException(400, "targetId cannot be null");
        }
        String itemType = normalizeUpper(rawItemType);
        if (ITEM_TYPE_EXERCISE.equals(itemType)) {
            FitExercise exercise = fitExerciseMapper.selectById(targetId);
            if (exercise == null || exercise.getStatus() == null || exercise.getStatus() != 1) {
                throw new BusinessException(400, "exercise not found or disabled");
            }
            return new ResolvedTarget(itemType, exercise.getExerciseName());
        }
        if (ITEM_TYPE_FOOD.equals(itemType)) {
            FitFood food = fitFoodMapper.selectById(targetId);
            if (food == null || food.getStatus() == null || food.getStatus() != 1) {
                throw new BusinessException(400, "food not found or disabled");
            }
            return new ResolvedTarget(itemType, food.getFoodName());
        }
        throw new BusinessException(400, "itemType must be EXERCISE or FOOD");
    }

    private void validatePlanDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(400, "startDate cannot be later than endDate");
        }
    }

    private String normalizeUpper(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private record ResolvedTarget(String itemType, String targetName) {}
}
