package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardKPIsResponse {
    // Revenue KPIs (Doanh thu từ seller - final_amount của đơn Delivered)
    private BigDecimal revenueThisMonth;
    private BigDecimal revenueThisYear;
    private Double revenueChangePercent;
    
    // Commission KPIs (Hoa hồng admin nhận được - 7% của revenue)
    private BigDecimal commissionThisMonth;
    private BigDecimal commissionThisYear;
    private Double commissionChangePercent;

    // Order KPIs
    private Long ordersToday;
    private Long ordersCompleted;
    private Long ordersProcessing;
    private Long ordersCancelled;

    // User KPIs
    private Long newUsersToday;
    private Long newBuyersToday;
    private Long newSellersToday;
    private Long activeUsers;
    private Long bannedUsers;

    // Product KPIs
    private Long totalProducts;
    private Long activeProducts;
    private Long outOfStockProducts;
    private Long pendingProducts;

    // Seller KPIs
    private Long newSellersThisMonth;
    private Long activeSellers;
    private Long bannedSellers;
}




