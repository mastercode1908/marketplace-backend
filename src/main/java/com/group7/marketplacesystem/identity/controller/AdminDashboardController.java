package com.group7.marketplacesystem.identity.controller;

import com.group7.marketplacesystem.identity.dto.request.RevenueFilterRequest;
import com.group7.marketplacesystem.identity.dto.response.*;
import com.group7.marketplacesystem.identity.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/kpis")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<DashboardKPIsResponse> getKPIs() {
        return ResponseEntity.ok(dashboardService.getKPIs());
    }

    @GetMapping("/revenue-chart")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<RevenueChartDataResponse> getRevenueChart(
            @RequestParam(defaultValue = "daily") String period) {
        return ResponseEntity.ok(dashboardService.getRevenueChartData(period));
    }

    @GetMapping("/order-status-chart")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<OrderStatusChartResponse> getOrderStatusChart() {
        return ResponseEntity.ok(dashboardService.getOrderStatusChart());
    }

    @GetMapping("/user-growth-chart")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<UserGrowthChartResponse> getUserGrowthChart() {
        return ResponseEntity.ok(dashboardService.getUserGrowthChart());
    }

    @GetMapping("/top-sellers-products")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<TopSellersProductsResponse> getTopSellersAndProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getTopSellersAndProducts(limit));
    }

    @GetMapping("/category-distribution")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<CategoryDistributionResponse> getCategoryDistribution() {
        return ResponseEntity.ok(dashboardService.getCategoryDistribution());
    }
    
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<AdminRevenueResponse> getAdminRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "daily") String periodType,
            @RequestParam(required = false) Integer sellerId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false, defaultValue = "10") Integer topSellersLimit) {
        
        RevenueFilterRequest filter = RevenueFilterRequest.builder()
            .startDate(startDate)
            .endDate(endDate)
            .periodType(periodType)
            .sellerId(sellerId)
            .categoryId(categoryId)
            .topSellersLimit(topSellersLimit)
            .build();
        
        return ResponseEntity.ok(dashboardService.getAdminRevenue(filter));
    }
    
    @GetMapping("/revenue/orders")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<List<OrderDetailRevenueResponse>> getOrderDetails(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer sellerId,
            @RequestParam(required = false) Integer categoryId) {
        
        RevenueFilterRequest filter = RevenueFilterRequest.builder()
            .startDate(startDate)
            .endDate(endDate)
            .sellerId(sellerId)
            .categoryId(categoryId)
            .build();
        
        return ResponseEntity.ok(dashboardService.getOrderDetails(filter));
    }
}

