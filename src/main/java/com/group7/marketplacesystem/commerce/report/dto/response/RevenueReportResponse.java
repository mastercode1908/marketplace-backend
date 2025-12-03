package com.group7.marketplacesystem.commerce.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodType;
    
    // Tổng quan
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private BigDecimal totalCodAmount;
    private BigDecimal totalOnlineAmount;
    
    // Doanh thu theo kỳ
    private List<RevenueByPeriodDTO> revenueByPeriod;
    
    // Top 10 ngày có doanh thu cao nhất
    private List<TopRevenueDayDTO> topRevenueDays;
    
    // Doanh thu theo seller
    private List<RevenueBySellerDTO> revenueBySeller;
}





