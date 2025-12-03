package com.group7.marketplacesystem.commerce.shipping.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO cho response item trong giỏ hàng
 */
@Data
public class CartItemResponse {
    private Integer itemId;
    private Integer productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private Integer sellerId;
    private String sellerName;
    private Integer stockQuantity; // Số lượng tồn kho để validate khi tăng số lượng
}


