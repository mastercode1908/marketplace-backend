package com.group7.marketplacesystem.communication.controller;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.communication.MessagePayload.MessagePayload;
import com.group7.marketplacesystem.communication.dto.request.SendMessageRequest;
import com.group7.marketplacesystem.communication.dto.response.ChatMessageResponse;
import com.group7.marketplacesystem.communication.entity.Conversation;
import com.group7.marketplacesystem.communication.repository.ConversationRepository;
import com.group7.marketplacesystem.communication.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;

    @MessageMapping("/chat.send")
    public void sendMessage(MessagePayload payload) {

        // Convert payload → SendMessageRequest
        SendMessageRequest req = new SendMessageRequest();
        req.setConversationId(payload.getConversationId());
        req.setSenderId(payload.getSenderId());
        req.setMessageType(payload.getMessageType());
        req.setContent(payload.getContent());
        req.setFileUrl(payload.getFileUrl());

        // Lưu message vào DB
        ChatMessageResponse messageResponse = chatMessageService.sendMessage(req);

        // Lấy conversation để xác định receiver
        Conversation conversation = conversationRepository.findById(payload.getConversationId())
                .orElseThrow(() -> new ApiException(ErrorCode.CONVERSATION_NOT_FOUND));

        Integer receiverId;
        if (payload.getSenderId().equals(conversation.getBuyer().getId())) {
            receiverId = conversation.getSeller().getId();
        } else {
            receiverId = conversation.getBuyer().getId();
        }

        // Gửi realtime tới người nhận
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/messages",
                messageResponse
        );
    }
}