package com.example.fitness.admin.exercise.controller;

import com.example.fitness.admin.exercise.dto.AdminExercisePageQuery;
import com.example.fitness.admin.exercise.dto.CreateExerciseRequest;
import com.example.fitness.admin.exercise.dto.UpdateExerciseRequest;
import com.example.fitness.admin.exercise.service.AdminExerciseService;
import com.example.fitness.common.api.Result;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.exercise.vo.ExerciseVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/exercises")
@RequiredArgsConstructor
public class AdminExerciseController {

    private final AdminExerciseService adminExerciseService;

    @GetMapping
    public Result<PageResponse<ExerciseVO>> pageExercises(@Valid AdminExercisePageQuery query) {
        return Result.success(adminExerciseService.pageExercises(query));
    }

    @PostMapping
    public Result<ExerciseVO> createExercise(@Valid @RequestBody CreateExerciseRequest request) {
        return Result.success(adminExerciseService.createExercise(request));
    }

    @PutMapping("/{id}")
    public Result<ExerciseVO> updateExercise(@PathVariable Long id, @RequestBody UpdateExerciseRequest request) {
        return Result.success(adminExerciseService.updateExercise(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteExercise(@PathVariable Long id) {
        adminExerciseService.deleteExercise(id);
        return Result.success(null);
    }
}
