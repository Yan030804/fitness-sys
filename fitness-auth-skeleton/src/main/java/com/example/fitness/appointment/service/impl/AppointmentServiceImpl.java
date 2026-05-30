package com.example.fitness.appointment.service.impl;

import com.example.fitness.appointment.dto.AppointmentCancelRequest;
import com.example.fitness.appointment.dto.AppointmentCreateRequest;
import com.example.fitness.appointment.dto.AppointmentPageQuery;
import com.example.fitness.appointment.dto.AppointmentUpdateRequest;
import com.example.fitness.appointment.entity.AppointmentInfo;
import com.example.fitness.appointment.mapper.AppointmentInfoMapper;
import com.example.fitness.appointment.service.AppointmentService;
import com.example.fitness.appointment.vo.AppointmentVO;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.common.utils.SecurityUtils;
import com.example.fitness.system.entity.SysUser;
import com.example.fitness.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_DONE = "DONE";

    private final AppointmentInfoMapper appointmentInfoMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppointmentVO create(AppointmentCreateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        checkCurrentUserUsable(currentUserId);

        AppointmentInfo appointmentInfo = new AppointmentInfo();
        appointmentInfo.setUserId(currentUserId);
        appointmentInfo.setName(request.getName());
        appointmentInfo.setPhone(request.getPhone());
        appointmentInfo.setGender(request.getGender());
        appointmentInfo.setAge(request.getAge());
        appointmentInfo.setAppointmentType(request.getAppointmentType());
        appointmentInfo.setReserveDate(request.getReserveDate());
        appointmentInfo.setReserveTime(request.getReserveTime());
        appointmentInfo.setStatus(STATUS_PENDING);
        appointmentInfo.setRemark(request.getRemark());

        int inserted = appointmentInfoMapper.insert(appointmentInfo);
        if (inserted <= 0 || appointmentInfo.getId() == null) {
            throw new BusinessException(500, "create appointment failed");
        }
        return detail(appointmentInfo.getId());
    }

    @Override
    public PageResponse<AppointmentVO> pageMy(AppointmentPageQuery query) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        long total = appointmentInfoMapper.countUserPage(currentUserId, query);
        List<AppointmentVO> list = total == 0
                ? List.of()
                : appointmentInfoMapper.selectUserPage(currentUserId, query);
        return PageResponse.of(list, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    public AppointmentVO detail(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        AppointmentVO appointmentVO = appointmentInfoMapper.selectDetailByIdAndUserId(id, currentUserId);
        if (appointmentVO == null) {
            throw new BusinessException(404, "appointment not found");
        }
        return appointmentVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppointmentVO update(Long id, AppointmentUpdateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        AppointmentInfo existing = appointmentInfoMapper.selectByIdAndUserId(id, currentUserId);
        if (existing == null) {
            throw new BusinessException(404, "appointment not found");
        }
        if (STATUS_DONE.equalsIgnoreCase(existing.getStatus())) {
            throw new BusinessException(409, "completed appointment cannot be updated");
        }

        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getPhone() != null) {
            existing.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            existing.setGender(request.getGender());
        }
        if (request.getAge() != null) {
            existing.setAge(request.getAge());
        }
        if (request.getAppointmentType() != null) {
            existing.setAppointmentType(request.getAppointmentType());
        }
        if (request.getReserveDate() != null) {
            existing.setReserveDate(request.getReserveDate());
        }
        if (request.getReserveTime() != null) {
            existing.setReserveTime(request.getReserveTime());
        }
        if (request.getRemark() != null) {
            existing.setRemark(request.getRemark());
        }

        int updated = appointmentInfoMapper.updateByIdAndUserId(existing);
        if (updated <= 0) {
            throw new BusinessException(500, "update appointment failed");
        }
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppointmentVO cancel(Long id, AppointmentCancelRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        AppointmentInfo existing = appointmentInfoMapper.selectByIdAndUserId(id, currentUserId);
        if (existing == null) {
            throw new BusinessException(404, "appointment not found");
        }
        if (STATUS_CANCELLED.equalsIgnoreCase(existing.getStatus())) {
            return detail(id);
        }
        if (STATUS_DONE.equalsIgnoreCase(existing.getStatus())) {
            throw new BusinessException(409, "completed appointment cannot be cancelled");
        }

        existing.setStatus(STATUS_CANCELLED);
        if (request != null && request.getRemark() != null) {
            existing.setRemark(request.getRemark());
        }
        int updated = appointmentInfoMapper.updateByIdAndUserId(existing);
        if (updated <= 0) {
            throw new BusinessException(500, "cancel appointment failed");
        }
        return detail(id);
    }

    private void checkCurrentUserUsable(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "user not found");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "user is disabled");
        }
    }
}
