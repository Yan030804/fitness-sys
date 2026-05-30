package com.example.fitness.admin.appointment.controller;

import com.example.fitness.admin.appointment.dto.AdminAppointmentPageQuery;
import com.example.fitness.admin.appointment.dto.AdminAppointmentStatusUpdateRequest;
import com.example.fitness.admin.appointment.service.AdminAppointmentService;
import com.example.fitness.appointment.vo.AppointmentVO;
import com.example.fitness.common.api.Result;
import com.example.fitness.common.model.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/admin/appointments")
@RequiredArgsConstructor
public class AdminAppointmentController {

    private final AdminAppointmentService adminAppointmentService;

    @GetMapping
    public Result<PageResponse<AppointmentVO>> page(@Valid AdminAppointmentPageQuery query,
                                                     @RequestParam(required = false) String phone) {
        return Result.success(adminAppointmentService.page(query, phone));
    }

    @GetMapping("/{id}")
    public Result<AppointmentVO> detail(@PathVariable Long id) {
        return Result.success(adminAppointmentService.detail(id));
    }

    @PutMapping("/{id}/status")
    public Result<AppointmentVO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminAppointmentStatusUpdateRequest request
    ) {
        return Result.success(adminAppointmentService.updateStatus(id, request));
    }
}
