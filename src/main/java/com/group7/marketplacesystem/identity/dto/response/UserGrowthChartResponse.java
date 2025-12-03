package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGrowthChartResponse {
    private List<UserGrowthDataPoint> buyerData;
    private List<UserGrowthDataPoint> sellerData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserGrowthDataPoint {
        private String month; // Format: "YYYY-MM"
        private Long count;
    }
}






