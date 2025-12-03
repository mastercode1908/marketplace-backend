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
public class ProductInfoResponse {

    private Integer productId;
    private Integer sellerId;
    private Integer categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer weight;
    private Integer stockQuantity;
    private String productStatus;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private List<ProductMediaResponse> media;

    // getters & setters
}
