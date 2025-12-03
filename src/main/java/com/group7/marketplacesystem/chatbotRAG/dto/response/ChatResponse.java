package com.group7.marketplacesystem.chatbotRAG.dto.response;

import lombok.Data;

import java.util.List;

/**
 * Response DTO for chat endpoint.
 */
@Data
public class ChatResponse {
    private String message;
    private List<ProductInfo> products;
}
