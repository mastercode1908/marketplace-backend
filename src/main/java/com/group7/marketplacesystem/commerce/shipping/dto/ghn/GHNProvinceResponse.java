package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO cho response lấy danh sách tỉnh/thành phố GHN
 */
@Data
public class GHNProvinceResponse {
    @JsonProperty("code")
    private Integer code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private List<GHNProvince> data;
}













