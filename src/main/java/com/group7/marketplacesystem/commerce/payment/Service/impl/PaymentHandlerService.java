package com.group7.marketplacesystem.commerce.payment.Service.impl;

import com.group7.marketplacesystem.commerce.payment.entity.PaymentSession;
import com.group7.marketplacesystem.commerce.payment.repository.PaymentSessionRepository;
import com.group7.marketplacesystem.commerce.shipping.service.CheckoutService;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.promotion.service.ServicePackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentHandlerService {

    private final CheckoutService checkoutService;
    private final PaymentSessionRepository paymentSessionRepository;
    private final ServicePackageService servicePackageService;

    /**
     * Xử lý khi thanh toán VNPAY thành công
     * @param txnRef Mã giao dịch VNPAY
     */
    public void handleSuccessfulPayment(String txnRef) {
        // 1. Tìm PaymentSession theo txnRef
        PaymentSession session = (PaymentSession) paymentSessionRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> new ApiException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getSellerId() == null) {
            // 2. Gọi checkout từ session
            checkoutService.checkoutFromPaymentSession(session.getId());
        }else{
            // 3. Seller mua gói dịch vụ
            servicePackageService.createOrUpdateSellerPackage(session);
            // 4. Cập nhật trạng thái session
            session.setStatus("COMPLETED");
            paymentSessionRepository.save(session);
        }

    }
}
