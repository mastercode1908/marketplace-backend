package com.group7.marketplacesystem.identity.service;

import com.group7.marketplacesystem.identity.dto.response.*;

import java.util.List;

public interface SellerDashboardService {

    SellerKPIResponse getKPI(Integer id); // đơn, doanh thu, sp, rating...

    OrderGrowthChartResponse getOrderGrowthChart(Integer id, String period); // daily / monthly

    ReviewStatsResponse getReviewStats(Integer id);

    List<OrderCommissionResponse> getOrderCommissions(Integer id); // chi tiết hoa hồng từng đơn

    List<TopProductResponse> getTopProducts(Integer id, int limit); // sản phẩm bán chạy

}
