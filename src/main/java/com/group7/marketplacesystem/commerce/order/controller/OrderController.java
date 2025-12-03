package com.group7.marketplacesystem.commerce.order.controller;

import com.group7.marketplacesystem.commerce.order.dto.request.CancelOrderRequest;
import com.group7.marketplacesystem.commerce.order.service.OrderService;
import com.group7.marketplacesystem.commerce.shipping.dto.response.OrderDetailResponse;
import com.group7.marketplacesystem.common.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho đơn hàng
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    /**
     * Lấy danh sách đơn hàng của buyer (với phân trang)
     */
    @PreAuthorize("hasRole('BUYER')")
    @GetMapping("/buyer")
    public ResponseEntity<Page<OrderDetailResponse>> getBuyerOrders(
            @PageableDefault(size = 10, sort = "orderDate") Pageable pageable) {
        Integer buyerId = getCurrentUserId();
        return ResponseEntity.ok(orderService.getOrdersByBuyerId(buyerId, pageable));
    }

    /**
     * Lấy danh sách đơn hàng của seller (với phân trang)
     */
    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/seller")
    public ResponseEntity<Page<OrderDetailResponse>> getSellerOrders(
            @PageableDefault(size = 10, sort = "orderDate") Pageable pageable) {
        Integer sellerId = getCurrentUserId();
        return ResponseEntity.ok(orderService.getOrdersBySellerId(sellerId, pageable));
    }

    /**
     * Lấy chi tiết đơn hàng
     */
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrder(@PathVariable Integer orderId) {
        Integer userId = getCurrentUserId();
        String role = getCurrentUserRole();
        return ResponseEntity.ok(orderService.getOrderById(orderId, userId, role));
    }

    /**
     * Hủy đơn hàng (buyer và seller đều có thể hủy)
     * Seller bắt buộc phải nhập lý do
     */
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(
            @PathVariable Integer orderId,
            @RequestBody(required = false) CancelOrderRequest request) {
        Integer userId = getCurrentUserId();
        String role = getCurrentUserRole();
        String reason = request != null ? request.getReason() : null;
        orderService.cancelOrder(orderId, userId, role, reason);
        return ResponseEntity.ok("Order cancelled successfully");
    }

    /**
     * Đồng bộ trạng thái đơn hàng từ GHN
     * Lấy trạng thái mới nhất từ GHN và cập nhật vào database
     * Cho phép cả buyer và seller đồng bộ
     */
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    @PostMapping("/{orderId}/sync-ghn")
    public ResponseEntity<OrderDetailResponse> syncOrderStatusFromGHN(@PathVariable Integer orderId) {
        Integer userId = getCurrentUserId();
        String role = getCurrentUserRole();
        OrderDetailResponse response = orderService.syncOrderStatusFromGHN(orderId, userId, role);
        return ResponseEntity.ok(response);
    }

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }

    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getRole();
    }
}


