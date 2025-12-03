package com.group7.marketplacesystem.commerce.shipping.controller;

import com.group7.marketplacesystem.commerce.payment.response.VNPayUrlResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.request.CheckoutRequest;
import com.group7.marketplacesystem.commerce.shipping.dto.response.OrderDetailResponse;
import com.group7.marketplacesystem.commerce.shipping.service.CheckoutService;
import com.group7.marketplacesystem.common.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho checkout và thanh toán
 */
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {
    private final CheckoutService checkoutService;

    /**
     * Thanh toán và tạo đơn hàng
     */
    @PreAuthorize("hasRole('BUYER')")
    @PostMapping
    public ResponseEntity<?> checkout(@Valid @RequestBody CheckoutRequest request) {

        String method = request.getPaymentMethod().toUpperCase();

        if ("COD".equals(method)) {
            // Gọi checkout cũ tạo order + payment COD
            OrderDetailResponse response = checkoutService.checkoutCOD(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if ("VNPAY".equals(method)) {
            // Gọi checkout VNPAY, trả về URL thanh toán
            VNPayUrlResponse response = checkoutService.checkoutVnPay(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Unsupported payment method: " + request.getPaymentMethod());
        }
    }

    private Integer getCurrentBuyerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }
}













