package com.example.fitness.admin.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminAppointmentStatusUpdateRequest {

    @NotBlank(message = "status cannot be blank")
    private String status;

    private String remark;
}
