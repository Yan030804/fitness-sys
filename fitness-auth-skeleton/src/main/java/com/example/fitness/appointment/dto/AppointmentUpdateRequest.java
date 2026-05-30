package com.example.fitness.appointment.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentUpdateRequest {
    private String name;
    private String phone;
    private String gender;
    private Integer age;
    private String appointmentType;
    private LocalDate reserveDate;
    private LocalTime reserveTime;
    private String remark;
}
