package com.group7.marketplacesystem.promotion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionResponse {

    private Integer id;
    private String ownerType;
    private Integer ownerId;
    private String promotionCode;
    private String description;
    private String discountType;
    private Integer usageLimit;
    private Integer usedCount;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String promotionStatus;
    private Instant createdAt;
    private Instant updatedAt;
}
