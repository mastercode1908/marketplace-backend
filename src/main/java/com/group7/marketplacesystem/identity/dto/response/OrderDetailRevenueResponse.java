package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailRevenueResponse {
    private Integer orderId;
    private Instant orderDate;
    private String orderStatus;
    
    // Seller info
    private Integer sellerId;
    private String shopName;
    private String sellerEmail;
    
    // Buyer info
    private Integer buyerId;
    private String buyerName;
    private String buyerEmail;
    
    // Revenue info
    private BigDecimal totalAmount; // Tổng tiền hàng
    private BigDecimal discountAmount; // Giảm giá
    private BigDecimal shippingFee; // Phí ship
    private BigDecimal finalAmount; // Tổng thanh toán (doanh thu từ seller)
    private BigDecimal commission; // Hoa hồng admin (7% của finalAmount)
    
    // Order items
    private List<OrderItemDetail> items;
    
    // Payment info
    private String paymentMethod;
    private String paymentStatus;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDetail {
        private Integer productId;
        private String productName;
        private String categoryName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal; // quantity * unitPrice
        private BigDecimal itemCommission; // 7% của subtotal
    }
}



