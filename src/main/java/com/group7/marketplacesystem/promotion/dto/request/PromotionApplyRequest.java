package com.group7.marketplacesystem.promotion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionApplyRequest {

    private String code;
    private BigDecimal orderTotal;

    private Integer sellerId; // thêm để backend check voucher
}
