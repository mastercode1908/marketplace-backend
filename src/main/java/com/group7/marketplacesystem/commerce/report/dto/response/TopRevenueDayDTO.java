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
public class TopRevenueDayDTO {
    private LocalDate date;
    private BigDecimal revenue;
    private Long orderCount;
    private Integer rank;
}





