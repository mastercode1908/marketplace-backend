package com.group7.marketplacesystem.commerce.shipping.dto.response;

import lombok.Data;

import java.time.Instant;

/**
 * DTO cho response địa chỉ người nhận
 */
@Data
public class BuyerAddressResponse {
    private Integer id;
    private Integer buyerId;
    private String receiverName;
    private String receiverPhone;
    private String addressDetail;
    private String wardCode;
    private Integer districtId;
    private String provinceName;
    private Boolean isDefault;
    private Instant createdAt;
    private Instant updatedAt;
}













