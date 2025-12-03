package com.group7.marketplacesystem.commerce.shipping.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho response checkout - hiển thị đơn hàng theo từng seller
 */
@Data
public class CheckoutResponse {
    private List<SellerOrderGroup> sellerOrders;
    private BigDecimal totalAmount;
    private BigDecimal totalShippingFee;
    private BigDecimal finalAmount;
}













