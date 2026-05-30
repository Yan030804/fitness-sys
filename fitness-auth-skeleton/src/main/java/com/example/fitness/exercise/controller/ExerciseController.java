package com.example.fitness.exercise.controller;

import com.example.fitness.common.api.Result;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.exercise.dto.ExercisePageQuery;
import com.example.fitness.exercise.service.ExerciseService;
import com.example.fitness.exercise.vo.ExerciseVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping
    public Result<PageResponse<ExerciseVO>> pageExercises(@Valid ExercisePageQuery query) {
        return Result.success(exerciseService.pageExercises(query));
    }

    @GetMapping("/{id}")
    public Result<ExerciseVO> getExerciseDetail(@PathVariable Long id) {
        return Result.success(exerciseService.getExerciseDetail(id));
    }
}
