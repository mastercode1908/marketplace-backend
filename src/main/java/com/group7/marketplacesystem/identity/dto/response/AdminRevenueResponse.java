package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRevenueResponse {
    // Tổng doanh thu từ seller (final_amount của đơn Delivered)
    private BigDecimal totalRevenue;
    
    // Hoa hồng admin nhận được (7% của totalRevenue)
    private BigDecimal commission;
    
    // Số đơn hàng đã giao
    private Long totalOrders;
    
    // Trung bình giá trị đơn hàng
    private BigDecimal averageOrderValue;
    
    // Dữ liệu theo thời gian
    private List<RevenueDataPoint> revenueByPeriod;
    
    // Top sellers theo doanh thu
    private List<TopSellerRevenue> topSellers;
    
    // Doanh thu theo danh mục
    private List<CategoryRevenue> revenueByCategory;
    
    // Thống kê theo seller (nếu filter)
    private SellerRevenueStats sellerStats;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueDataPoint {
        private String period; // "YYYY-MM-DD" hoặc "YYYY-MM" hoặc "YYYY-Q1"
        private BigDecimal revenue; // Doanh thu từ seller
        private BigDecimal commission; // Hoa hồng admin (7%)
        private Long orderCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSellerRevenue {
        private Integer sellerId;
        private String shopName;
        private BigDecimal revenue; // Doanh thu từ seller
        private BigDecimal commission; // Hoa hồng admin (7%)
        private Long orderCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryRevenue {
        private Integer categoryId;
        private String categoryName;
        private BigDecimal revenue; // Doanh thu từ seller
        private BigDecimal commission; // Hoa hồng admin (7%)
        private Long orderCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SellerRevenueStats {
        private Integer sellerId;
        private String shopName;
        private BigDecimal totalRevenue;
        private BigDecimal totalCommission;
        private Long totalOrders;
        private BigDecimal averageOrderValue;
    }
}



