package com.example.fitness.workout.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkoutRecordUpdateRequest {

    private Long exerciseId;
    private LocalDate workoutDate;

    @Min(value = 0, message = "durationMin cannot be negative")
    private Integer durationMin;

    @Min(value = 0, message = "setsCount cannot be negative")
    private Integer setsCount;

    @Min(value = 0, message = "repsCount cannot be negative")
    private Integer repsCount;

    @Min(value = 0, message = "caloriesBurned cannot be negative")
    private Integer caloriesBurned;

    private String completionStatus;

    @Min(value = 1, message = "feedbackScore must be between 1 and 5")
    @Max(value = 5, message = "feedbackScore must be between 1 and 5")
    private Integer feedbackScore;

    private String remark;
}
