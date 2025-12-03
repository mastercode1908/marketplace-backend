package com.group7.marketplacesystem.catalog.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewRequest {

    private Integer productId;

    private Integer buyerId;

    private Integer rating;

    private Boolean isVerifiedPurchase;

    private String comment;

    private Integer orderDetailId;
}
