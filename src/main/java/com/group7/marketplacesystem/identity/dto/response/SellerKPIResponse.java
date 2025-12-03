package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerKPIResponse {
    private long totalProducts;       // Tổng số sản phẩm đang bán
    private long totalOrders;         // Tổng số đơn đã nhận
    private double totalRevenue;      // Tổng doanh thu đã kiếm được
    private double averageRating;     // Điểm trung bình của shop
    private long pendingOrders;       // Đơn đang chờ xử lý
    private long completedOrders ;     // Đơn đã hoàn tất
    private long cancelledOrders;     // Đơn bị hủy
    private double totalCommission;   // Tổng hoa hồng (7% của doanh thu)
    private double commissionRate;    // Tỷ lệ hoa hồng (7.0%)
}
