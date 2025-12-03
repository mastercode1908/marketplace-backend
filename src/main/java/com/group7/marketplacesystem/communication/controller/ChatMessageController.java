package com.group7.marketplacesystem.communication.controller;

import com.group7.marketplacesystem.communication.dto.request.CreateConversationRequest;
import com.group7.marketplacesystem.communication.dto.request.SendMessageRequest;
import com.group7.marketplacesystem.communication.dto.response.ChatMessageResponse;
import com.group7.marketplacesystem.communication.dto.response.ConversationResponse;
import com.group7.marketplacesystem.communication.service.ChatMessageService;
import com.group7.marketplacesystem.communication.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ConversationService conversationService;
    private final ChatMessageService messageService;

    // Tạo conversation mới
    @PostMapping("/conversations")
    public ConversationResponse createConversation(@RequestBody CreateConversationRequest request) {
        return conversationService.createConversation(request);
    }

    // Gửi tin nhắn
    @PostMapping("/messages/send")
    public ChatMessageResponse sendMessage(@RequestBody SendMessageRequest request) {
        return messageService.sendMessage(request);
    }

//    // Lấy danh sách conversation của 1 user
//    @GetMapping("/conversations/{userId}")
//    public List<ConversationResponse> getConversations(@PathVariable Integer userId) {
//        return conversationService.getUserConversations(userId);
//    }
//
    // Lấy danh sách tin nhắn trong 1 conversation
    @GetMapping("/conversations/{conversationId}/messages")
    public List<ChatMessageResponse> getMessages(@PathVariable Integer conversationId) {
        return messageService.getMessagesByConversation(conversationId);
    }
}
