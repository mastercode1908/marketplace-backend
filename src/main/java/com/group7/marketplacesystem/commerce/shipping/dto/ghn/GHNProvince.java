package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho tỉnh/thành phố GHN
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GHNProvince {
    @JsonProperty("ProvinceID")
    private Integer provinceId;

    @JsonProperty("ProvinceName")
    private String provinceName;
}

