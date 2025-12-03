package com.group7.marketplacesystem.chatbotRAG.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for search endpoint.
 */
@Data
public class SearchRequest {

    @NotBlank(message = "Query cannot be empty")
    private String query;

    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 20, message = "Limit cannot exceed 20")
    private Integer limit = 5;
}
