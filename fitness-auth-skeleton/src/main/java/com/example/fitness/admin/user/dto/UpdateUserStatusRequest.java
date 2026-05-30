package com.example.fitness.admin.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {

    @NotNull(message = "status不能为空")
    @Min(value = 0, message = "status只能为0或1")
    @Max(value = 1, message = "status只能为0或1")
    private Integer status;
}
