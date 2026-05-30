package com.example.fitness.appointment.controller;

import com.example.fitness.appointment.dto.AppointmentCancelRequest;
import com.example.fitness.appointment.dto.AppointmentCreateRequest;
import com.example.fitness.appointment.dto.AppointmentPageQuery;
import com.example.fitness.appointment.dto.AppointmentUpdateRequest;
import com.example.fitness.appointment.service.AppointmentService;
import com.example.fitness.appointment.vo.AppointmentVO;
import com.example.fitness.common.api.Result;
import com.example.fitness.common.model.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public Result<AppointmentVO> create(@Valid @RequestBody AppointmentCreateRequest request) {
        return Result.success(appointmentService.create(request));
    }

    @GetMapping("/my")
    public Result<PageResponse<AppointmentVO>> pageMy(@Valid AppointmentPageQuery query) {
        return Result.success(appointmentService.pageMy(query));
    }

    @GetMapping("/{id}")
    public Result<AppointmentVO> detail(@PathVariable Long id) {
        return Result.success(appointmentService.detail(id));
    }

    @PutMapping("/{id}")
    public Result<AppointmentVO> update(@PathVariable Long id, @Valid @RequestBody AppointmentUpdateRequest request) {
        return Result.success(appointmentService.update(id, request));
    }

    @PutMapping("/{id}/cancel")
    public Result<AppointmentVO> cancel(@PathVariable Long id, @RequestBody(required = false) AppointmentCancelRequest request) {
        return Result.success(appointmentService.cancel(id, request));
    }
}
