package com.group7.marketplacesystem.commerce.payment.Service.impl;

import com.group7.marketplacesystem.commerce.payment.Service.VNPayService;
import com.group7.marketplacesystem.commerce.payment.entity.Payment;
import com.group7.marketplacesystem.commerce.payment.entity.PaymentSession;
import com.group7.marketplacesystem.commerce.payment.entity.Paymentvnpay;
import com.group7.marketplacesystem.commerce.payment.repository.PaymentSessionRepository;
import com.group7.marketplacesystem.commerce.payment.repository.PaymentvnpayRepository;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentIpnService {

    private final PaymentSessionRepository paymentSessionRepository;
    private final PaymentHandlerService paymentHandlerService;
    private final VNPayService vnPayService; // dùng để verify checksum
    private final PaymentvnpayRepository paymentvnpayRepository; // thêm repo

    public String process(Map<String, String> params) {

        // 1. Verify chữ ký (checksum)
        boolean valid = vnPayService.verifyChecksum(params);
        if (!valid) {
            System.out.println("IPN checksum sai!");
            return "97"; // checksum sai
        }

        // 2. Lấy thông tin IPN
        String txnRef = params.get("vnp_TxnRef");
        String transactionNo = params.get("vnp_TransactionNo");
        String responseCode = params.get("vnp_ResponseCode");
        String txnStatus = params.get("vnp_TransactionStatus");
        String bankCode = params.get("vnp_BankCode");
        String orderInfo = params.get("vnp_OrderInfo");
        String amountStr = params.get("vnp_Amount"); // vnpay trả về tiền *100 (cần /100)
        Instant payDate = null;
        String rawPayDate = params.get("vnp_PayDate");

        if (rawPayDate != null) {
            try {
                // VNPAY gửi về dạng yyyyMMddHHmmss → parse vào LocalDateTime
                LocalDateTime ldt = LocalDateTime.parse(
                        rawPayDate,
                        DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                );

                payDate = ldt.toInstant(ZoneOffset.UTC); // convert về UTC để sync FE
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        BigDecimal amount = BigDecimal.ZERO;
        if (amountStr != null && !amountStr.isEmpty()) {
            try {
                amount = new BigDecimal(amountStr).divide(BigDecimal.valueOf(100)); // vnpay trả về *100
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // 4. Lưu thông tin IPN vào PaymentVnpay
        Paymentvnpay paymentvnpay = new Paymentvnpay();
        paymentvnpay.setVnpTransactionCode(transactionNo);
        paymentvnpay.setVnpResponseCode(responseCode);
        paymentvnpay.setVnpBankCode(bankCode);
        paymentvnpay.setVnpPayDate(payDate);
        paymentvnpay.setVnpOrderInfo(orderInfo);
        paymentvnpay.setAmount(amount);
        paymentvnpay.setVnpTxnRef(txnRef);
        paymentvnpay.setCreatedAt(Instant.now());
        // Cập nhật status dựa vào IPN
        if ("00".equals(responseCode) && "00".equals(txnStatus)) {
            paymentvnpay.setStatus("Success");
        } else {
            paymentvnpay.setStatus("Failed");
        }
        paymentvnpayRepository.save(paymentvnpay);

        // 4. Trường hợp thanh toán thành công
        if ("00".equals(responseCode) && "00".equals(txnStatus)) {
            try {
                paymentHandlerService.handleSuccessfulPayment(txnRef);
            } catch (Exception e) {
                e.printStackTrace();
                return "99"; // lỗi xử lý order
            }
        } else {
            System.out.println(">>> Payment failed for txnRef = " + txnRef);
            // Bạn có thể cập nhật session.setStatus("FAILED") nếu muốn
        }

        // 5. Báo VNPAY là server đã xử lý OK
        return "00";
    }
}
