package com.example.fitness.recommendation.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RecommendationGenerateRequest {

    @NotEmpty(message = "recommendTypes cannot be empty")
    private List<String> recommendTypes;
}
