package com.group7.marketplacesystem.communication.MessagePayload;

import lombok.Data;

import java.time.Instant;

@Data
public class MessagePayload {
    private Integer conversationId;
    private Integer senderId;
    private String messageType;
    private String content;
    private String fileUrl;
    private Instant sentAt;  // ← bắt buộc để client hiển thị thời gian ngay lập tức
}
