package com.group7.marketplacesystem.commerce.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueBySellerDTO {
    private Integer sellerId;
    private String sellerName;
    private String sellerEmail;
    private BigDecimal revenue;
    private Long orderCount;
    private BigDecimal codAmount;
    private BigDecimal onlineAmount;
}





