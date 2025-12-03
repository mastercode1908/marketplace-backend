package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO cho data chi tiết đơn hàng GHN
 */
@Data
public class GHNOrderDetailData {
    @JsonProperty("order_code")
    private String orderCode;

    @JsonProperty("status")
    private String status; // Trạng thái đơn hàng GHN

    @JsonProperty("delivery_status")
    private String deliveryStatus; // Trạng thái giao hàng

    @JsonProperty("expected_delivery_time")
    private String expectedDeliveryTime; // GHN trả về dưới dạng String

    @JsonProperty("total_fee")
    private Integer totalFee;

    // Có thể thêm các trường khác nếu cần
}

