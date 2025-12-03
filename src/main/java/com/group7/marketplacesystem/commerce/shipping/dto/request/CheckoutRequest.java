package com.group7.marketplacesystem.commerce.shipping.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * DTO cho request checkout
 */
@Data
public class CheckoutRequest {
    @NotNull(message = "Address ID is required")
    private Integer addressId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // "VNPAY" hoặc "COD"

    private String note;

    private List<SellerPromotionRequest> promotions;
}













