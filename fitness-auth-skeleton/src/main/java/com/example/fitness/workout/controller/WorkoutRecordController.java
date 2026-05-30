package com.example.fitness.workout.controller;

import com.example.fitness.common.api.Result;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.workout.dto.WorkoutRecordCreateRequest;
import com.example.fitness.workout.dto.WorkoutRecordPageQuery;
import com.example.fitness.workout.dto.WorkoutRecordUpdateRequest;
import com.example.fitness.workout.service.WorkoutRecordService;
import com.example.fitness.workout.vo.WorkoutRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/workout-records")
@RequiredArgsConstructor
public class WorkoutRecordController {

    private final WorkoutRecordService workoutRecordService;

    @PostMapping
    public Result<WorkoutRecordVO> create(@Valid @RequestBody WorkoutRecordCreateRequest request) {
        return Result.success(workoutRecordService.create(request));
    }

    @GetMapping
    public Result<PageResponse<WorkoutRecordVO>> page(@Valid WorkoutRecordPageQuery query) {
        return Result.success(workoutRecordService.page(query));
    }

    @GetMapping("/{id}")
    public Result<WorkoutRecordVO> detail(@PathVariable Long id) {
        return Result.success(workoutRecordService.detail(id));
    }

    @PutMapping("/{id}")
    public Result<WorkoutRecordVO> update(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutRecordUpdateRequest request
    ) {
        return Result.success(workoutRecordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        workoutRecordService.delete(id);
        return Result.success(null);
    }
}
