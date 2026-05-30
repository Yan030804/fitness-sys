package com.example.fitness.plan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlanItemCompleteRequest {

    @NotBlank(message = "completionStatus cannot be blank")
    private String completionStatus;

    private String remark;
}
