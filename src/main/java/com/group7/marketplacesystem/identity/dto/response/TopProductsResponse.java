package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductsResponse {

    private long productId;
    private String productName;
    private long totalSold;    // Tổng số lượng đã bán
    private double totalRevenue; // Doanh thu sản phẩm
}
