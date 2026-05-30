package com.example.fitness.plan.controller;

import com.example.fitness.common.api.Result;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.plan.dto.*;
import com.example.fitness.plan.service.PlanService;
import com.example.fitness.plan.vo.PlanItemVO;
import com.example.fitness.plan.vo.PlanVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping("/generate")
    public Result<PlanVO> generate(@Valid @RequestBody PlanGenerateRequest request) {
        return Result.success(planService.generate(request));
    }

    @GetMapping
    public Result<PageResponse<PlanVO>> page(@Valid PlanPageQuery query) {
        return Result.success(planService.page(query));
    }

    @GetMapping("/{id}")
    public Result<PlanVO> detail(@PathVariable Long id) {
        return Result.success(planService.detail(id));
    }

    @PutMapping("/{id}")
    public Result<PlanVO> update(@PathVariable Long id, @Valid @RequestBody PlanUpdateRequest request) {
        return Result.success(planService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        planService.delete(id);
        return Result.success(null);
    }

    @GetMapping("/{id}/items")
    public Result<Map<String, Object>> listItems(@PathVariable Long id) {
        return Result.success(planService.listItems(id));
    }

    @PostMapping("/{id}/items")
    public Result<PlanItemVO> createItem(@PathVariable Long id, @Valid @RequestBody PlanItemCreateRequest request) {
        return Result.success(planService.createItem(id, request));
    }

    @PutMapping("/items/{itemId}")
    public Result<PlanItemVO> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody PlanItemUpdateRequest request
    ) {
        return Result.success(planService.updateItem(itemId, request));
    }

    @PostMapping("/items/{itemId}/complete")
    public Result<PlanItemVO> completeItem(
            @PathVariable Long itemId,
            @Valid @RequestBody PlanItemCompleteRequest request
    ) {
        return Result.success(planService.completeItem(itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public Result<Void> deleteItem(@PathVariable Long itemId) {
        planService.deleteItem(itemId);
        return Result.success(null);
    }
}
