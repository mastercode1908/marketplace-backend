package com.group7.marketplacesystem.commerce.shipping.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request để preview shop info từ GHN (không lưu vào DB)
 */
@Data
public class GHNShopInfoPreviewRequest {
    @NotBlank(message = "GHN Token không được để trống")
    private String ghnToken;

    @NotNull(message = "GHN Shop Code không được để trống")
    private Integer ghnShopCode;
}











