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
public class PaymentResponse {
    private Integer id;
    private Integer targetId;
    private String targetType;
    private String method;
    private BigDecimal amount;
    private String status;
    private String transactionCode;
    private Instant createdAt;
}
