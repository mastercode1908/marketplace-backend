package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductResponse {
    private Integer productId;           // ID sản phẩm
    private String productName;          // Tên sản phẩm
    private String imageUrl;             // URL ảnh sản phẩm
    private Long soldQuantity;           // Số lượng đã bán
    private Double totalRevenue;         // Tổng doanh thu từ sản phẩm
    private Double commissionAmount;     // Tổng hoa hồng (7% của revenue)
}
