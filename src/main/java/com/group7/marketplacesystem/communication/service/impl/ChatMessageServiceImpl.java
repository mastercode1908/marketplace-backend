package com.group7.marketplacesystem.communication.service.impl;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.communication.dto.request.SendMessageRequest;
import com.group7.marketplacesystem.communication.dto.response.ChatMessageResponse;
import com.group7.marketplacesystem.communication.entity.Chatmessage;
import com.group7.marketplacesystem.communication.entity.Conversation;
import com.group7.marketplacesystem.communication.mapper.ChatMessageMapper;
import com.group7.marketplacesystem.communication.repository.ChatmessageRepository;
import com.group7.marketplacesystem.communication.repository.ConversationRepository;
import com.group7.marketplacesystem.communication.service.ChatMessageService;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ChatmessageRepository chatmessageRepository;

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(SendMessageRequest req) {

        Conversation conversation = conversationRepository.findById(req.getConversationId())
                .orElseThrow(() -> new ApiException(ErrorCode.CONVERSATION_NOT_FOUND));

        User sender = userRepository.findById(req.getSenderId())
                .orElseThrow(() -> new ApiException(ErrorCode.SENDER_NOT_FOUND));

        Chatmessage msg = ChatMessageMapper.fromRequest(req, conversation, sender);

        chatmessageRepository.save(msg);

        conversation.setLastMessage(req.getContent() != null ? req.getContent() : "[FILE]");
        conversation.setLastMessageTime(msg.getSentAt());

        int unreadBuyer = conversation.getUnreadCountBuyer() == null ? 0 : conversation.getUnreadCountBuyer();
        int unreadSeller = conversation.getUnreadCountSeller() == null ? 0 : conversation.getUnreadCountSeller();

        if (sender.getId().equals(conversation.getBuyer().getId())) {
            conversation.setUnreadCountSeller(unreadSeller + 1);
        } else {
            conversation.setUnreadCountBuyer(unreadBuyer + 1);
        }

        conversationRepository.save(conversation);

        // Mapper
        return ChatMessageMapper.toResponse(msg);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessagesByConversation(Integer conversationId) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ApiException(ErrorCode.CONVERSATION_NOT_FOUND));

        List<Chatmessage> messages = chatmessageRepository.findByConversationOrderBySentAt(conversation);

        return messages.stream()
                .map(ChatMessageMapper::toResponse)
                .collect(Collectors.toList());
    }
}
