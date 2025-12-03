package com.group7.marketplacesystem.communication.repository;

import com.group7.marketplacesystem.communication.entity.Conversation;
import com.group7.marketplacesystem.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
    Optional<Conversation> findByBuyerIdAndSellerId(Integer buyerId, Integer sellerId);
    List<Conversation> findByBuyerIdOrSellerIdOrderByLastMessageTimeDesc(Integer buyerId, Integer sellerId);

    List<Conversation> findByBuyerOrSeller(User user, User user1);
}