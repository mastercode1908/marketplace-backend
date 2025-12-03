package com.group7.marketplacesystem.identity.service.impl;

import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.catalog.repository.ReviewRepository;
import com.group7.marketplacesystem.commerce.order.repository.OrderDetailRepository;
import com.group7.marketplacesystem.commerce.order.repository.OrderRepository;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.commerce.order.entity.Order;
import com.group7.marketplacesystem.identity.dto.response.OrderCommissionResponse;
import com.group7.marketplacesystem.identity.dto.response.OrderGrowthChartResponse;
import com.group7.marketplacesystem.identity.dto.response.ReviewStatsResponse;
import com.group7.marketplacesystem.identity.dto.response.SellerKPIResponse;
import com.group7.marketplacesystem.identity.dto.response.TopProductResponse;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.repository.SellerRepository;
import com.group7.marketplacesystem.identity.service.SellerDashboardService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class SellerDashboardServiceImpl implements SellerDashboardService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final SellerRepository sellerRepository;


    @Override
    public SellerKPIResponse getKPI(Integer id) {

        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));

        long totalProducts = productRepository.countBySellerId(seller.getId());

        long totalOrders = orderRepository.countBySellerId(seller.getId());

        double totalRevenue = orderRepository.sumRevenueBySellerId(seller.getId());

        double averageRating = reviewRepository.getSellerAverageRating(seller.getId());

        long pendingOrders = orderRepository.countBySellerIdAndStatus(seller.getId(), "PENDING");
        long completedOrders  = orderRepository.countBySellerIdAndStatus(seller.getId(), "DELIVERED");
        long cancelledOrders = orderRepository.countBySellerIdAndStatus(seller.getId(), "CANCELLED");

        // Tính tổng hoa hồng = Sum(mỗi order DELIVERED × 7%)
        double commissionRate = 7.0;
        List<Order> orders = orderRepository.findBySellerId(seller.getId());
        double totalCommission = orders.stream()
                .filter(order -> "DELIVERED".equalsIgnoreCase(order.getOrderStatus())) // Chỉ tính đơn đã giao
                .mapToDouble(order -> {
                    double orderTotal = order.getFinalAmount() != null ? 
                            order.getFinalAmount().doubleValue() : 0.0;
                    return orderTotal * (commissionRate / 100);
                })
                .sum();

        return SellerKPIResponse.builder()
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .averageRating(averageRating)
                .pendingOrders(pendingOrders)
                .completedOrders (completedOrders )
                .cancelledOrders(cancelledOrders)
                .totalCommission(totalCommission)
                .commissionRate(commissionRate)
                .build();
    }

    @Override
    public OrderGrowthChartResponse getOrderGrowthChart(Integer id, String period) {

        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));

        List<Object[]> rawData;

        if ("monthly".equalsIgnoreCase(period)) {
            rawData = orderRepository.getMonthlyOrderGrowth(seller.getId()) != null ?
            orderRepository.getMonthlyOrderGrowth(seller.getId()) : new ArrayList<>();
        } else if ("weekly".equalsIgnoreCase(period)) {
            rawData = orderRepository.getWeeklyOrderGrowth(seller.getId()) != null ?
            orderRepository.getWeeklyOrderGrowth(seller.getId()) : new ArrayList<>();
        } else {
            rawData = orderRepository.getDailyOrderGrowth(seller.getId()) != null ?
            orderRepository.getDailyOrderGrowth(seller.getId()) : new ArrayList<>();
        }

        List<String> labels = new ArrayList<>();
        List<Long> orderCounts = new ArrayList<>();
        List<Double> revenues = new ArrayList<>();

        for (Object[] r : rawData) {
            labels.add(Objects.toString(r[0]));
            orderCounts.add(((Number) r[1]).longValue());
            revenues.add(((BigDecimal) r[2]).doubleValue());
        }

        return OrderGrowthChartResponse.builder()
                .labels(labels)
                .orderCounts(orderCounts)
                .revenues(revenues)
                .build();
    }

    @Override
    public ReviewStatsResponse getReviewStats(Integer id) {

        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));

        Integer total = reviewRepository.countBySellerId(seller.getId());

        if (total == 0) {
            return ReviewStatsResponse.builder()
                    .totalReviews(0)
                    .averageRating(0.0)
                    .build();
        }

        long one = reviewRepository.countBySellerIdAndRating(seller.getId(), 1);
        long two = reviewRepository.countBySellerIdAndRating(seller.getId(), 2);
        long three = reviewRepository.countBySellerIdAndRating(seller.getId(), 3);
        long four = reviewRepository.countBySellerIdAndRating(seller.getId(), 4);
        long five = reviewRepository.countBySellerIdAndRating(seller.getId(), 5);

        double avg = reviewRepository.getSellerAverageRating(seller.getId()) != null ?
                reviewRepository.getSellerAverageRating(seller.getId()) : 0.0;

        return ReviewStatsResponse.builder()
                .totalReviews(total)
                .averageRating(avg)
                .oneStar(one)
                .twoStar(two)
                .threeStar(three)
                .fourStar(four)
                .fiveStar(five)
                .build();
    }

    @Override
    public List<OrderCommissionResponse> getOrderCommissions(Integer id) {

        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));

        List<Order> orders = orderRepository.findBySellerId(seller.getId());

        double commissionRate = 7.0;

        return orders.stream()
                .filter(order -> "DELIVERED".equalsIgnoreCase(order.getOrderStatus())) // Chỉ hiển thị đơn đã giao
                .map(order -> OrderCommissionResponse.builder()
                        .orderId(order.getId())
                        .orderDate(order.getOrderDate() != null ? 
                                java.time.LocalDateTime.ofInstant(order.getOrderDate(), 
                                        java.time.ZoneId.systemDefault()) : null)
                        .orderTotal(order.getFinalAmount() != null ? 
                                order.getFinalAmount().doubleValue() : 0.0)
                        .commissionRate(commissionRate)
                        .commissionAmount((order.getFinalAmount() != null ? 
                                order.getFinalAmount().doubleValue() : 0.0) * (commissionRate / 100))
                        .status(order.getOrderStatus())
                        .build())
                .toList();
    }

    @Override
    public List<TopProductResponse> getTopProducts(Integer id, int limit) {

        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));

        List<Object[]> rawData = orderDetailRepository.getTopProductsBySeller(seller.getId());

        double commissionRate = 7.0;

        return rawData.stream()
                .limit(limit)
                .map(row -> {
                    Integer productId = (Integer) row[0];
                    String productName = (String) row[1];
                    String imageUrl = (String) row[2];
                    Long soldQuantity = ((Number) row[3]).longValue();
                    Double totalRevenue = ((BigDecimal) row[4]).doubleValue();
                    Double commissionAmount = totalRevenue * (commissionRate / 100);

                    return TopProductResponse.builder()
                            .productId(productId)
                            .productName(productName)
                            .imageUrl(imageUrl)
                            .soldQuantity(soldQuantity)
                            .totalRevenue(totalRevenue)
                            .commissionAmount(commissionAmount)
                            .build();
                })
                .toList();
    }
}
