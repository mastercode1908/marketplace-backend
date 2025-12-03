package com.group7.marketplacesystem.communication.mapper;

import com.group7.marketplacesystem.communication.dto.request.SendMessageRequest;
import com.group7.marketplacesystem.communication.dto.response.ChatMessageResponse;
import com.group7.marketplacesystem.communication.entity.Chatmessage;
import com.group7.marketplacesystem.communication.entity.Conversation;
import com.group7.marketplacesystem.identity.entity.User;

import java.time.Instant;

public class ChatMessageMapper {

    public static Chatmessage fromRequest(SendMessageRequest req, Conversation conversation, User sender) {
        if (req == null || conversation == null || sender == null) return null;

        Chatmessage msg = new Chatmessage();
        msg.setConversation(conversation);
        msg.setSender(sender);
        msg.setMessageType(req.getMessageType() != null ? req.getMessageType() : "TEXT");
        msg.setContent(req.getContent());
        msg.setFileUrl(req.getFileUrl());
        msg.setSentAt(Instant.now());
        msg.setIsRead(false);

        // default cho deleted flags
        msg.setIsDeletedBuyer(false);
        msg.setIsDeletedSeller(false);

        return msg;
    }


    public static ChatMessageResponse toResponse(Chatmessage entity) {
        if (entity == null) return null;

        ChatMessageResponse res = new ChatMessageResponse();
        res.setMessageId(entity.getId());
        res.setConversationId(entity.getConversation().getId());
        res.setSenderId(entity.getSender().getId());
        res.setMessageType(entity.getMessageType());
        res.setContent(entity.getContent());
        res.setFileUrl(entity.getFileUrl());
        res.setSentAt(entity.getSentAt());
        res.setIsRead(entity.getIsRead());

        return res;
    }

}
