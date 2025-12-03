package com.group7.marketplacesystem.promotion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionApplyResponse {
    private String code;
    private BigDecimal discountAmount;
    private String message;
}
