package com.group7.marketplacesystem.communication.service.impl;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.communication.dto.request.CreateConversationRequest;
import com.group7.marketplacesystem.communication.dto.response.ConversationResponse;
import com.group7.marketplacesystem.communication.entity.Conversation;
import com.group7.marketplacesystem.communication.mapper.ConversationMapper;
import com.group7.marketplacesystem.communication.repository.ChatmessageRepository;
import com.group7.marketplacesystem.communication.repository.ConversationRepository;
import com.group7.marketplacesystem.communication.service.ConversationService;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ChatmessageRepository chatmessageRepository;

    @Override
    @Transactional
    public ConversationResponse createConversation(CreateConversationRequest req) {

        User buyer = userRepository.findById(req.getBuyerId())
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));
        User seller = userRepository.findById(req.getSellerId())
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));

        // Mapper
        Conversation conv = ConversationMapper.fromRequest(req, buyer, seller);

        conversationRepository.save(conv);

        return ConversationMapper.toResponse(conv);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        List<Conversation> conversations = conversationRepository.findByBuyerOrSeller(user, user);

        return conversations.stream()
                .map(ConversationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getMyConversations() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        return getUserConversations(user.getId());
    }

    @Override
    @Transactional
    public ConversationResponse startConversation(Integer receiverId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        // Check if conversation exists
        Optional<Conversation> existingConv = conversationRepository.findByBuyerIdAndSellerId(sender.getId(),
                receiverId);
        if (existingConv.isPresent()) {
            return ConversationMapper.toResponse(existingConv.get());
        }

        // Check reverse (if sender is seller)
        existingConv = conversationRepository.findByBuyerIdAndSellerId(receiverId, sender.getId());
        if (existingConv.isPresent()) {
            return ConversationMapper.toResponse(existingConv.get());
        }

        // Create new
        Conversation conv = new Conversation();
        // Determine who is buyer/seller based on logic or just assign.
        // For simplicity, if sender is buyer role, assign as buyer.
        // But here we don't know roles easily.
        // Let's assume sender is buyer for now if role check is complex,
        // OR better: check roles.

        // Assuming logic: If I start conversation, I am the buyer unless I am
        // explicitly a seller context?
        // Actually, in marketplace, usually Buyer starts conversation.
        // But Seller can also reply.
        // Let's just assign sender as Buyer and Receiver as Seller for new conversation
        // if not exists.
        // Wait, what if Receiver is also a Buyer?
        // Ideally we should check roles.

        // For now, let's assume the one starting is Buyer.
        conv.setBuyer(sender);
        conv.setSeller(receiver);

        conversationRepository.save(conv);
        return ConversationMapper.toResponse(conv);
    }

    @Override
    @Transactional
    public void markAsRead(Integer conversationId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ApiException(ErrorCode.CONVERSATION_NOT_FOUND));

        // Update messages
        chatmessageRepository.markMessagesAsRead(conversationId, user.getId());

        // Update conversation unread count
        if (conversation.getBuyer().getId().equals(user.getId())) {
            conversation.setUnreadCountBuyer(0);
        } else if (conversation.getSeller().getId().equals(user.getId())) {
            conversation.setUnreadCountSeller(0);
        }

        conversationRepository.save(conversation);
    }
}
