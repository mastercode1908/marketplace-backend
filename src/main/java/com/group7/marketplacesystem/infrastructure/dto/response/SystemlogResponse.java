package com.group7.marketplacesystem.infrastructure.dto.response;

import lombok.Data;

import java.time.Instant;

@Data
public class SystemlogResponse {
    private Integer id;
    private Integer userId;
    private String action;
    private String description;
    private Instant createdAt;
    private String ipAddress;
}
