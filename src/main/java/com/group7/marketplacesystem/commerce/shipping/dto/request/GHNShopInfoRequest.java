package com.group7.marketplacesystem.commerce.shipping.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO cho request seller nhập thông tin GHN shop
 * Lưu ý: Nếu GHN API không có endpoint lấy shop info, seller cần nhập thủ công
 * pickup_address, pickup_district_id, pickup_ward_code
 */
@Data
public class GHNShopInfoRequest {
    @NotBlank(message = "GHN Token is required")
    private String ghnToken;

    @NotNull(message = "GHN Shop Code is required")
    private Integer ghnShopCode;

    // Các trường optional - seller có thể nhập thủ công nếu GHN API không có endpoint
    private String pickupAddress;
    private Integer pickupDistrictId;
    private String pickupWardCode;
}

