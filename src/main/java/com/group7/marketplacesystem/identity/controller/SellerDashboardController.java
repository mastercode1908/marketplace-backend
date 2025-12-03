package com.group7.marketplacesystem.identity.controller;

import com.group7.marketplacesystem.common.security.CustomUserDetails;
import com.group7.marketplacesystem.identity.dto.response.OrderCommissionResponse;
import com.group7.marketplacesystem.identity.dto.response.OrderGrowthChartResponse;
import com.group7.marketplacesystem.identity.dto.response.ReviewStatsResponse;
import com.group7.marketplacesystem.identity.dto.response.SellerKPIResponse;
import com.group7.marketplacesystem.identity.dto.response.TopProductResponse;
import com.group7.marketplacesystem.identity.service.SellerDashboardService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seller/dashboard")
@AllArgsConstructor
public class SellerDashboardController {

    private final SellerDashboardService sellerDashboardService;

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/kpi")
    public ResponseEntity<SellerKPIResponse> getKPI() {
        Integer id = getCurrentSellerId();
        return ResponseEntity.ok(sellerDashboardService.getKPI(id));
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/order-growth")
    public ResponseEntity<OrderGrowthChartResponse> getOrderGrowth(
            @RequestParam(defaultValue = "daily") String period) {
        Integer id = getCurrentSellerId();
        return ResponseEntity.ok(sellerDashboardService.getOrderGrowthChart(id, period));
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/review-stats")
    public ResponseEntity<ReviewStatsResponse> getReviewStats() {
        Integer id = getCurrentSellerId();
        return ResponseEntity.ok(sellerDashboardService.getReviewStats(id));
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/commissions")
    public ResponseEntity<List<OrderCommissionResponse>> getOrderCommissions() {
        Integer id = getCurrentSellerId();
        return ResponseEntity.ok(sellerDashboardService.getOrderCommissions(id));
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductResponse>> getTopProducts(
            @RequestParam(defaultValue = "5") int limit) {
        Integer id = getCurrentSellerId();
        return ResponseEntity.ok(sellerDashboardService.getTopProducts(id, limit));
    }
    private Integer getCurrentSellerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }

}
