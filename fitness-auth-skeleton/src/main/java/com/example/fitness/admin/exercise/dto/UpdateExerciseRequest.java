package com.example.fitness.admin.exercise.dto;

import lombok.Data;

@Data
public class UpdateExerciseRequest {

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
}
