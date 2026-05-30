package com.example.fitness.appointment.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class AppointmentInfo {
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String gender;
    private Integer age;
    private String appointmentType;
    private LocalDate reserveDate;
    private LocalTime reserveTime;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
