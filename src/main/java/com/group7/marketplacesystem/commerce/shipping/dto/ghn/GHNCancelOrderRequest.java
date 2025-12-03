package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO cho request hủy đơn hàng GHN
 */
@Data
public class GHNCancelOrderRequest {
    @JsonProperty("order_codes")
    private String[] orderCodes; // Mã đơn hàng GHN cần hủy
}












