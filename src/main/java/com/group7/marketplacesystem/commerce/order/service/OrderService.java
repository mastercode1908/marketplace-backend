package com.group7.marketplacesystem.commerce.order.service;

import com.group7.marketplacesystem.commerce.shipping.dto.response.OrderDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    List<OrderDetailResponse> getOrdersByBuyerId(Integer buyerId);
    List<OrderDetailResponse> getOrdersBySellerId(Integer sellerId);
    
    // Pagination methods
    Page<OrderDetailResponse> getOrdersByBuyerId(Integer buyerId, Pageable pageable);
    Page<OrderDetailResponse> getOrdersBySellerId(Integer sellerId, Pageable pageable);
    
    OrderDetailResponse getOrderById(Integer orderId, Integer userId, String role);
    void cancelOrder(Integer orderId, Integer userId, String role, String reason);
    /**
     * Đồng bộ trạng thái đơn hàng từ GHN
     * Lấy trạng thái mới nhất từ GHN và cập nhật vào database
     */
    OrderDetailResponse syncOrderStatusFromGHN(Integer orderId, Integer userId, String role);
}


