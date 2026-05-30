package com.example.fitness.workout.service;

import com.example.fitness.common.model.PageResponse;
import com.example.fitness.workout.dto.WorkoutRecordCreateRequest;
import com.example.fitness.workout.dto.WorkoutRecordPageQuery;
import com.example.fitness.workout.dto.WorkoutRecordUpdateRequest;
import com.example.fitness.workout.vo.WorkoutRecordVO;

public interface WorkoutRecordService {

    WorkoutRecordVO create(WorkoutRecordCreateRequest request);

    PageResponse<WorkoutRecordVO> page(WorkoutRecordPageQuery query);

    WorkoutRecordVO detail(Long id);

    WorkoutRecordVO update(Long id, WorkoutRecordUpdateRequest request);

    void delete(Long id);
}
