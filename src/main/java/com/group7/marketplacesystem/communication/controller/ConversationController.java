package com.group7.marketplacesystem.communication.controller;

import com.group7.marketplacesystem.common.response.ApiResponse;
import com.group7.marketplacesystem.communication.dto.response.ConversationResponse;
import com.group7.marketplacesystem.communication.service.ConversationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public ApiResponse<List<ConversationResponse>> getMyConversations() {
        return ApiResponse.<List<ConversationResponse>>builder()
                .success(true)
                .message("Request successful")
                .data(conversationService.getMyConversations())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @PostMapping("/start")
    public ApiResponse<ConversationResponse> startConversation(@RequestBody StartConversationRequest request) {
        return ApiResponse.<ConversationResponse>builder()
                .success(true)
                .message("Request successful")
                .data(conversationService.startConversation(request.getReceiverId()))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @PutMapping("/{conversationId}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Integer conversationId) {
        conversationService.markAsRead(conversationId);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Conversation marked as read")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Data
    public static class StartConversationRequest {
        private Integer receiverId;
    }
}
