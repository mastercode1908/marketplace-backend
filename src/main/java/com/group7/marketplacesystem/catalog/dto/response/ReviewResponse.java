package com.group7.marketplacesystem.catalog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewResponse {

    private Integer reviewId;

    private Integer buyerId;

    private Integer productId;

    private Integer rating;

    private String comment;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant deletedAt;

    private Boolean isVerifiedPurchase;

    private String buyerName;

    private String avatarUrl;
}
