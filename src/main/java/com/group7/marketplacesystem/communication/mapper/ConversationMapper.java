package com.group7.marketplacesystem.communication.mapper;

import com.group7.marketplacesystem.communication.dto.request.CreateConversationRequest;
import com.group7.marketplacesystem.communication.dto.response.ConversationResponse;
import com.group7.marketplacesystem.communication.entity.Conversation;
import com.group7.marketplacesystem.identity.entity.User;

import java.time.Instant;

public class ConversationMapper {

    public static Conversation fromRequest(CreateConversationRequest req, User buyer, User seller) {
        if (req == null || buyer == null || seller == null) return null;

        Conversation conv = new Conversation();
        conv.setBuyer(buyer);
        conv.setSeller(seller);

        // Khởi tạo mặc định
        conv.setUnreadCountBuyer(0);
        conv.setUnreadCountSeller(0);
        conv.setIsDeletedBuyer(false);
        conv.setIsDeletedSeller(false);
        conv.setCreatedAt(Instant.now());
        conv.setUpdatedAt(Instant.now());

        return conv;
    }


    public static ConversationResponse toResponse(Conversation entity) {
        if (entity == null) return null;

        ConversationResponse res = new ConversationResponse();
        res.setConversationId(entity.getId());
        res.setBuyerId(entity.getBuyer().getId());
        res.setSellerId(entity.getSeller().getId());
        res.setLastMessage(entity.getLastMessage());
        res.setLastMessageTime(entity.getLastMessageTime());
        res.setUnreadCountBuyer(entity.getUnreadCountBuyer());
        res.setUnreadCountSeller(entity.getUnreadCountSeller());
        
        if (entity.getBuyer() != null) {
            res.setBuyerName(entity.getBuyer().getFullName());
            res.setBuyerAvatar(entity.getBuyer().getAvatarUrl());
        }
        
        if (entity.getSeller() != null) {
            res.setSellerName(entity.getSeller().getFullName());
            res.setSellerAvatar(entity.getSeller().getAvatarUrl());
        }

        return res;
    }
}
