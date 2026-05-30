package com.example.fitness.exercise.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ExercisePageQuery {

    @Min(value = 1, message = "pageNum must be >= 1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "pageSize must be >= 1")
    private Integer pageSize = 10;

    private String category;
    private String bodyPart;
    private String difficulty;

    public int getOffset() {
        int currentPageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int currentPageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        return (currentPageNum - 1) * currentPageSize;
    }
}
