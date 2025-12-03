package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO cho response tạo đơn hàng GHN
 */
@Data
public class GHNCreateOrderResponse {
    @JsonProperty("code")
    private Integer code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private GHNOrderData data;
}













