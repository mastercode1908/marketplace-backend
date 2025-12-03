package com.group7.marketplacesystem.communication.repository;

import com.group7.marketplacesystem.communication.entity.Chatmessage;
import com.group7.marketplacesystem.communication.entity.Conversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatmessageRepository extends JpaRepository<Chatmessage, Integer> {
    List<Chatmessage> findByConversationIdOrderBySentAtAsc(Integer conversationId);

    List<Chatmessage> findByConversationIdOrderBySentAtDesc(Integer conversationId, Pageable pageable);

    long countByConversationIdAndIsReadFalseAndSenderIdNot(Integer conversationId, Integer recipientId);

    List<Chatmessage> findByConversationOrderBySentAt(Conversation conversation);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Chatmessage c SET c.isRead = true WHERE c.conversation.id = :conversationId AND c.sender.id <> :readerId AND c.isRead = false")
    void markMessagesAsRead(@org.springframework.data.repository.query.Param("conversationId") Integer conversationId,
            @org.springframework.data.repository.query.Param("readerId") Integer readerId);
}