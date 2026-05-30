package com.example.fitness.ai.controller;

import com.example.fitness.ai.dto.AiChatRequest;
import com.example.fitness.ai.service.AiChatService;
import com.example.fitness.ai.vo.AiAppointmentAssistVO;
import com.example.fitness.ai.vo.AiChatResponseVO;
import com.example.fitness.common.api.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Validated
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public Result<AiChatResponseVO> chat(@Valid @RequestBody AiChatRequest request) {
        return Result.success(aiChatService.chat(request));
    }

    @PostMapping(path = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@Valid @RequestBody AiChatRequest request) {
        return aiChatService.streamChat(request);
    }

    @PostMapping("/appointment-assist")
    public Result<AiAppointmentAssistVO> appointmentAssist(@Valid @RequestBody AiChatRequest request) {
        return Result.success(aiChatService.appointmentAssist(request));
    }
}
