package com.example.fitness.appointment.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class AppointmentVO {
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
