package com.example.fitness.exercise.service;

import com.example.fitness.common.model.PageResponse;
import com.example.fitness.exercise.dto.ExercisePageQuery;
import com.example.fitness.exercise.vo.ExerciseVO;

public interface ExerciseService {

    PageResponse<ExerciseVO> pageExercises(ExercisePageQuery query);

    ExerciseVO getExerciseDetail(Long id);
}
