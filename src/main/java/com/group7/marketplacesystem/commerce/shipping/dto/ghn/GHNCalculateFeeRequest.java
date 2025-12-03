package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO cho request tính phí vận chuyển GHN
 */
@Data
public class GHNCalculateFeeRequest {
    @JsonProperty("from_district_id")
    private Integer fromDistrictId;

    @JsonProperty("from_ward_code")
    private String fromWardCode;

    @JsonProperty("to_district_id")
    private Integer toDistrictId;

    @JsonProperty("to_ward_code")
    private String toWardCode;

    @JsonProperty("weight")
    private Integer weight; // Trọng lượng (gram)

    @JsonProperty("service_type_id")
    private Integer serviceTypeId; // 1: Express, 2: Standard

    @JsonProperty("service_id")
    private Integer serviceId; // ID dịch vụ (có thể null)
}













