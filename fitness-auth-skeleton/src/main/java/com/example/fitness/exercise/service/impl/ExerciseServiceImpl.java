package com.example.fitness.exercise.service.impl;

import com.example.fitness.common.constants.RoleConstants;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.common.utils.SecurityUtils;
import com.example.fitness.exercise.dto.ExercisePageQuery;
import com.example.fitness.exercise.entity.FitExercise;
import com.example.fitness.exercise.mapper.FitExerciseMapper;
import com.example.fitness.exercise.service.ExerciseService;
import com.example.fitness.exercise.support.ExerciseDifficultyNormalizer;
import com.example.fitness.exercise.vo.ExerciseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseServiceImpl implements ExerciseService {

    private final FitExerciseMapper fitExerciseMapper;

    @Override
    public PageResponse<ExerciseVO> pageExercises(ExercisePageQuery query) {
        query.setDifficulty(ExerciseDifficultyNormalizer.normalize(query.getDifficulty()));
        long total = fitExerciseMapper.countPage(query);
        List<ExerciseVO> list = total == 0 ? List.of() : fitExerciseMapper.selectPage(query);
        return PageResponse.of(list, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    public ExerciseVO getExerciseDetail(Long id) {
        FitExercise fitExercise = fitExerciseMapper.selectById(id);
        if (fitExercise == null) {
            throw new BusinessException(404, "exercise not found");
        }
        boolean isAdmin = RoleConstants.ROLE_ADMIN.equals(SecurityUtils.getCurrentRoleCode());
        if (!isAdmin && (fitExercise.getStatus() == null || fitExercise.getStatus() != 1)) {
            throw new BusinessException(404, "exercise not found");
        }
        ExerciseVO exerciseVO = fitExerciseMapper.selectDetailById(id);
        if (exerciseVO == null) {
            throw new BusinessException(404, "exercise not found");
        }
        return exerciseVO;
    }
}
