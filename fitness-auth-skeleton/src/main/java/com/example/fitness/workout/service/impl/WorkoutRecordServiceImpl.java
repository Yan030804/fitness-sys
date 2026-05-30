package com.example.fitness.workout.service.impl;

import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.common.utils.SecurityUtils;
import com.example.fitness.exercise.entity.FitExercise;
import com.example.fitness.exercise.mapper.FitExerciseMapper;
import com.example.fitness.record.entity.UserWorkoutRecord;
import com.example.fitness.system.entity.SysUser;
import com.example.fitness.system.mapper.SysUserMapper;
import com.example.fitness.workout.dto.WorkoutRecordCreateRequest;
import com.example.fitness.workout.dto.WorkoutRecordPageQuery;
import com.example.fitness.workout.dto.WorkoutRecordUpdateRequest;
import com.example.fitness.workout.mapper.UserWorkoutRecordMapper;
import com.example.fitness.workout.service.WorkoutRecordService;
import com.example.fitness.workout.vo.WorkoutRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutRecordServiceImpl implements WorkoutRecordService {

    private final UserWorkoutRecordMapper userWorkoutRecordMapper;
    private final FitExerciseMapper fitExerciseMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkoutRecordVO create(WorkoutRecordCreateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        checkCurrentUserUsable(currentUserId);
        checkExerciseUsable(request.getExerciseId());

        UserWorkoutRecord record = new UserWorkoutRecord();
        record.setUserId(currentUserId);
        record.setExerciseId(request.getExerciseId());
        record.setWorkoutDate(request.getWorkoutDate());
        record.setDurationMin(request.getDurationMin());
        record.setSetsCount(request.getSetsCount());
        record.setRepsCount(request.getRepsCount());
        record.setCaloriesBurned(request.getCaloriesBurned());
        record.setCompletionStatus(request.getCompletionStatus());
        record.setFeedbackScore(request.getFeedbackScore());
        record.setRemark(request.getRemark());

        int inserted = userWorkoutRecordMapper.insert(record);
        if (inserted <= 0 || record.getId() == null) {
            throw new BusinessException(500, "create workout record failed");
        }
        return detail(record.getId());
    }

    @Override
    public PageResponse<WorkoutRecordVO> page(WorkoutRecordPageQuery query) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        long total = userWorkoutRecordMapper.countPage(currentUserId, query);
        List<WorkoutRecordVO> list = total == 0
                ? List.of()
                : userWorkoutRecordMapper.selectPage(currentUserId, query);
        return PageResponse.of(list, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    public WorkoutRecordVO detail(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        WorkoutRecordVO detail = userWorkoutRecordMapper.selectDetailByIdAndUserId(id, currentUserId);
        if (detail == null) {
            throw new BusinessException(404, "workout record not found");
        }
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkoutRecordVO update(Long id, WorkoutRecordUpdateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        UserWorkoutRecord existing = userWorkoutRecordMapper.selectByIdAndUserId(id, currentUserId);
        if (existing == null) {
            throw new BusinessException(404, "workout record not found");
        }
        if (request.getExerciseId() != null) {
            checkExerciseUsable(request.getExerciseId());
            existing.setExerciseId(request.getExerciseId());
        }
        if (request.getWorkoutDate() != null) {
            existing.setWorkoutDate(request.getWorkoutDate());
        }
        if (request.getDurationMin() != null) {
            existing.setDurationMin(request.getDurationMin());
        }
        if (request.getSetsCount() != null) {
            existing.setSetsCount(request.getSetsCount());
        }
        if (request.getRepsCount() != null) {
            existing.setRepsCount(request.getRepsCount());
        }
        if (request.getCaloriesBurned() != null) {
            existing.setCaloriesBurned(request.getCaloriesBurned());
        }
        if (request.getCompletionStatus() != null) {
            existing.setCompletionStatus(request.getCompletionStatus());
        }
        if (request.getFeedbackScore() != null) {
            existing.setFeedbackScore(request.getFeedbackScore());
        }
        if (request.getRemark() != null) {
            existing.setRemark(request.getRemark());
        }

        int updated = userWorkoutRecordMapper.updateByIdAndUserId(existing);
        if (updated <= 0) {
            throw new BusinessException(500, "update workout record failed");
        }
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        UserWorkoutRecord existing = userWorkoutRecordMapper.selectByIdAndUserId(id, currentUserId);
        if (existing == null) {
            throw new BusinessException(404, "workout record not found");
        }
        int deleted = userWorkoutRecordMapper.deleteByIdAndUserId(id, currentUserId);
        if (deleted <= 0) {
            throw new BusinessException(500, "delete workout record failed");
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

    private FitExercise checkExerciseUsable(Long exerciseId) {
        FitExercise exercise = fitExerciseMapper.selectById(exerciseId);
        if (exercise == null || exercise.getStatus() == null || exercise.getStatus() != 1) {
            throw new BusinessException(400, "exercise not found or disabled");
        }
        return exercise;
    }
}
