package com.group7.marketplacesystem.chatbotRAG.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for chat endpoint.
 */
@Data
public class ChatRequest {

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 1000, message = "Message too long (max 1000 characters)")
    private String message;
}
