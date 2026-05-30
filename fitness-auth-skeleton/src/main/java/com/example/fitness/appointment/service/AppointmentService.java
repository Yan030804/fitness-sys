package com.example.fitness.appointment.service;

import com.example.fitness.appointment.dto.AppointmentCancelRequest;
import com.example.fitness.appointment.dto.AppointmentCreateRequest;
import com.example.fitness.appointment.dto.AppointmentPageQuery;
import com.example.fitness.appointment.dto.AppointmentUpdateRequest;
import com.example.fitness.appointment.vo.AppointmentVO;
import com.example.fitness.common.model.PageResponse;

public interface AppointmentService {

    AppointmentVO create(AppointmentCreateRequest request);

    PageResponse<AppointmentVO> pageMy(AppointmentPageQuery query);

    AppointmentVO detail(Long id);

    AppointmentVO update(Long id, AppointmentUpdateRequest request);

    AppointmentVO cancel(Long id, AppointmentCancelRequest request);
}
