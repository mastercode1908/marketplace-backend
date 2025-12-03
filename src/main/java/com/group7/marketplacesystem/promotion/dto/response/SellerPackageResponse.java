package com.group7.marketplacesystem.promotion.dto.response;

import lombok.Data;

import java.time.Instant;

@Data
public class SellerPackageResponse {
    private Integer id;
    private Integer sellerId;
    private Integer packageId;
    private Instant startDate;
    private Instant endDate;
    private Integer remainingUsage;
    private String status;
    private Instant createdAt;
    private String packageName;
}
