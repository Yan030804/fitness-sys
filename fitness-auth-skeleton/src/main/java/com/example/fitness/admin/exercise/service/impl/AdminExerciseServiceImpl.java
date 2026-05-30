package com.example.fitness.admin.exercise.service.impl;

import com.example.fitness.admin.exercise.dto.AdminExercisePageQuery;
import com.example.fitness.admin.exercise.dto.CreateExerciseRequest;
import com.example.fitness.admin.exercise.dto.UpdateExerciseRequest;
import com.example.fitness.admin.exercise.service.AdminExerciseService;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.exercise.entity.FitExercise;
import com.example.fitness.exercise.mapper.FitExerciseMapper;
import com.example.fitness.exercise.support.ExerciseDifficultyNormalizer;
import com.example.fitness.exercise.vo.ExerciseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminExerciseServiceImpl implements AdminExerciseService {

    private final FitExerciseMapper fitExerciseMapper;

    @Override
    public PageResponse<ExerciseVO> pageExercises(AdminExercisePageQuery query) {
        query.setDifficulty(ExerciseDifficultyNormalizer.normalize(query.getDifficulty()));
        long total = fitExerciseMapper.countAdminPage(query);
        List<ExerciseVO> list = total == 0 ? List.of() : fitExerciseMapper.selectAdminPage(query);
        return PageResponse.of(list, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExerciseVO createExercise(CreateExerciseRequest request) {
        FitExercise fitExercise = new FitExercise();
        fitExercise.setExerciseName(request.getExerciseName().trim());
        fitExercise.setCategory(request.getCategory().trim());
        fitExercise.setBodyPart(normalizeString(request.getBodyPart()));
        fitExercise.setDifficulty(ExerciseDifficultyNormalizer.normalize(request.getDifficulty()));
        fitExercise.setEquipment(normalizeString(request.getEquipment()));
        fitExercise.setCaloriesPerHour(request.getCaloriesPerHour());
        fitExercise.setDefaultSets(request.getDefaultSets());
        fitExercise.setDefaultReps(request.getDefaultReps());
        fitExercise.setDurationMin(request.getDurationMin());
        fitExercise.setDescription(normalizeString(request.getDescription()));
        fitExercise.setCaution(normalizeString(request.getCaution()));
        fitExercise.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        fitExercise.setCreatedAt(LocalDateTime.now());
        fitExercise.setUpdatedAt(LocalDateTime.now());

        int inserted = fitExerciseMapper.insert(fitExercise);
        if (inserted <= 0 || fitExercise.getId() == null) {
            throw new BusinessException(500, "create exercise failed");
        }
        return getRequiredDetail(fitExercise.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExerciseVO updateExercise(Long id, UpdateExerciseRequest request) {
        FitExercise existing = checkExerciseExists(id);

        FitExercise toUpdate = new FitExercise();
        toUpdate.setId(id);
        toUpdate.setExerciseName(resolveStringField(request.getExerciseName(), existing.getExerciseName()));
        toUpdate.setCategory(resolveStringField(request.getCategory(), existing.getCategory()));
        toUpdate.setBodyPart(resolveStringField(request.getBodyPart(), existing.getBodyPart()));
        toUpdate.setDifficulty(resolveDifficultyField(request.getDifficulty(), existing.getDifficulty()));
        toUpdate.setEquipment(resolveStringField(request.getEquipment(), existing.getEquipment()));
        toUpdate.setCaloriesPerHour(request.getCaloriesPerHour() != null ? request.getCaloriesPerHour() : existing.getCaloriesPerHour());
        toUpdate.setDefaultSets(request.getDefaultSets() != null ? request.getDefaultSets() : existing.getDefaultSets());
        toUpdate.setDefaultReps(request.getDefaultReps() != null ? request.getDefaultReps() : existing.getDefaultReps());
        toUpdate.setDurationMin(request.getDurationMin() != null ? request.getDurationMin() : existing.getDurationMin());
        toUpdate.setDescription(resolveStringField(request.getDescription(), existing.getDescription()));
        toUpdate.setCaution(resolveStringField(request.getCaution(), existing.getCaution()));
        toUpdate.setStatus(request.getStatus() != null ? request.getStatus() : existing.getStatus());
        toUpdate.setUpdatedAt(LocalDateTime.now());

        if (!StringUtils.hasText(toUpdate.getExerciseName())) {
            throw new BusinessException(400, "exerciseName cannot be blank");
        }
        if (!StringUtils.hasText(toUpdate.getCategory())) {
            throw new BusinessException(400, "category cannot be blank");
        }

        int updated = fitExerciseMapper.updateById(toUpdate);
        if (updated <= 0) {
            throw new BusinessException(500, "update exercise failed");
        }
        return getRequiredDetail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExercise(Long id) {
        FitExercise existing = checkExerciseExists(id);
        if (existing.getStatus() != null && existing.getStatus() == 0) {
            return;
        }
        int updated = fitExerciseMapper.updateStatusById(id, 0, LocalDateTime.now());
        if (updated <= 0) {
            throw new BusinessException(500, "delete exercise failed");
        }
    }

    private FitExercise checkExerciseExists(Long id) {
        FitExercise fitExercise = fitExerciseMapper.selectById(id);
        if (fitExercise == null) {
            throw new BusinessException(404, "exercise not found");
        }
        return fitExercise;
    }

    private ExerciseVO getRequiredDetail(Long id) {
        ExerciseVO exerciseVO = fitExerciseMapper.selectDetailById(id);
        if (exerciseVO == null) {
            throw new BusinessException(404, "exercise not found");
        }
        return exerciseVO;
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

    private String resolveDifficultyField(String requestValue, String existingValue) {
        if (requestValue == null) {
            return existingValue;
        }
        return ExerciseDifficultyNormalizer.normalize(requestValue);
    }
}
