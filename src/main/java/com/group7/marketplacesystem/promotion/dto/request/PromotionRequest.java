package com.group7.marketplacesystem.promotion.dto.request;

import jakarta.validation.constraints.*;
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
public class PromotionRequest {

    @NotBlank(message = "Promotion code is required")
    @Size(max = 100, message = "Promotion code must not exceed 100 characters")
    private String promotionCode;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Discount type is required")
    @Pattern(regexp = "PERCENT|AMOUNT", message = "Discount type must be PERCENT or AMOUNT")
    private String discountType;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.01", message = "Max discount amount must be greater than 0")
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Pattern(regexp = "Active|Inactive|Expired", message = "Promotion status must be Active, Inactive, or Expired")
    private String promotionStatus;
}
