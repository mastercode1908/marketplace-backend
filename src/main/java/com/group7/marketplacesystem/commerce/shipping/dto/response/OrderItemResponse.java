package com.group7.marketplacesystem.commerce.shipping.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO cho response item trong đơn hàng
 */
@Data
public class OrderItemResponse {
    private Integer productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private Boolean isReviewed;
    private Boolean isReported;
    private Integer orderDetailId;
}













