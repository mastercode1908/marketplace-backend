package com.group7.marketplacesystem.chatbotRAG.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for product information in RAG responses.
 */
@Data
public class ProductInfo {
    private Integer productId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String categoryName;
    private String sellerName;
    private String imageUrl;
}
