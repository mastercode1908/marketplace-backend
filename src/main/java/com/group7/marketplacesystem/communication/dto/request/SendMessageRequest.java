package com.group7.marketplacesystem.communication.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendMessageRequest {
    private Integer conversationId;
    private Integer senderId;
    private String messageType;  // TEXT / FILE / IMAGE / VIDEO
    private String content;
    private String fileUrl;
}