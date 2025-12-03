package com.group7.marketplacesystem.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopInfoRequest {

    @NotBlank(message = "Tên cửa hàng không được để trống.")
    @Size(min = 3, max = 50, message = "Tên cửa hàng phải từ 3 đến 50 ký tự.")
    private String shopName;

    @NotBlank(message = "Địa chỉ cửa hàng không được để trống.")
    private String shopAddress;

    @NotBlank(message = "Mô tả cửa hàng không được để trống.")
    @Size(min = 10, message = "Mô tả phải ít nhất 10 ký tự.")
    private String shopDescription;

    @Pattern(
            regexp = "^[0-9]{10,13}$",
            message = "Mã số thuế không hợp lệ (phải là 10–13 chữ số)."
    )
    private String taxCode;
}
