package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO cho request tạo đơn hàng GHN
 */
@Data
public class GHNCreateOrderRequest {
    @JsonProperty("payment_type_id")
    private Integer paymentTypeId; // 1: Người bán trả, 2: Người mua trả

    @JsonProperty("required_note")
    private String requiredNote; // "CHOXEMHANGKHONGTHU", "CHOTHUHANG", "KHONGCHOXEMHANG"

    @JsonProperty("to_name")
    private String toName;

    @JsonProperty("to_phone")
    private String toPhone;

    @JsonProperty("to_address")
    private String toAddress;

    @JsonProperty("to_ward_code")
    private String toWardCode;

    @JsonProperty("to_district_id")
    private Integer toDistrictId;

    @JsonProperty("cod_amount")
    private Integer codAmount; // Số tiền thu hộ (0 nếu không thu hộ)

    @JsonProperty("weight")
    private Integer weight; // Trọng lượng (gram)

    @JsonProperty("service_id")
    private Integer serviceId; // ID dịch vụ (có thể null)

    @JsonProperty("service_type_id")
    private Integer serviceTypeId; // 1: Express, 2: Standard

    @JsonProperty("items")
    private List<GHNItem> items;
}













