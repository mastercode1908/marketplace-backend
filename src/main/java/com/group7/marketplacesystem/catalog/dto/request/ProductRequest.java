package com.group7.marketplacesystem.catalog.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * DTO for {@link com.group7.marketplacesystem.catalog.entity.Product}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {

    private Integer categoryId;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stockQuantity;

    private String productStatus;

    private String text;
}






//@FieldDefaults(level = AccessLevel.PRIVATE)
//chi can khi k muon go private