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
public class ChatMessageResponse {
    private Integer messageId;
    private Integer conversationId;
    private Integer senderId;
    private String messageType;
    private String content;
    private String fileUrl;
    private Instant sentAt;
    private Boolean isRead;
}

