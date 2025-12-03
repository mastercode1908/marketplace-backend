package com.group7.marketplacesystem.communication.service;

import com.group7.marketplacesystem.communication.dto.request.SendMessageRequest;
import com.group7.marketplacesystem.communication.dto.response.ChatMessageResponse;

import java.util.List;

public interface ChatMessageService {
    ChatMessageResponse sendMessage(SendMessageRequest req);

    List<ChatMessageResponse> getMessagesByConversation(Integer conversationId);


}

