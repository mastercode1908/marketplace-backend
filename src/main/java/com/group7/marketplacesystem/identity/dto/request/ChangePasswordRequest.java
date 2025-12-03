package com.group7.marketplacesystem.identity.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePasswordRequest {
    private String oldPassword;

    @jakarta.validation.constraints.NotBlank(message = "Mật khẩu mới không được để trống")
    @jakarta.validation.constraints.Size(min = 8, message = "Mật khẩu phải từ 8 ký tự trở lên")
    @jakarta.validation.constraints.Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.\\[\\]{}\";,<>?/+_=\\-]).*$",
            message = "Mật khẩu phải gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
    )
    private String newPassword;

    private String confirmPassword;
}
