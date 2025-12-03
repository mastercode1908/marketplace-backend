package com.group7.marketplacesystem.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisterRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 20, message = "Username phải có độ dài từ 3-20 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải từ 8 ký tự trở lên")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.\\[\\]{}\";,<>?/+_=\\-]).*$",
            message = "Mật khẩu phải gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
    )
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Role không được để trống")
    private String role;  // "Buyer" | "Seller"     | "Admin"
}
