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
public class NotificationResponse {
    private Integer id;
    private String title;
    private String message;
    private String type;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant sentAt;
    private Long recipientCount; // Number of users who received this notification
}
