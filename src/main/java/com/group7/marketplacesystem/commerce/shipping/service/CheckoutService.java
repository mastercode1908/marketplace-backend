package com.group7.marketplacesystem.commerce.shipping.service;

import com.group7.marketplacesystem.commerce.payment.entity.PaymentSession;
import com.group7.marketplacesystem.commerce.payment.response.VNPayUrlResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.request.CheckoutRequest;
import com.group7.marketplacesystem.commerce.shipping.dto.response.OrderDetailResponse;

public interface CheckoutService {
    OrderDetailResponse checkoutCOD(CheckoutRequest request);

    VNPayUrlResponse checkoutVnPay(CheckoutRequest request);

    void checkoutFromPaymentSession(Long sessionId);
}













