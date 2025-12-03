package com.group7.marketplacesystem.communication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationResponse {
    private Integer conversationId;
    private Integer buyerId;
    private Integer sellerId;
    private String lastMessage;
    private Instant lastMessageTime;
    private Integer unreadCountBuyer;
    private Integer unreadCountSeller;
    private String buyerName;
    private String buyerAvatar;
    private String sellerName;
    private String sellerAvatar;
}
