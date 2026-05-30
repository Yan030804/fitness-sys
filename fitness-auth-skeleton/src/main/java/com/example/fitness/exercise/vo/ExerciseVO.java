package com.example.fitness.exercise.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExerciseVO {

    private Long id;
    private String exerciseName;
    private String category;
    private String bodyPart;
    private String difficulty;
    private String equipment;
    private Integer caloriesPerHour;
    private Integer defaultSets;
    private Integer defaultReps;
    private Integer durationMin;
    private String description;
    private String caution;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
