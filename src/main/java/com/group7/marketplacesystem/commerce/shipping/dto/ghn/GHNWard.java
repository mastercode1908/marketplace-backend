package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO cho phường/xã GHN
 */
@Data
public class GHNWard {
    @JsonProperty("WardCode")
    private String wardCode;

    @JsonProperty("WardName")
    private String wardName;

    @JsonProperty("DistrictID")
    private Integer districtId;
}













