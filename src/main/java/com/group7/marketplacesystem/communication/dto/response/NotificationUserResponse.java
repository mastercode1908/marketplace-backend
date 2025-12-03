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
public class NotificationUserResponse {

    private Integer id;
    private String title;
    private String message;
    private Instant createdAt;
    private Boolean isRead;
}
