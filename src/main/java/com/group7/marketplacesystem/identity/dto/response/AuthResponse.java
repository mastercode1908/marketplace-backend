package com.group7.marketplacesystem.identity.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;

    //    private Integer userId;
//    private String username;
//    private String email;
//    private String fullName;
//    private String role;
    private AuthUserInfo user;

    private String status;     // "INCOMPLETE" | "ACTIVE" | ...
    private String message;    // thông báo thêm
}

