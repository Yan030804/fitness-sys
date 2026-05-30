package com.example.fitness.plan.service;

import com.example.fitness.common.model.PageResponse;
import com.example.fitness.plan.dto.*;
import com.example.fitness.plan.vo.PlanItemVO;
import com.example.fitness.plan.vo.PlanVO;

import java.util.Map;

public interface PlanService {

    PlanVO generate(PlanGenerateRequest request);

    PageResponse<PlanVO> page(PlanPageQuery query);

    PlanVO detail(Long id);

    PlanVO update(Long id, PlanUpdateRequest request);

    void delete(Long id);

    Map<String, Object> listItems(Long planId);

    PlanItemVO createItem(Long planId, PlanItemCreateRequest request);

    PlanItemVO updateItem(Long itemId, PlanItemUpdateRequest request);

    PlanItemVO completeItem(Long itemId, PlanItemCompleteRequest request);

    void deleteItem(Long itemId);
}
