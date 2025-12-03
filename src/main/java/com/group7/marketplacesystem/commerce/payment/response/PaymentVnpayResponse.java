package com.group7.marketplacesystem.commerce.payment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentVnpayResponse {
    private Integer id;
    private String vnpTxnRef;
    private String vnpTransactionCode;
    private String vnpResponseCode;
    private String vnpBankCode;
    private Instant vnpPayDate; // hoặc LocalDateTime nếu muốn giờ VN
    private String vnpOrderInfo;
    private String status;
    private Instant createdAt;
    private BigDecimal amount;
    private Boolean isSellerPayment;
}
