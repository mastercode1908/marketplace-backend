package com.group7.marketplacesystem.catalog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishlistResponse {
    private Integer BuyerId;
    private Integer productId;
    private Instant addedAt;
    private String productName;
    private Integer stockQuantity;
    private BigDecimal price;
    private Integer sellerId;
    private String shopName;
    private List<ProductMediaResponse> media;
}