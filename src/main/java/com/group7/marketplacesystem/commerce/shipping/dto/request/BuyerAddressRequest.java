package com.group7.marketplacesystem.commerce.shipping.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO cho request tạo/cập nhật địa chỉ người nhận
 */
@Data
public class BuyerAddressRequest {
    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Receiver phone is required")
    private String receiverPhone;

    @NotBlank(message = "Address detail is required")
    private String addressDetail;

    @NotBlank(message = "Ward code is required")
    private String wardCode;

    @NotNull(message = "District ID is required")
    private Integer districtId;

    private String provinceName;

    private Boolean isDefault = false;
}













