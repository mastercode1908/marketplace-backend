package com.group7.marketplacesystem.commerce.payment.Service;

import com.group7.marketplacesystem.commerce.payment.request.CreatePaymentRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Map;


public interface VNPayService {
    String createPayment(BigDecimal amount, String txnRef) throws UnsupportedEncodingException;

    boolean verifyChecksum(Map<String, String> params);
//    String handleIpn(Map<String, String> params);
}
