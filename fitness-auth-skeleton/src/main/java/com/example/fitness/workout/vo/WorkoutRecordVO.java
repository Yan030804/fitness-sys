package com.example.fitness.workout.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WorkoutRecordVO {
    private Long id;
    private Long userId;
    private Long exerciseId;
    private String exerciseName;
    private LocalDate workoutDate;
    private Integer durationMin;
    private Integer setsCount;
    private Integer repsCount;
    private Integer caloriesBurned;
    private String completionStatus;
    private Integer feedbackScore;
    private String remark;
    private LocalDateTime createdAt;
}
