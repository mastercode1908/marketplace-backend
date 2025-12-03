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
public class TopSellersProductsResponse {
    private List<TopSellerData> topSellers;
    private List<TopProductData> topProducts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSellerData {
        private Integer sellerId;
        private String shopName;
        private BigDecimal revenue;
        private Long orderCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProductData {
        private Integer productId;
        private String productName;
        private Long salesCount;
        private BigDecimal revenue;
    }
}






