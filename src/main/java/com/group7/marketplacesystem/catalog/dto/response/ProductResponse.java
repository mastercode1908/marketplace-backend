package com.group7.marketplacesystem.catalog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for {@link com.group7.marketplacesystem.catalog.entity.Product}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {

    private Integer id;

    private Integer categoryId;
    private String categoryName;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stockQuantity;

    private String productStatus;

    private Instant createdAt;

    private Instant updatedAt;
}