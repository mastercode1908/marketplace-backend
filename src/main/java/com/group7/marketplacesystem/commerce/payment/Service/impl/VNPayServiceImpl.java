package com.group7.marketplacesystem.commerce.payment.Service.impl;


import com.group7.marketplacesystem.commerce.payment.Service.VNPayService;
import com.group7.marketplacesystem.commerce.payment.entity.PaymentSession;
import com.group7.marketplacesystem.commerce.payment.entity.Paymentvnpay;
import com.group7.marketplacesystem.commerce.payment.mapper.PaymentVnpayMapper;
import com.group7.marketplacesystem.commerce.payment.repository.PaymentSessionRepository;
import com.group7.marketplacesystem.commerce.payment.repository.PaymentvnpayRepository;
import com.group7.marketplacesystem.commerce.payment.response.PaymentVnpayResponse;
import com.group7.marketplacesystem.common.config.VNPayConfig;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.common.utils.VNPayUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {

    private final VNPayConfig vnPayConfig;
    private final VNPayUtils vnPayUtils;
    private final PaymentvnpayRepository  paymentvnpayRepository;
    private final PaymentVnpayMapper paymentVnpayMapper;
    private final PaymentSessionRepository paymentSessionRepository;

    @Override
    public String createPayment(BigDecimal amount, String txnRef) throws UnsupportedEncodingException {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(ErrorCode.INVALID_AMOUNT);
        }

        long vnpAmount = amount.multiply(BigDecimal.valueOf(100)).longValue(); // VNPAY yêu cầu *100

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnpAmount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", txnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + txnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.vnp_ReturnUrl);

        // ⚠ Lấy IP thật thay vì 127.0.0.1
        String vnp_IpAddr = "0.0.0.0";
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);


        // ======================= FIX TIMEZONE =======================
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        String vnp_CreateDate = LocalDateTime.now(zone).format(formatter);
        String vnp_ExpireDate = LocalDateTime.now(zone).plusMinutes(15).format(formatter);

        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        // =============================================================

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                query.append('&');
                hashData.append('&');
            }
        }

        if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);
        if (query.length() > 0) query.setLength(query.length() - 1);

        String vnp_SecureHash = vnPayUtils.hmacSHA512(vnPayConfig.vnp_HashSecret, hashData.toString());
        return vnPayConfig.vnp_PayUrl + "?" + query + "&vnp_SecureHash=" + vnp_SecureHash;
    }

    @Override
    public boolean verifyChecksum(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        String vnpHashSecret = vnPayConfig.vnp_HashSecret;

        Map<String, String> sorted = new TreeMap<>(params);
        sorted.remove("vnp_SecureHash");
        sorted.remove("vnp_SecureHashType");

        StringBuilder sb = new StringBuilder();
        for (String key : sorted.keySet()) {
            sb.append(key).append("=")
                    .append(URLEncoder.encode(sorted.get(key), StandardCharsets.UTF_8))
                    .append("&");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);

        String expectedHash = VNPayUtils.hmacSHA512(vnpHashSecret, sb.toString());

        return expectedHash.equalsIgnoreCase(vnpSecureHash);
    }


    public PaymentVnpayResponse getPaymentByTxnRef(String txnRef) {
        Paymentvnpay payment = paymentvnpayRepository.findTopByVnpTxnRefOrderByCreatedAtDesc(txnRef)
                .orElseThrow(() -> new ApiException(ErrorCode.PAYMENT_VNPAY_NOT_FOUND));
        PaymentVnpayResponse response = paymentVnpayMapper.toResponse(payment);

        // Xác định loại payment: seller (mua gói dịch vụ) hay buyer (mua hàng)
        // Mặc định là false (buyer) nếu không tìm thấy session
        response.setIsSellerPayment(false);
        paymentSessionRepository.findByTxnRef(txnRef)
                .ifPresent(session -> {
                    PaymentSession paymentSession = (PaymentSession) session;
                    // Nếu có sellerId thì là seller mua gói dịch vụ
                    response.setIsSellerPayment(paymentSession.getSellerId() != null);
                });

        return response;
    }

}