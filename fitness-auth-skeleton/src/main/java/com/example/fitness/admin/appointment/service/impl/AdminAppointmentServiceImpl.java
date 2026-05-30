package com.example.fitness.admin.appointment.service.impl;

import com.example.fitness.admin.appointment.dto.AdminAppointmentPageQuery;
import com.example.fitness.admin.appointment.dto.AdminAppointmentStatusUpdateRequest;
import com.example.fitness.admin.appointment.service.AdminAppointmentService;
import com.example.fitness.appointment.entity.AppointmentInfo;
import com.example.fitness.appointment.mapper.AppointmentInfoMapper;
import com.example.fitness.appointment.vo.AppointmentVO;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.model.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminAppointmentServiceImpl implements AdminAppointmentService {

    private static final Set<String> ALLOWED_STATUS = Set.of("PENDING", "CONFIRMED", "CANCELLED", "DONE");

    private final AppointmentInfoMapper appointmentInfoMapper;

    @Override
    public PageResponse<AppointmentVO> page(AdminAppointmentPageQuery query, String phone) {
        long total = appointmentInfoMapper.countAdminPage(query, phone);
        List<AppointmentVO> list = total == 0 ? List.of() : appointmentInfoMapper.selectAdminPage(query, phone);
        return PageResponse.of(list, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    public AppointmentVO detail(Long id) {
        AppointmentVO appointmentVO = appointmentInfoMapper.selectDetailById(id);
        if (appointmentVO == null) {
            throw new BusinessException(404, "appointment not found");
        }
        return appointmentVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppointmentVO updateStatus(Long id, AdminAppointmentStatusUpdateRequest request) {
        AppointmentInfo existing = appointmentInfoMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "appointment not found");
        }

        String normalizedStatus = normalizeStatus(request.getStatus());
        int updated = appointmentInfoMapper.updateStatusById(id, normalizedStatus, request.getRemark());
        if (updated <= 0) {
            throw new BusinessException(500, "update appointment status failed");
        }
        return detail(id);
    }

    private String normalizeStatus(String status) {
        String normalized = status == null ? null : status.trim().toUpperCase(Locale.ROOT);
        if (normalized == null || !ALLOWED_STATUS.contains(normalized)) {
            throw new BusinessException(400, "invalid appointment status");
        }
        return normalized;
    }
}
