package com.group7.marketplacesystem.catalog.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductCreateRequest {

    @NotNull
    private Integer categoryId;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    @NotNull
    private Integer weight; // gram

    private Integer stockQuantity = 0;

    private String productStatus = "Pending";

    private List<ProductMediaRequest> media;


}
