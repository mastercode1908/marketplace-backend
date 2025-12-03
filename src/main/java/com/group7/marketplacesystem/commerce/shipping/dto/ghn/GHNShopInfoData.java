package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO cho data thông tin shop từ GHN API
 */
@Data
public class GHNShopInfoData {
    @JsonProperty("shop_id")
    private Integer shopId;

    @JsonProperty("shop_name")
    private String shopName;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("address")
    private String address;

    @JsonProperty("district_id")
    private Integer districtId;

    @JsonProperty("ward_code")
    private String wardCode;

    @JsonProperty("province_id")
    private Integer provinceId;
}

