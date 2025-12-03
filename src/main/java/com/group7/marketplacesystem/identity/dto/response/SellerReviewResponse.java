package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerReviewResponse {
    private Integer sellerId;
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String shopName;
    private String shopAddress;
    private String shopDescription;
    private String taxCode;
    private String userStatus;
    private Instant createdAt;
    private Instant updatedAt;
}

