package com.group7.marketplacesystem.identity.service;

import com.group7.marketplacesystem.identity.dto.request.RevenueFilterRequest;
import com.group7.marketplacesystem.identity.dto.response.*;

import java.util.List;

public interface AdminDashboardService {
    DashboardKPIsResponse getKPIs();
    RevenueChartDataResponse getRevenueChartData(String period); // "daily" or "monthly"
    OrderStatusChartResponse getOrderStatusChart();
    UserGrowthChartResponse getUserGrowthChart();
    TopSellersProductsResponse getTopSellersAndProducts(int limit);
    CategoryDistributionResponse getCategoryDistribution();
    
    // Revenue với commission 7%
    AdminRevenueResponse getAdminRevenue(RevenueFilterRequest filter);
    
    // Danh sách đơn hàng chi tiết với commission
    List<OrderDetailRevenueResponse> getOrderDetails(RevenueFilterRequest filter);
}




