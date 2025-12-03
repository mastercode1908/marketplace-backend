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
public class CategoryDistributionResponse {
    private List<CategoryData> productCountByCategory;
    private List<CategoryData> revenueByCategory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryData {
        private Integer categoryId;
        private String categoryName;
        private Long count; // For product count
        private BigDecimal revenue; // For revenue
    }
}






