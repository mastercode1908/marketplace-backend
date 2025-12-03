package com.group7.marketplacesystem.identity.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerRequest {
    private String full_name;
    private Boolean gender;
    private String phone;
    private String avatar;

    @NotNull(message = "Tên cửa hàng không được để trống.")
    private String shop_name;

    @NotNull(message = "Địa chỉ cửa hàng không được để trống.")
    private String shop_address;
    
    private String shop_description;
}
