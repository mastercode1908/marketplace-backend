package com.group7.marketplacesystem.identity.dto.response;

import com.group7.marketplacesystem.catalog.dto.response.ProductReportResponse;
import com.group7.marketplacesystem.catalog.dto.response.ProductResponse;
import com.group7.marketplacesystem.commerce.order.dto.response.OrderResponse;
import com.group7.marketplacesystem.infrastructure.dto.response.SystemlogResponse;
import com.group7.marketplacesystem.promotion.dto.response.SellerPackageResponse;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class UserDetailResponse {
    private Integer id;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private Boolean gender;
    private String avatarUrl;
    private String role;
    private String userStatus;
    private Instant createdAt;
    private Instant updatedAt;

    // Buyer specific fields
    private String address;
    private LocalDate dateOfBirth;

    // Seller specific fields
    private String shopName;
    private String shopAddress;
    private String shopDescription;
    private String taxCode;
    private Integer ratingCount;
    private Integer totalSales;

    // Related data
    private List<OrderResponse> orderHistory;
    private List<SystemlogResponse> systemLogs;
    private List<ProductReportResponse> productReports;
    private List<ProductResponse> products; // For sellers
    private List<SellerPackageResponse> sellerPackages; // For sellers
}
