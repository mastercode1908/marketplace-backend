package com.group7.marketplacesystem.commerce.order.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class OrderResponse {
    private Integer id;
    private Integer buyerId;
    private Integer sellerId;
    private Integer cartId;
    private Integer promotionId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String note;
    private Instant orderDate;
    private String orderStatus;
}
