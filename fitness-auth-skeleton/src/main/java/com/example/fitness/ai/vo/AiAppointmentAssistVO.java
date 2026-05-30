package com.example.fitness.ai.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AiAppointmentAssistVO {

    private String appointmentType;

    private LocalDate reserveDate;

    private LocalTime reserveTime;

    private String remark;

    private String answer;
}
