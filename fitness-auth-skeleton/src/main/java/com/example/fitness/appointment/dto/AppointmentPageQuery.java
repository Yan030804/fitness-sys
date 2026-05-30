package com.example.fitness.appointment.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AppointmentPageQuery {

    @Min(value = 1, message = "pageNum must be greater than 0")
    private Integer pageNum = 1;

    @Min(value = 1, message = "pageSize must be greater than 0")
    private Integer pageSize = 10;

    private String status;
    private LocalDate reserveDateStart;
    private LocalDate reserveDateEnd;

    public int getOffset() {
        return (Math.max(pageNum, 1) - 1) * Math.max(pageSize, 1);
    }
}
