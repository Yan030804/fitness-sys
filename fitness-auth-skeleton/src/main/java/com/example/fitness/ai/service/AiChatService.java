package com.example.fitness.ai.service;

import com.example.fitness.ai.dto.AiChatRequest;
import com.example.fitness.ai.vo.AiAppointmentAssistVO;
import com.example.fitness.ai.vo.AiChatResponseVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiChatService {

    AiChatResponseVO chat(AiChatRequest request);

    SseEmitter streamChat(AiChatRequest request);

    AiAppointmentAssistVO appointmentAssist(AiChatRequest request);
}
