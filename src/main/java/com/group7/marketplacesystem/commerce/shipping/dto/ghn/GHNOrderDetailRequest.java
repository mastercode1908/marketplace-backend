package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO cho request lấy chi tiết đơn hàng GHN
 */
@Data
public class GHNOrderDetailRequest {
    @JsonProperty("order_code")
    private String orderCode;
}













