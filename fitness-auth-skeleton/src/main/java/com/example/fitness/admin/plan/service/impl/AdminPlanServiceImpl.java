package com.example.fitness.admin.plan.service.impl;

import com.example.fitness.admin.plan.dto.AdminPlanPageQuery;
import com.example.fitness.admin.plan.service.AdminPlanService;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.plan.mapper.FitnessPlanMapper;
import com.example.fitness.plan.vo.PlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPlanServiceImpl implements AdminPlanService {

    private final FitnessPlanMapper fitnessPlanMapper;

    @Override
    public PageResponse<PlanVO> page(AdminPlanPageQuery query, String phone) {
        long total = fitnessPlanMapper.countAdminPage(query, phone);
        List<PlanVO> list = total == 0 ? List.of() : fitnessPlanMapper.selectAdminPage(query, phone);
        return PageResponse.of(list, total, query.getPageNum(), query.getPageSize());
    }
}
