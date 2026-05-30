package com.example.fitness.exercise.support;

import org.springframework.util.StringUtils;

public final class ExerciseDifficultyNormalizer {

    private ExerciseDifficultyNormalizer() {
    }

    public static String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return switch (trimmed) {
            case "1" -> "初级";
            case "2" -> "中级";
            case "3" -> "高级";
            default -> trimmed;
        };
    }
}
