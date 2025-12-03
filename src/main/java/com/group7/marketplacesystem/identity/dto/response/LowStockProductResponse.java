package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockProductResponse {
    private long productId;
    private String productName;
    private int stockQuantity;     // Số lượng còn lại
    private int threshold;         // Ngưỡng cảnh báo
}
