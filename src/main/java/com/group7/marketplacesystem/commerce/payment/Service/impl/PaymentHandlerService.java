package com.group7.marketplacesystem.commerce.payment.Service.impl;

import com.group7.marketplacesystem.commerce.payment.entity.PaymentSession;
import com.group7.marketplacesystem.commerce.payment.repository.PaymentSessionRepository;
import com.group7.marketplacesystem.commerce.shipping.service.CheckoutService;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.promotion.service.ServicePackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentHandlerService {

    private final CheckoutService checkoutService;
    private final PaymentSessionRepository paymentSessionRepository;
    private final ServicePackageService servicePackageService;

    /**
     * Xử lý khi thanh toán VNPAY thành công
     *
     * @param txnRef Mã giao dịch VNPAY
     */
    @Async
    public void handleSuccessfulPayment(String txnRef) {
        try {
            log.info("Starting async payment processing for txnRef: {}", txnRef);
            // 1. Tìm PaymentSession theo txnRef
            PaymentSession session = (PaymentSession) paymentSessionRepository.findByTxnRef(txnRef)
                    .orElseThrow(() -> new ApiException(ErrorCode.SESSION_NOT_FOUND));

            if (session.getSellerId() == null) {
                // 2. Gọi checkout từ session
                checkoutService.checkoutFromPaymentSession(session.getId());
            } else {
                // 3. Seller mua gói dịch vụ
                servicePackageService.createOrUpdateSellerPackage(session);
                // 4. Cập nhật trạng thái session
                session.setStatus("COMPLETED");
                paymentSessionRepository.save(session);
            }
            log.info("Successfully processed payment for txnRef: {}", txnRef);
        } catch (Exception e) {
            log.error("Error processing payment for txnRef: {}", txnRef, e);
            // Log lỗi nhưng không throw để không ảnh hưởng đến response của IPN
        }
    }
}