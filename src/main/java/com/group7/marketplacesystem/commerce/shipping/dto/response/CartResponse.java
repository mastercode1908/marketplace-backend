package com.group7.marketplacesystem.commerce.shipping.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho response giỏ hàng
 */
@Data
public class CartResponse {
    private Integer cartId;
    private Integer buyerId;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
}













