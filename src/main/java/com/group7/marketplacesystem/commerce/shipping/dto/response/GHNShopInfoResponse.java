package com.group7.marketplacesystem.commerce.shipping.dto.response;

import lombok.Data;

/**
 * DTO cho response thông tin GHN shop của seller
 */
@Data
public class GHNShopInfoResponse {
    private Integer id;
    private Integer sellerId;
    private String ghnToken;
    private Integer ghnShopCode;
    private String ghnShopName;
    private String pickupAddress;
    private Integer pickupDistrictId;
    private String pickupWardCode;
    private Integer pickupProvinceId; // Để frontend tự động chọn province
    private String pickupProvinceName;
    private String pickupDistrictName;
    private String pickupWardName;
}

