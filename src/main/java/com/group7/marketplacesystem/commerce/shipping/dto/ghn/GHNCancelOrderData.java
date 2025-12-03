package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO cho data response hủy đơn hàng GHN
 */
@Data
public class GHNCancelOrderData {
    @JsonProperty("order_code")
    private String orderCode;

    @JsonProperty("result")
    private Boolean result; // true nếu hủy thành công
}












