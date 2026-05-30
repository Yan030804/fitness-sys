package com.example.fitness.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentCreateRequest {

    @NotBlank(message = "name cannot be blank")
    private String name;

    @NotBlank(message = "phone cannot be blank")
    private String phone;

    private String gender;
    private Integer age;

    @NotBlank(message = "appointmentType cannot be blank")
    private String appointmentType;

    @NotNull(message = "reserveDate cannot be null")
    private LocalDate reserveDate;

    @NotNull(message = "reserveTime cannot be null")
    private LocalTime reserveTime;

    private String remark;
}
