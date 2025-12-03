package com.group7.marketplacesystem.commerce.shipping.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CalculateFeeRequest {
    @NotNull(message = "Seller ID is required")
    private Integer sellerId;

    @NotNull(message = "To District ID is required")
    private Integer toDistrictId;

    @NotNull(message = "To Ward Code is required")
    private String toWardCode;

    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Quantity is required")
    private Integer quantity;
}
