package com.group7.marketplacesystem.identity.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {
    private String refreshToken;
}
