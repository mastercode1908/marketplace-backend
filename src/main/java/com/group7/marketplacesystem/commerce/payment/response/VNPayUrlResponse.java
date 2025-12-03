package com.group7.marketplacesystem.commerce.payment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VNPayUrlResponse {
    private String paymentUrl;
}
