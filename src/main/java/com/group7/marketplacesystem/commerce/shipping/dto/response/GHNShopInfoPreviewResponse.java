package com.group7.marketplacesystem.commerce.shipping.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response cho preview shop info từ GHN
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GHNShopInfoPreviewResponse {
    private String shopName;
    private String address;
    private Integer districtId;
    private String wardCode;
    private Integer provinceId;
    private String provinceName;
    private String districtName;
    private String wardName;
}



