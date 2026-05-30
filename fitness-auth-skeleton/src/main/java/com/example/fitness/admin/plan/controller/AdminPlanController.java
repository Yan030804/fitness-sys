package com.example.fitness.admin.plan.controller;

import com.example.fitness.admin.plan.dto.AdminPlanPageQuery;
import com.example.fitness.admin.plan.service.AdminPlanService;
import com.example.fitness.common.api.Result;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.plan.vo.PlanVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin/plans")
@RequiredArgsConstructor
public class AdminPlanController {

    private final AdminPlanService adminPlanService;

    @GetMapping
    public Result<PageResponse<PlanVO>> page(@Valid AdminPlanPageQuery query,
                                              @RequestParam(required = false) String phone) {
        return Result.success(adminPlanService.page(query, phone));
    }
}
