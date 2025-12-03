package com.group7.marketplacesystem.commerce.shipping.dto.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SellerPromotionRequest {
    private Integer sellerId;
    private String promotionCode;
}
