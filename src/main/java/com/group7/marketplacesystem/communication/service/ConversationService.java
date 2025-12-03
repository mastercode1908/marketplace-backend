package com.group7.marketplacesystem.communication.service;

import com.group7.marketplacesystem.communication.dto.request.CreateConversationRequest;
import com.group7.marketplacesystem.communication.dto.response.ConversationResponse;

import java.util.List;

public interface ConversationService {

    ConversationResponse createConversation(CreateConversationRequest req);

    List<ConversationResponse> getUserConversations(Integer userId);

    List<ConversationResponse> getMyConversations();

    ConversationResponse startConversation(Integer receiverId);

    void markAsRead(Integer conversationId);
}
