package com.example.fitness.diet.service;

import com.example.fitness.common.model.PageResponse;
import com.example.fitness.diet.dto.DietRecordCreateRequest;
import com.example.fitness.diet.dto.DietRecordPageQuery;
import com.example.fitness.diet.dto.DietRecordUpdateRequest;
import com.example.fitness.diet.vo.DietRecordVO;

public interface DietRecordService {

    DietRecordVO create(DietRecordCreateRequest request);

    PageResponse<DietRecordVO> page(DietRecordPageQuery query);

    DietRecordVO detail(Long id);

    DietRecordVO update(Long id, DietRecordUpdateRequest request);

    void delete(Long id);
}
