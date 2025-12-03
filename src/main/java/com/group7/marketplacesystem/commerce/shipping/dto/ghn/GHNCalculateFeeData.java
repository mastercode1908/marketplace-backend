package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO cho data tính phí vận chuyển GHN
 */
@Data
public class GHNCalculateFeeData {
    @JsonProperty("total")
    private Integer total; // Tổng phí vận chuyển (VND)

    @JsonProperty("service_fee")
    private Integer serviceFee; // Phí dịch vụ

    @JsonProperty("insurance_fee")
    private Integer insuranceFee; // Phí bảo hiểm

    @JsonProperty("pick_station_fee")
    private Integer pickStationFee; // Phí lấy hàng tại trạm

    @JsonProperty("coupon_value")
    private Integer couponValue; // Giá trị coupon (nếu có)

    @JsonProperty("r2s_fee")
    private Integer r2sFee; // Phí R2S
}













