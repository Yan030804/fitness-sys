package com.example.fitness.admin.exercise.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateExerciseRequest {

    @NotBlank(message = "exerciseName cannot be blank")
    private String exerciseName;

    @NotBlank(message = "category cannot be blank")
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
