package com.group7.marketplacesystem.commerce.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCartItemRequest {
    @NotNull
    private Integer productId;

    @Min(1)
    private Integer quantity;
}

