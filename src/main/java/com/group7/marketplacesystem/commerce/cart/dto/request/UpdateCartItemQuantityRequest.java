package com.group7.marketplacesystem.commerce.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO cho request cập nhật số lượng sản phẩm trong giỏ hàng
 */
@Data
public class UpdateCartItemQuantityRequest {
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}













