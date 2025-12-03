package com.group7.marketplacesystem.commerce.report.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodType; // "daily", "monthly", "quarterly", "yearly"
    private Integer sellerId; // null for all sellers
}





