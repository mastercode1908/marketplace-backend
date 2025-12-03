package com.group7.marketplacesystem.chatbotRAG.controller;

import com.group7.marketplacesystem.common.response.ApiResponse;
import com.group7.marketplacesystem.chatbotRAG.service.ChatbotIfElseService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotIfElseController {

    private final ChatbotIfElseService chatbotIfElseService;

    @PostMapping("/message")
    public ApiResponse<String> sendMessage(@RequestBody ChatMessage request) {
        String response = chatbotIfElseService.processMessage(request.getMessage());
        return ApiResponse.success(response);
    }

    @Data
    public static class ChatMessage {
        private String message;
    }
}
