package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO cho response lấy danh sách phường/xã GHN
 */
@Data
public class GHNWardResponse {
    @JsonProperty("code")
    private Integer code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private List<GHNWard> data;
}













