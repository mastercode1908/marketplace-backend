package com.group7.marketplacesystem.commerce.payment.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePaymentRequest {
    private Integer targetId;
    private String targetType;
    private String amount;
    private String method;
}
