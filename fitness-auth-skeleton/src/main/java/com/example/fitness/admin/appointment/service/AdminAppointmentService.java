package com.example.fitness.admin.appointment.service;

import com.example.fitness.admin.appointment.dto.AdminAppointmentPageQuery;
import com.example.fitness.admin.appointment.dto.AdminAppointmentStatusUpdateRequest;
import com.example.fitness.appointment.vo.AppointmentVO;
import com.example.fitness.common.model.PageResponse;

public interface AdminAppointmentService {

    PageResponse<AppointmentVO> page(AdminAppointmentPageQuery query, String phone);

    AppointmentVO detail(Long id);

    AppointmentVO updateStatus(Long id, AdminAppointmentStatusUpdateRequest request);
}
