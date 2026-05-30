package com.example.fitness.ai.vo;

import lombok.Data;

import java.util.List;

@Data
public class AiChatResponseVO {

    private String answer;

    private String suggestionType;

    private List<String> references;
}
