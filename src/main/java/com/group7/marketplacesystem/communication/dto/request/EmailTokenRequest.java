package com.group7.marketplacesystem.communication.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailTokenRequest {
    private Integer userId;     // ID người dùng
    private String userEmail;   // Email người dùng
}
