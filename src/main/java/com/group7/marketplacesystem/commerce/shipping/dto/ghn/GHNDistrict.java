package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO cho quận/huyện GHN
 */
@Data
public class GHNDistrict {
    @JsonProperty("DistrictID")
    private Integer districtId;

    @JsonProperty("DistrictName")
    private String districtName;

    @JsonProperty("ProvinceID")
    private Integer provinceId;
}













