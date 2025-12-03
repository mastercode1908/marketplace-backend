package com.group7.marketplacesystem.commerce.shipping.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO cho response chi tiết đơn hàng
 */
@Data
public class OrderDetailResponse {
    private Integer orderId;
    private Integer buyerId;
    private String buyerName;
    private Integer sellerId;
    private String sellerName;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal finalAmount;
    private String orderStatus;
    private String shippingStatus; // Trạng thái vận chuyển
    private String trackingNumber;
    private String ghnOrderCode;
    private Instant orderDate;
    private List<OrderItemResponse> items;
    private BuyerAddressResponse shippingAddress;
}



