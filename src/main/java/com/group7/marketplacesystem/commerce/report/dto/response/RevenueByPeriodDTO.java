package com.group7.marketplacesystem.commerce.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueByPeriodDTO {
    private LocalDate period;
    private String periodLabel; // "2024-01-01", "Tháng 1/2024", "Q1/2024", "2024"
    private BigDecimal revenue;
    private Long orderCount;
}





