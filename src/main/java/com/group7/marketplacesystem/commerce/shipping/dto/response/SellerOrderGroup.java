package com.group7.marketplacesystem.commerce.shipping.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho nhóm đơn hàng theo seller
 */
@Data
public class SellerOrderGroup {
    private Integer sellerId;
    private String sellerName;
    private List<CartItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal total;
}













