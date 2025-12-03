package com.group7.marketplacesystem.commerce.payment.controller;


import com.group7.marketplacesystem.commerce.payment.Service.VNPayService;
import com.group7.marketplacesystem.commerce.payment.Service.impl.PaymentIpnService;
import com.group7.marketplacesystem.commerce.payment.Service.impl.VNPayServiceImpl;
import com.group7.marketplacesystem.commerce.payment.request.CreatePaymentRequest;
import com.group7.marketplacesystem.commerce.payment.response.PaymentVnpayResponse;
import com.group7.marketplacesystem.common.constants.MessageConstants;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/payment/vnpay")
@RequiredArgsConstructor
public class VNPayController {

    private final VNPayServiceImpl vnPayServiceImpl;
    private final PaymentIpnService paymentIpnService;

    @PostMapping("/create")
    public ResponseEntity<String> createPayment() {
        try {
            String paymentUrl = vnPayServiceImpl.createPayment(BigDecimal.valueOf(1000000), "11111111");
            return ResponseEntity.ok(paymentUrl);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi khi tạo thanh toán!");
        }
    }

    @GetMapping("/ipn")
    public String handleIpn(HttpServletRequest request) {

        Map<String, String> params = request.getParameterMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue()[0]
                ));

        return paymentIpnService.process(params); // trả code 00/97/01
    }



    @GetMapping("/return")
    public ResponseEntity<ApiResponse<PaymentVnpayResponse>> returnPayment(
            @RequestParam(name = "vnp_TxnRef") String txnRef) {
        try {
            PaymentVnpayResponse response = vnPayServiceImpl.getPaymentByTxnRef(txnRef);

            return ResponseEntity.ok(
                    ApiResponse.success(MessageConstants.PAYMENT_RETURN_SUCCESS, response)
            );

        } catch (ApiException ex) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.fail(MessageConstants.PAYMENT_RETURN_FAILED));

        } catch (Exception ex) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.fail("Internal server error"));
        }
    }
}
