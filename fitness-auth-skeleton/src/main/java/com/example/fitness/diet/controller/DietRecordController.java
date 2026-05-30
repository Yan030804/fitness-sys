package com.example.fitness.diet.controller;

import com.example.fitness.common.api.Result;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.diet.dto.DietRecordCreateRequest;
import com.example.fitness.diet.dto.DietRecordPageQuery;
import com.example.fitness.diet.dto.DietRecordUpdateRequest;
import com.example.fitness.diet.service.DietRecordService;
import com.example.fitness.diet.vo.DietRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/diet-records")
@RequiredArgsConstructor
public class DietRecordController {

    private final DietRecordService dietRecordService;

    @PostMapping
    public Result<DietRecordVO> create(@Valid @RequestBody DietRecordCreateRequest request) {
        return Result.success(dietRecordService.create(request));
    }

    @GetMapping
    public Result<PageResponse<DietRecordVO>> page(@Valid DietRecordPageQuery query) {
        return Result.success(dietRecordService.page(query));
    }

    @GetMapping("/{id}")
    public Result<DietRecordVO> detail(@PathVariable Long id) {
        return Result.success(dietRecordService.detail(id));
    }

    @PutMapping("/{id}")
    public Result<DietRecordVO> update(
            @PathVariable Long id,
            @Valid @RequestBody DietRecordUpdateRequest request
    ) {
        return Result.success(dietRecordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dietRecordService.delete(id);
        return Result.success(null);
    }
}
