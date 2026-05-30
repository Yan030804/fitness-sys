package com.example.fitness.admin.exercise.service;

import com.example.fitness.admin.exercise.dto.AdminExercisePageQuery;
import com.example.fitness.admin.exercise.dto.CreateExerciseRequest;
import com.example.fitness.admin.exercise.dto.UpdateExerciseRequest;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.exercise.vo.ExerciseVO;

public interface AdminExerciseService {

    PageResponse<ExerciseVO> pageExercises(AdminExercisePageQuery query);

    ExerciseVO createExercise(CreateExerciseRequest request);

    ExerciseVO updateExercise(Long id, UpdateExerciseRequest request);

    void deleteExercise(Long id);
}
