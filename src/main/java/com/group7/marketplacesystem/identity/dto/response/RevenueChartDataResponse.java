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
public class RevenueChartDataResponse {
    private List<RevenueDataPoint> dailyData; // For current month
    private List<RevenueDataPoint> monthlyData; // For current year
    private BigDecimal currentPeriodTotal;
    private BigDecimal previousPeriodTotal;
    private Double changePercent;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueDataPoint {
        private String date; // Format: "YYYY-MM-DD" or "YYYY-MM"
        private BigDecimal revenue;
    }
}






