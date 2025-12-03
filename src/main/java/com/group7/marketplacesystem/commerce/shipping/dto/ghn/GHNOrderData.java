package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO cho data đơn hàng GHN
 */
@Data
public class GHNOrderData {
    @JsonProperty("order_code")
    private String orderCode;

    @JsonProperty("sort_code")
    private String sortCode;

    @JsonProperty("trans_type")
    private String transType;

    @JsonProperty("ward_encode")
    private String wardEncode;

    @JsonProperty("district_encode")
    private String districtEncode;

    @JsonProperty("expected_delivery_time")
    private String expectedDeliveryTime;

    @JsonProperty("total_fee")
    private Integer totalFee;
}













