package com.group7.marketplacesystem.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerRejectRequest {
    @NotBlank(message = "Ghi chú từ chối không được để trống")
    private String note;
}









