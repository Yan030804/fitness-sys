package com.example.fitness.admin.plan.service;

import com.example.fitness.admin.plan.dto.AdminPlanPageQuery;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.plan.vo.PlanVO;

public interface AdminPlanService {

    PageResponse<PlanVO> page(AdminPlanPageQuery query, String phone);
}
